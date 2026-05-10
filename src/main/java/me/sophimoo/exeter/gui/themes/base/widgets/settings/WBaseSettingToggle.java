package me.sophimoo.exeter.gui.themes.base.widgets.settings;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.AnimatedOverlayRenderer;
import me.sophimoo.exeter.gui.themes.base.utils.enums.GradientApplicationMode;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleAnimationMode;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleGradientDirection;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleIndicatorPosition;
import me.sophimoo.exeter.gui.themes.base.utils.SmartSlideAnimationState;
import me.sophimoo.exeter.gui.themes.base.utils.WidgetSizeDebug;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class WBaseSettingToggle extends WPressable implements BaseWidget {
    private final String title;
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private final boolean showIndicator;

    private double titleWidth;

    private final MarqueeState marquee = new MarqueeState();
    private final SmartSlideAnimationState smartSlide = new SmartSlideAnimationState();

    private double animationProgress;
    private double hoverOverlayProgress;

    public WBaseSettingToggle(BoolSetting setting) {
        this(setting.title, setting.description, setting::get, setting::set, true);
    }

    protected WBaseSettingToggle(String title, String tooltip, BooleanSupplier getter, Consumer<Boolean> setter, boolean showIndicator) {
        this.title = title;
        this.tooltip = tooltip;
        this.getter = getter;
        this.setter = setter;
        this.showIndicator = showIndicator;
        this.animationProgress = getter.getAsBoolean() ? 1.0 : 0.0;
        this.hoverOverlayProgress = 0.0;
    }

    @Override
    public double pad() {
        return theme().rowPadX();
    }

    @Override
    protected void onCalculateSize() {
        if (titleWidth == 0) titleWidth = theme().textWidth(title);

        double padX = theme().rowPadX();
        double padY = theme().rowPadY();
        width = padX + titleWidth + padX;
        height = resolveItemRowHeight(padY + theme().textHeight() + padY);
    }

    @Override
    protected void onPressed(int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) setter.accept(!getter.getAsBoolean());
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        boolean active = getter.getAsBoolean();
        boolean shouldFadeIn = active || mouseOver;

        logDebugInfo();
        ModuleAnimationMode effectiveAnimationMode = calculateAnimationMode(active, mouseOver, mouseX, mouseY, shouldFadeIn);
        updateAnimationProgress(active, mouseOver, delta, shouldFadeIn);

        ModuleGradientDirection gradientDir = theme().moduleGradientDirection.get();
        GradientApplicationMode applyMode = theme().gradientApplicationMode.get();

        renderBackgroundLayers(renderer, active, mouseOver, effectiveAnimationMode, gradientDir, applyMode);
        renderOutlineIfNeeded(renderer, mouseOver);
        renderTitle(renderer, active, mouseOver, delta);

        if (active && showIndicator) renderIndicator(renderer);
    }

    private void logDebugInfo() {
        WidgetSizeDebug.log(
            theme(),
            this,
            "SettingToggle",
            width,
            height,
            String.format(
                java.util.Locale.US,
                "padX=%.2f padY=%.2f globalPad=%.2f rowPadX=%.2f rowPadY=%.2f",
                theme().rowPadX(),
                theme().rowPadY(),
                theme().pad(),
                theme().rowPadX(),
                theme().rowPadY()
            )
        );
    }

    private ModuleAnimationMode calculateAnimationMode(boolean active, boolean mouseOver, double mouseX, double mouseY, boolean shouldFadeIn) {
        ModuleAnimationMode animationMode = theme().moduleAnimationMode.get();
        return smartSlide.resolveMode(
            animationMode,
            mouseOver,
            shouldFadeIn,
            mouseX,
            mouseY,
            x,
            y,
            width,
            height,
            theme(),
            animationProgress
        );
    }

    private void updateAnimationProgress(boolean active, boolean mouseOver, double delta, boolean shouldFadeIn) {
        double fadeInSpeed = theme().moduleSelectSpeed.get();
        double fadeOutSpeed = theme().moduleDeselectSpeed.get();
        animationProgress = smartSlide.stepProgress(animationProgress, shouldFadeIn, delta, fadeInSpeed, fadeOutSpeed);
        hoverOverlayProgress = smartSlide.stepProgress(hoverOverlayProgress, mouseOver, delta, fadeInSpeed, fadeOutSpeed);
    }

    private void renderBackgroundLayers(GuiRenderer renderer, boolean active, boolean mouseOver,
                                       ModuleAnimationMode effectiveAnimationMode,
                                       ModuleGradientDirection gradientDir, GradientApplicationMode applyMode) {
        Color inactiveColor = theme().itemBackgroundColor.get();
        boolean applyInactiveGradient = applyMode.shouldApply(false) && gradientDir != ModuleGradientDirection.NONE;
        Color inactiveGradient = theme().itemBackgroundGradientColor.get();
        Color overlayColor = active ? theme().itemActiveColor.get() : theme().itemHoveredBackgroundColor.get();
        Color overlayGradient = active ? theme().itemActiveGradientColor.get() : theme().itemHoveredBackgroundGradientColor.get();
        boolean applyOverlayGradient = applyMode.shouldApply(active);
        Color hoveredOverlayColor = theme().itemHoveredBackgroundColor.get();
        Color hoveredOverlayGradient = theme().itemHoveredBackgroundGradientColor.get();
        boolean applyHoveredOverlayGradient = applyMode.shouldApply(false);

        AnimatedOverlayRenderer.render(
            renderer,
            x,
            y,
            width,
            height,
            ModuleAnimationMode.FADE,
            1,
            inactiveColor,
            inactiveGradient,
            applyInactiveGradient ? gradientDir : ModuleGradientDirection.NONE
        );

        if (animationProgress > 0) {
            AnimatedOverlayRenderer.render(
                renderer,
                x,
                y,
                width,
                height,
                effectiveAnimationMode,
                animationProgress,
                overlayColor,
                overlayGradient,
                applyOverlayGradient ? gradientDir : ModuleGradientDirection.NONE
            );
        }

        if (active && hoverOverlayProgress > 0) {
            AnimatedOverlayRenderer.render(
                renderer,
                x,
                y,
                width,
                height,
                effectiveAnimationMode,
                hoverOverlayProgress,
                hoveredOverlayColor,
                hoveredOverlayGradient,
                applyHoveredOverlayGradient ? gradientDir : ModuleGradientDirection.NONE
            );
        }
    }

    private void renderOutlineIfNeeded(GuiRenderer renderer, boolean mouseOver) {
        double outlineThickness = theme().scale(theme().moduleOutlineThickness.get());
        Color outlineColor = theme().outlineColor.get(pressed, mouseOver);
        if (outlineThickness > 0 && outlineColor != null) {
            renderOutline(renderer, x, y, width, height, outlineThickness, outlineColor);
        }
    }

    private void renderTitle(GuiRenderer renderer, boolean active, boolean mouseOver, double delta) {
        double padX = theme().rowPadX();
        Color textColor = active ? theme().moduleTextActiveColor.get() : (mouseOver ? theme().moduleTextHoveredColor.get() : theme().moduleTextInactiveColor.get());

        double titleAreaX = x + padX;
        double titleAreaW = Math.max(0, width - padX * 2);

        double textHeight = theme().textHeight();
        double textY = y + (height - textHeight) / 2;

        renderTextWithMarquee(renderer, marquee, title, titleAreaX, y, titleAreaW, height, textY, titleWidth,
            mouseOver, delta, true, titleAreaX, textColor);
    }

    private void renderIndicator(GuiRenderer renderer) {
        ModuleIndicatorPosition position = theme().moduleIndicatorPosition.get();
        if (position == ModuleIndicatorPosition.NONE) return;

        double thickness = theme().scale(theme().moduleIndicatorThickness.get());
        if (thickness <= 0) return;

        Color accentColor = theme().accentColor.get();
        double ix = x, iy = y, iw = width, ih = height;

        switch (position) {
            case LEFT -> iw = thickness;
            case RIGHT -> {
                ix = x + width - thickness;
                iw = thickness;
            }
            case TOP -> ih = thickness;
            case BOTTOM -> {
                iy = y + height - thickness;
                ih = thickness;
            }
        }

        renderer.quad(ix, iy, iw, ih, accentColor);
    }
}
