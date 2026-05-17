package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.screens.BaseModulesScreen;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.Click;

public class WBaseWindow extends WWindow implements BaseWidget {
    private BaseModulesScreen modulesScreen;
    // https://github.com/X-C-0/catppuccin-addon/blob/d642959fbaa9e5757013ea38f57556eb88c8b822/src/main/java/me/pindour/catppuccin/gui/themes/catppuccin/widgets/container/WCatppuccinWindow.java#L29
    private double mouseOffsetX;
    private double mouseOffsetY;

    public WBaseWindow(WWidget icon, String title) {
        super(icon, title);
    }

    public void initSnapping(BaseModulesScreen modulesScreen) {
        this.modulesScreen = modulesScreen;
    }

    @Override
    protected WHeader header(WWidget icon) {
        return new WBaseHeader(icon);
    }

    @Override
    public void calculateSize() {
        super.calculateSize();

        if (theme().shouldUseFixedCategoryWidth(id)) {
            width = theme().scaledFixedCategoryWidth();
        }
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (padding == 0) {
            padding = theme.scale(theme().windowOutlineThickness.get());
        }
        
        if (!visible) return true;

        boolean scissor = (animProgress != 0 && animProgress != 1) || (expanded && animProgress != 1);
        if (scissor) renderer.scissorStart(x, y, width, (height - header.height) * animProgress + header.height);

        if (expanded || animProgress > 0) {
            renderQuadWithOptionalBlur(renderer, x, y + header.height, width, height - header.height, theme().backgroundColor.get());
        }

        super.render(renderer, mouseX, mouseY, delta);

        if (scissor) renderer.scissorEnd();

        if (expanded || animProgress > 0) {
            double thickness = theme.scale(theme().windowOutlineThickness.get());
            if (thickness > 0) {
                double contentY = y + header.height;
                double contentHeight = (height - header.height) * animProgress;
                Color outlineColor = theme().windowOutlineColor.get();

                if (animProgress > 0) renderOutline(renderer, x, contentY, width, contentHeight, thickness, outlineColor);
            }
        }

        return false;
    }

    private class WBaseHeader extends WHeader {
        public WBaseHeader(WWidget icon) {
            super(icon);
        }

        @Override
        public void init() {
            super.init();

            Cell<?> titleCell = getTitleLabelCell();
            if (titleCell == null) return;

            AlignmentX alignment = theme().categoryTitleAlignment.get();
            if (alignment == AlignmentX.Center) {
                titleCell.center();
            } else if (alignment == AlignmentX.Right) {
                titleCell.right().centerY();
            }
        }

        @Override
        protected void onCalculateWidgetPositions() {
            super.onCalculateWidgetPositions();

            if (theme().categoryTitleAlignment.get() == AlignmentX.Left) {
                Cell<?> titleCell = getTitleLabelCell();
                if (titleCell != null && titleCell.widget() instanceof WLabel label) {
                    label.x = titleCell.x + titleCell.padLeft();
                }
            }
        }

        private Cell<?> getTitleLabelCell() {
            for (Cell<?> cell : cells) {
                if (cell.widget() instanceof WLabel) return cell;
            }

            return null;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            renderQuadWithOptionalBlur(renderer, x, y, width, height, theme().accentColor.get());
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
