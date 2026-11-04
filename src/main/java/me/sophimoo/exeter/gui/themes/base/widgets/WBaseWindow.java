package me.sophimoo.exeter.gui.themes.base.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import me.sophimoo.exeter.gui.screens.BaseModulesScreen;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleSettingsIndicator;
import me.sophimoo.exeter.gui.themes.base.utils.enums.SelectionRenderingMode;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.mixininterface.IGameRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.Click;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WBaseWindow extends WWindow implements BaseWidget {
    public boolean useSettingsPanel = true;

    private WBaseSettingsPanel settingsPanel;
    private BaseModulesScreen modulesScreen;
    // https://github.com/X-C-0/catppuccin-addon/blob/d642959fbaa9e5757013ea38f57556eb88c8b822/src/main/java/me/pindour/catppuccin/gui/themes/catppuccin/widgets/container/WCatppuccinWindow.java#L29
    private double mouseOffsetX;
    private double mouseOffsetY;

    public WBaseWindow(WWidget icon, String title) {
        super(icon, title);
    }

    @Override
    public void init() {
        super.init();
        if (useSettingsPanel) {
            settingsPanel = view.add(new WBaseSettingsPanel()).expandX().widget();
        }
    }

    @Override
    public <T extends WWidget> Cell<T> add(T widget) {
        return settingsPanel != null ? settingsPanel.add(widget) : super.add(widget);
    }

    @Override
    public void clear() {
        if (settingsPanel != null) settingsPanel.clear();
        else super.clear();
    }

    public void initSnapping(BaseModulesScreen modulesScreen) {
        this.modulesScreen = modulesScreen;
    }

    public void moveTo(double targetX, double targetY) {
        moved = true;
        movedX = targetX;
        movedY = targetY;
        move(targetX - x, targetY - y);
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WBaseHeader(icon);
    }

    @Override
    public void calculateSize() {
        padding = theme().windowOutlineThickness.get();
        for (Cell<?> cell : cells) {
            if (cell.widget() == view) cell.pad(padding);
        }
        super.calculateSize();

        if (theme().shouldUseFixedCategoryWidth(id)) {
            width = theme().scaledFixedCategoryWidth();
        }
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!visible) return true;
        if (padding != theme().windowOutlineThickness.get()) invalidate();

        renderer.scissorStart(x, y, width, (height - header.height) * animProgress + header.height);

        if (expanded || animProgress > 0) {
            renderQuadWithOptionalBlur(renderer, x, y + header.height, width, height - header.height, theme().backgroundColor.get());
        }

        super.render(renderer, mouseX, mouseY, delta);

        renderer.scissorEnd();

        // https://github.com/MeteorDevelopment/meteor-client/commit/6f385e2ecd0a0c18a781d65ca79e6d0ea3a1549f
        // this commit introduces the mixin in meteor for this
        var projection = RenderSystem.getProjectionMatrixBuffer();
        var projectionType = RenderSystem.getProjectionType();
        try {
            ((IGameRenderer) mc.gameRenderer).meteor$flushGuiState();
        } finally {
            RenderSystem.setProjectionMatrix(projection, projectionType);
        }

        if (expanded || animProgress > 0) {
            double thickness = theme.scale(theme().windowOutlineThickness.get());
            if (thickness > 0) {
                double contentY = y + header.height;
                double contentHeight = (height - header.height) * animProgress;
                if (animProgress > 0) {
                    WBaseHeader categoryHeader = (WBaseHeader) header;
                    renderRowSurface(renderer, x, contentY, width, contentHeight, SelectionRenderingMode.FADE,
                        categoryHeader.activeProgress, localHoverSurfaceProgress(categoryHeader.hoverProgress),
                        categoryTitleRowSurfaceStyle(categoryHeader.activeProgress > 0, categoryHeader.mouseOver), thickness);
                }
            }
        }

        return false;
    }

    private class WBaseHeader extends WHeader {
        private double activeProgress = WBaseWindow.this.expanded ? 1.0 : 0.0;
        private double hoverProgress;

        private WHorizontalList list;
        private WLabel titleLabel;
        private WIndicator indicator;

        public WBaseHeader(WWidget icon) {
            super(icon);
        }

        private ModuleSettingsIndicator resolveIndicatorStyle() {
            return theme().indicatorsOnCategoryTitles.get() ? theme().indicatorType.get() : ModuleSettingsIndicator.NONE;
        }

        private String safeIndicator(String value) {
            return value == null ? "" : value;
        }

        @Override
        public void init() {
            list = add(theme.horizontalList()).expandX().widget();
            list.spacing = 0;

            if (WBaseWindow.this.icon != null) add(WBaseWindow.this.icon).centerY();
            if (beforeHeaderInit != null) beforeHeaderInit.accept(this);

            double pad = pad();
            titleLabel = theme.label(title, true);
            Cell<?> titleCell = add(titleLabel).expandCellX().centerY().padLeft(pad).padRight(pad);

            AlignmentX alignment = theme().categoryTitleAlignment.get();
            if (alignment == AlignmentX.Center) titleCell.center();
            else if (alignment == AlignmentX.Right) titleCell.right().centerY();

            if (resolveIndicatorStyle() != ModuleSettingsIndicator.NONE) {
                indicator = add(new WIndicator()).right().centerY().padRight(pad).widget();
            }
        }

        @Override
        public <T extends WWidget> Cell<T> add(T widget) {
            if (list != null) return list.add(widget);
            return super.add(widget);
        }

        @Override
        protected void onCalculateSize() {
            super.onCalculateSize();
            double pad = pad();
            height = resolveCategoryTitleRowHeight(Math.max(height, pad + theme().textHeight() + pad));
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            RowAnimationState animationState = animateRow(delta, mouseOver, false, mouseOver, 0, hoverProgress);
            hoverProgress = animationState.hoverProgress();
            activeProgress = stepProgress(activeProgress, WBaseWindow.this.expanded, delta);

            renderQuadWithOptionalBlur(renderer, x, y, width, height, theme().backgroundColor.get());
            RowSurfaceStyle surfaceStyle = categoryTitleRowSurfaceStyle(activeProgress > 0, mouseOver);
            renderRowSurface(renderer, x, y, width, height, animationState.effectiveAnimationMode(), activeProgress, localHoverSurfaceProgress(hoverProgress), surfaceStyle, 0);
            renderInterpolationHover(renderer, x, y, width, height, mouseOver, delta, surfaceStyle);

            Color textColor = resolveCategoryTitleTextColor(activeProgress, hoverProgress);
            if (titleLabel != null) titleLabel.color = textColor;
            if (indicator != null) indicator.color = textColor;
        }

        private class WIndicator extends WWidget implements BaseWidget {
            private Color color;
            private double exeterRotation;
            private double meteorRotation = Double.NaN;

            @Override
            protected void onCalculateSize() {
                width = indicatorWidth(resolveIndicatorStyle());
                height = theme().textHeight();
            }

            @Override
            protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
                double pad = pad();
                double rowY = WBaseHeader.this.y;
                double rowHeight = WBaseHeader.this.height;

                switch (resolveIndicatorStyle()) {
                    case EXETER -> renderExeterIndicator(renderer, x, rowY, width, rowHeight, pad, exeterRotation = stepExeterIndicatorRotation(exeterRotation, WBaseWindow.this.expanded, delta), color);
                    case METEOR -> renderMeteorIndicator(renderer, x, rowY, width, rowHeight, pad, meteorRotation = stepMeteorIndicatorRotation(meteorRotation, WBaseWindow.this.expanded, delta), color);
                    case DROPDOWN -> renderText(renderer, activeProgress >= 0.5 ? safeIndicator(theme().expandedIndicator.get()) : safeIndicator(theme().collapsedIndicator.get()), x, rowY + (rowHeight - theme().textHeight()) / 2, color);
                    default -> {}
                }
            }
        }

        // https://github.com/X-C-0/catppuccin-addon/blob/d642959fbaa9e5757013ea38f57556eb88c8b822/src/main/java/me/pindour/catppuccin/gui/themes/catppuccin/widgets/container/WCatppuccinWindow.java#L259
        @Override
        public boolean onMouseClicked(Click click, boolean doubled) {
            boolean clicked = super.onMouseClicked(click, doubled);

            if (clicked && shouldSnap()) {
                mouseOffsetX = click.x() - WBaseWindow.this.x;
                mouseOffsetY = click.y() - WBaseWindow.this.y;
            }

            return clicked;
        }

        @Override
        public boolean onMouseReleased(Click click) {
            if (modulesScreen != null) modulesScreen.showGrid(false);
            return super.onMouseReleased(click);
        }

        @Override
        public void onMouseMoved(double mouseX, double mouseY, double lastMouseX, double lastMouseY) {
            if (!shouldSnap()) {
                super.onMouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);
                return;
            }

            if (!dragging) return;

            int gridSize = theme().snappingGridSize.get();
            double targetX = snapToGrid(mouseX - mouseOffsetX, gridSize);
            double targetY = snapToGrid(mouseY - mouseOffsetY, gridSize);

            WBaseWindow.this.move(targetX - WBaseWindow.this.x, targetY - WBaseWindow.this.y);

            moved = true;
            movedX = WBaseWindow.this.x;
            movedY = WBaseWindow.this.y;

            if (id != null) {
                WindowConfig config = theme.getWindowConfig(id);
                config.x = WBaseWindow.this.x;
                config.y = WBaseWindow.this.y;
            }

            if (modulesScreen != null && !modulesScreen.showGrid()) modulesScreen.showGrid(true);
            dragged = true;
        }
    }

    private boolean shouldSnap() {
        return modulesScreen != null && theme().snapModuleCategories.get();
    }

    private double snapToGrid(double value, int gridSize) {
        return Math.round(value / gridSize) * gridSize;
    }
}
