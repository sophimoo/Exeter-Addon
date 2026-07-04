package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleSettingsIndicator;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.AlignmentY;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.Click;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WBaseSection extends WSection implements BaseWidget {
    private double activeProgress;
    private double fullHeight;

    public WBaseSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
        activeProgress = expanded ? 1.0 : 0.0;
    }

    @Override
    public void setExpanded(boolean expanded) {
        if (this.expanded != expanded) super.setExpanded(expanded);
    }

    @Override
    protected void onCalculateSize() {
        width = 0;
        height = 0;

        for (int i = 0; i < cells.size(); i++) {
            var cell = cells.get(i);
            if (i > 0) height += spacing();

            width = Math.max(width, cell.padLeft() + cell.widget().width + cell.padRight());
            height += cell.padTop() + cell.widget().height + cell.padBottom();
        }

        if (cells.isEmpty()) return;

        fullHeight = height;
        double headerHeight = cells.get(0).widget().height;
        if (fullHeight > headerHeight) height = Math.round((fullHeight - headerHeight) * dropdownHeightProgress(animProgress, expanded) + headerHeight);
    }

    @Override
    public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!visible) return true;

        if (cells.isEmpty()) return false;

        double headerHeight = cells.get(0).widget().height;
        double contentHeight = fullHeight - headerHeight;
        double previousAnimProgress = animProgress;

        animProgress = stepProgress(animProgress, expanded, delta);
        if (previousAnimProgress != animProgress) invalidate();

        double scissorHeight = contentHeight * dropdownHeightProgress(animProgress, expanded) + headerHeight;
        boolean scissor = (animProgress != 0 && animProgress != 1) || (expanded && animProgress != 1);
        if (scissor) renderer.scissorStart(x, y, width, scissorHeight);

        for (var cell : cells) {
            WWidget widget = cell.widget();
            renderWidget(widget, renderer, mouseX, mouseY, delta);
        }

        if (scissor) renderer.scissorEnd();

        return false;
    }

    @Override
    protected WHeader createHeader() {
        return new WBaseHeader(title);
    }

    protected class WBaseHeader extends WHeader {
        private double titleWidth;
        private double hoverProgress;
        private double exeterIconRotation;
        private double meteorIconRotation = Double.NaN;
        private final MarqueeState marquee = new MarqueeState();

        private ModuleSettingsIndicator resolveSeparatorIndicatorStyle() {
            ModuleSettingsIndicator style = theme().moduleSettingsIndicator.get();
            if (style == ModuleSettingsIndicator.NONE) return style;
            return theme().indicatorsOnSeparators.get() ? style : ModuleSettingsIndicator.NONE;
        }

        public WBaseHeader(String title) {
            super(title);
        }

        @Override
        public void init() {
            if (headerWidget != null) {
                add(headerWidget).expandCellX().right().padRight(theme().rowPadX() + indicatorRightInset());
            }
        }

        @Override
        public boolean onMouseClicked(Click click, boolean doubled) {
            if (mouseOver) {
                if (click.button() == GLFW_MOUSE_BUTTON_LEFT || click.button() == GLFW_MOUSE_BUTTON_RIGHT) {
                    onClick();
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onCalculateSize() {
            super.onCalculateSize();

            ModuleSettingsIndicator indicatorStyle = resolveSeparatorIndicatorStyle();
            double pad = pad();
            double iconWidth = indicatorWidth(indicatorStyle);
            double iconGap = indicatorGap(indicatorStyle);

            if (titleWidth == 0) titleWidth = theme().textWidth(title);

            width = Math.max(width, pad + titleWidth + iconGap + iconWidth + pad);
            height = resolveSeparatorRowHeight(pad + theme().textHeight() + pad);
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double pad = pad();
            String collapsedIndicator = theme().moduleCollapsedIndicator.get();
            String expandedIndicator = theme().moduleExpandedIndicator.get();
            if (collapsedIndicator == null) collapsedIndicator = "";
            if (expandedIndicator == null) expandedIndicator = "";

            ModuleSettingsIndicator indicatorStyle = resolveSeparatorIndicatorStyle();
            double iconWidth = indicatorWidth(indicatorStyle);
            double iconGap = indicatorGap(indicatorStyle);
            RowAnimationState animationState = animateRow(delta, mouseOver, mouseOver, false, hoverProgress, 0);
            hoverProgress = animationState.primaryProgress();
            activeProgress = stepProgress(activeProgress, WBaseSection.this.expanded, delta);
            RowSurfaceStyle surfaceStyle = separatorRowSurfaceStyle(activeProgress > 0, mouseOver);
            renderRowSurface(renderer, x, y, width, height, animationState.effectiveAnimationMode(), activeProgress, localHoverSurfaceProgress(hoverProgress), surfaceStyle);
            renderInterpolationHover(renderer, x, y, width, height, mouseOver, delta, surfaceStyle);

            double textAreaX = x + pad;
            double textAreaWidth = Math.max(0, width - pad * 2 - iconWidth - iconGap);
            if (titleWidth == 0) titleWidth = theme().textWidth(title);
            RowTextLayout layout = resolveRowTextLayout(textAreaX, y, textAreaWidth, height, titleWidth, theme().moduleAlignment.get(), AlignmentY.Center);

            Color textColor = resolveSeparatorTextColor(activeProgress, hoverProgress);
            renderRowTitle(renderer, marquee, title, titleWidth, delta, false, false, textColor, hoverProgress, layout);

            if (indicatorStyle != ModuleSettingsIndicator.NONE) {
                double iconX = x + width - pad - iconWidth;

                switch (indicatorStyle) {
                    case EXETER -> {
                        exeterIconRotation = stepExeterIndicatorRotation(exeterIconRotation, WBaseSection.this.expanded, delta);
                        renderExeterIndicator(renderer, iconX, y, iconWidth, height, pad, exeterIconRotation, textColor);
                    }
                    case METEOR -> {
                        meteorIconRotation = stepMeteorIndicatorRotation(meteorIconRotation, WBaseSection.this.expanded, delta);
                        renderMeteorIndicator(renderer, iconX, y, iconWidth, height, pad, meteorIconRotation, textColor);
                    }
                    default -> {
                        String dropdownIcon = activeProgress >= 0.5 ? expandedIndicator : collapsedIndicator;
                        renderText(renderer, dropdownIcon, iconX, layout.textY(), textColor);
                    }
                }
            }
        }

        @Override
        protected void onCalculateWidgetPositions() {
            super.onCalculateWidgetPositions();

            if (!(headerWidget instanceof WCheckbox checkbox) || cells.isEmpty()) return;

            double checkboxSize = Math.max(0, height);
            checkbox.width = checkboxSize;
            checkbox.height = checkboxSize;
            cells.get(0).alignWidget();
        }

        private double indicatorWidth(ModuleSettingsIndicator style) {
            return switch (style) {
                case NONE -> 0;
                case EXETER -> theme().textHeight() * EXETER_ICON_SCALE;
                case METEOR -> theme().textHeight();
                case DROPDOWN -> {
                    String collapsedIndicator = theme().moduleCollapsedIndicator.get();
                    String expandedIndicator = theme().moduleExpandedIndicator.get();
                    if (collapsedIndicator == null) collapsedIndicator = "";
                    if (expandedIndicator == null) expandedIndicator = "";
                    yield Math.max(theme().textWidth(collapsedIndicator), theme().textWidth(expandedIndicator));
                }
            };
        }

        private double indicatorGap(ModuleSettingsIndicator style) {
            return style != ModuleSettingsIndicator.NONE ? pad() : 0;
        }

        private double indicatorRightInset() {
            ModuleSettingsIndicator style = resolveSeparatorIndicatorStyle();
            return indicatorGap(style) + indicatorWidth(style) + indicatorGap(style);
        }
    }
}
