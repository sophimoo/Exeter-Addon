package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.AnimatedOverlayRenderer;
import me.sophimoo.exeter.gui.themes.base.GradientApplicationMode;
import me.sophimoo.exeter.gui.themes.base.ModuleAnimationMode;
import me.sophimoo.exeter.gui.themes.base.ModuleGradientDirection;
import me.sophimoo.exeter.gui.themes.base.SmartSlideAnimationState;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.Click;
import net.minecraft.util.math.MathHelper;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WBaseSection extends WSection implements BaseWidget {
    public WBaseSection(String title, boolean expanded, WWidget headerWidget) {
        super(title, expanded, headerWidget);
    }

    @Override
    protected WHeader createHeader() {
        return new WBaseHeader(title);
    }

    protected class WBaseHeader extends WHeader {
        private double titleWidth;
        private double hoverProgress;
        private final SmartSlideAnimationState smartSlide = new SmartSlideAnimationState();

        public WBaseHeader(String title) {
            super(title);
        }

        @Override
        public void init() {
            if (headerWidget != null) {
                add(headerWidget).expandCellX().right().padRight(rowPadX() + indicatorRightInset());
            }
        }

        @Override
        public boolean onMouseClicked(Click click, boolean doubled) {
            if (mouseOver && !doubled) {
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

            boolean showIndicator = theme().showModuleIndicator.get();
            double iconWidth = indicatorWidth();
            double iconGap = indicatorGap();
            double textHeight = theme().textHeight();

            ModuleGradientDirection gradientDirection = theme().moduleGradientDirection.get();
            GradientApplicationMode applyMode = theme().gradientApplicationMode.get();
            double thickness = theme().scale(theme().moduleOutlineThickness.get());

            // Determine render direction for gradients
            ModuleGradientDirection renderDirection = (gradientDirection != ModuleGradientDirection.None) ? gradientDirection : ModuleGradientDirection.None;

            // Base layer - always render separator + separator-gradient (like inactive module base)
            boolean renderBaseGradient = applyMode.appliesToInactive() && gradientDirection != ModuleGradientDirection.None;
            AnimatedOverlayRenderer.render(
                renderer,
                x,
                y,
                width,
                height,
                ModuleAnimationMode.FADE,
                1,
                theme().separatorColor.get(),
                theme().separatorGradientColor.get(),
                renderBaseGradient ? renderDirection : ModuleGradientDirection.None
            );

            // Hover overlay layer - animated (like hovered/active module overlay)
            ModuleAnimationMode animationMode = theme().moduleAnimationMode.get();
            ModuleAnimationMode effectiveAnimationMode = smartSlide.resolveMode(
                animationMode,
                mouseOver,
                mouseOver,
                mouseX,
                mouseY,
                x,
                y,
                width,
                height,
                theme(),
                hoverProgress
            );

            hoverProgress = smartSlide.stepProgress(
                hoverProgress,
                mouseOver,
                delta,
                theme().moduleSelectSpeed.get(),
                theme().moduleDeselectSpeed.get()
            );
            hoverProgress = MathHelper.clamp(hoverProgress, 0, 1);

            if (hoverProgress > 0) {
                boolean renderHoverGradient = applyMode.appliesToInactive() && gradientDirection != ModuleGradientDirection.None;
                AnimatedOverlayRenderer.render(
                    renderer,
                    x,
                    y,
                    width,
                    height,
                    effectiveAnimationMode,
                    hoverProgress,
                    theme().separatorHoveredColor.get(),
                    theme().separatorHoveredGradientColor.get(),
                    renderHoverGradient ? renderDirection : ModuleGradientDirection.None
                );
            }

            if (thickness > 0) renderOutline(renderer, x, y, width, height, thickness, theme().outlineColor.get(false, mouseOver));

            double textY = y + pad + (height - pad * 2 - textHeight) / 2;
            double textAreaX = x + pad;
            double textAreaWidth = Math.max(0, width - pad * 2 - iconWidth - iconGap);
            if (titleWidth == 0) titleWidth = theme().textWidth(title);

            double textX = textAreaX;
            if (theme().moduleAlignment.get() == AlignmentX.Center) {
                textX += textAreaWidth / 2 - titleWidth / 2;
            } else if (theme().moduleAlignment.get() == AlignmentX.Right) {
                textX += textAreaWidth - titleWidth;
            }

            Color textColor = theme().separatorText.get();
            renderText(renderer, title, textX, textY, textColor);

            if (showIndicator) {
                String dropdownIcon = animProgress >= 0.5 ? expandedIndicator : collapsedIndicator;
                double iconX = x + width - pad - iconWidth;
                renderText(renderer, dropdownIcon, iconX, textY, textColor);
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
            if (!theme().showModuleIndicator.get()) return 0;

            String collapsedIndicator = theme().moduleCollapsedIndicator.get();
            String expandedIndicator = theme().moduleExpandedIndicator.get();
            if (collapsedIndicator == null) collapsedIndicator = "";
            if (expandedIndicator == null) expandedIndicator = "";

            return Math.max(theme().textWidth(collapsedIndicator), theme().textWidth(expandedIndicator));
        }

        private double indicatorGap() {
            return theme().showModuleIndicator.get() ? pad() : 0;
        }

        private double indicatorRightInset() {
            return indicatorGap() + indicatorWidth() + indicatorGap();
        }
    }
}
