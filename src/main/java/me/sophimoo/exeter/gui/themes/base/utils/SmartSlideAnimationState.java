package me.sophimoo.exeter.gui.themes.base.utils;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import net.minecraft.util.math.MathHelper;

public final class SmartSlideAnimationState {
    private boolean wasHovered;
    private ModuleAnimationMode cachedSlideInDirection;
    private ModuleAnimationMode cachedSlideOutDirection;

    public ModuleAnimationMode resolveMode(ModuleAnimationMode animationMode, boolean hoveredNow, boolean shouldFadeIn,
                                           double mouseX, double mouseY,
                                           double x, double y, double width, double height,
                                           BaseGuiTheme theme, double currentProgress) {
        ModuleAnimationMode effectiveAnimationMode = animationMode;

        if (animationMode == ModuleAnimationMode.SMART_SLIDE) {
            if (hoveredNow && !wasHovered) {
                cachedSlideInDirection = calculateSmartSlideDirection(x, y, width, height, theme);
                cachedSlideOutDirection = null;
            }

            if (!hoveredNow && wasHovered) {
                cachedSlideOutDirection = calculateDirectionFromMouse(mouseX, mouseY, x, y, width, height);
                theme.updateLastHoveredPosition(x + width / 2, y + height / 2);
            }

            if (shouldFadeIn && cachedSlideInDirection != null) effectiveAnimationMode = cachedSlideInDirection;
            else if (!shouldFadeIn && cachedSlideOutDirection != null) effectiveAnimationMode = cachedSlideOutDirection;

            if (currentProgress <= 0 && !hoveredNow) {
                cachedSlideInDirection = null;
                cachedSlideOutDirection = null;
            }
        }

        wasHovered = hoveredNow;
        return effectiveAnimationMode;
    }

    public double stepProgress(double currentProgress, boolean shouldFadeIn, double delta, double fadeInSpeed, double fadeOutSpeed) {
        if (shouldFadeIn && fadeInSpeed == 0) return 1;
        if (!shouldFadeIn && fadeOutSpeed == 0) return 0;

        double progress = currentProgress + delta * (shouldFadeIn ? fadeInSpeed : fadeOutSpeed) * (shouldFadeIn ? 1 : -1);
        return MathHelper.clamp(progress, 0, 1);
    }

    private ModuleAnimationMode calculateSmartSlideDirection(double x, double y, double width, double height, BaseGuiTheme theme) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;

        if (!theme.hasValidLastHover()) return ModuleAnimationMode.SLIDE_LEFT;

        double deltaX = centerX - theme.getLastHoveredX();
        double deltaY = centerY - theme.getLastHoveredY();

        boolean isHorizontalDominant = Math.abs(deltaX) > Math.abs(deltaY);
        if (isHorizontalDominant) return deltaX > 0 ? ModuleAnimationMode.SLIDE_LEFT : ModuleAnimationMode.SLIDE_RIGHT;
        return deltaY > 0 ? ModuleAnimationMode.SLIDE_UP : ModuleAnimationMode.SLIDE_DOWN;
    }

    private ModuleAnimationMode calculateDirectionFromMouse(double mouseX, double mouseY,
                                                            double x, double y, double width, double height) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;

        double deltaX = mouseX - centerX;
        double deltaY = mouseY - centerY;

        boolean isHorizontalDominant = Math.abs(deltaX) > Math.abs(deltaY) * 12.0;
        if (isHorizontalDominant) return deltaX > 0 ? ModuleAnimationMode.SLIDE_RIGHT : ModuleAnimationMode.SLIDE_LEFT;
        return deltaY > 0 ? ModuleAnimationMode.SLIDE_DOWN : ModuleAnimationMode.SLIDE_UP;
    }
}
