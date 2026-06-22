package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
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
    public WBaseSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
    }

    @Override
    public void setExpanded(boolean expanded) {
        if (this.expanded == expanded) return;
        // Snap animation if mid-flight so repeated toggles feel instant.
        if (animProgress > 0 && animProgress < 1) {
            animProgress = this.expanded ? 1 : 0;
        }
        super.setExpanded(expanded);
    }

    @Override
    protected WHeader createHeader() {
        return new WBaseHeader(title);
    }

    protected class WBaseHeader extends WHeader {
        private double titleWidth;
        private double hoverProgress;
        private final MarqueeState marquee = new MarqueeState();

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

            double pad = pad();
            double iconWidth = indicatorWidth();
            double iconGap = indicatorGap();

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

            boolean showIndicator = theme().dropdownIndicator.get();
            double iconWidth = indicatorWidth();
            double iconGap = indicatorGap();
            RowAnimationState animationState = animateRow(delta, mouseOver, mouseOver, false, hoverProgress, 0);
            hoverProgress = animationState.primaryProgress();
            RowSurfaceStyle surfaceStyle = separatorRowSurfaceStyle(animProgress > 0, mouseOver);
            renderRowSurface(renderer, x, y, width, height, animationState.effectiveAnimationMode(), animProgress, localHoverSurfaceProgress(hoverProgress), surfaceStyle);
            renderInterpolationHover(renderer, x, y, width, height, mouseOver, delta, surfaceStyle);

            double textAreaX = x + pad;
            double textAreaWidth = Math.max(0, width - pad * 2 - iconWidth - iconGap);
            if (titleWidth == 0) titleWidth = theme().textWidth(title);
            RowTextLayout layout = resolveRowTextLayout(textAreaX, y, textAreaWidth, height, titleWidth, theme().moduleAlignment.get(), AlignmentY.Center);

            Color textColor = resolveSeparatorTextColor(hoverProgress);
            renderRowTitle(renderer, marquee, title, titleWidth, delta, false, false, textColor, hoverProgress, layout);

            if (showIndicator) {
                String dropdownIcon = animProgress >= 0.5 ? expandedIndicator : collapsedIndicator;
                double iconX = x + width - pad - iconWidth;
                renderText(renderer, dropdownIcon, iconX, layout.textY(), textColor);
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

        private double indicatorWidth() {
            if (!theme().dropdownIndicator.get()) return 0;

            String collapsedIndicator = theme().moduleCollapsedIndicator.get();
            String expandedIndicator = theme().moduleExpandedIndicator.get();
            if (collapsedIndicator == null) collapsedIndicator = "";
            if (expandedIndicator == null) expandedIndicator = "";

            return Math.max(theme().textWidth(collapsedIndicator), theme().textWidth(expandedIndicator));
        }

        private double indicatorGap() {
            return theme().dropdownIndicator.get() ? pad() : 0;
        }

        private double indicatorRightInset() {
            return indicatorGap() + indicatorWidth() + indicatorGap();
        }
    }
}
