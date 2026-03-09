package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.ModuleAnimationMode;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WBaseModule extends WVerticalList implements BaseWidget {
    private final Module module;
    private final String title;

    private boolean settingsExpanded = false;
    private WVerticalList settingsContainer = null;

    public WBaseModule(Module module, String title) {
        this.module = module;
        this.title = title;
        this.spacing = 0;
    }

    @Override
    public void init() {
        add(new WModuleButton()).expandX();
    }

    private void toggleSettings() {
        settingsExpanded = !settingsExpanded;

        if (settingsExpanded) {
            settingsContainer = theme.verticalList();
            settingsContainer.add(theme.settings(module.settings)).expandX();
            add(settingsContainer).expandX();
        } else {
            if (settingsContainer != null) {
                remove(cells.get(cells.size() - 1));
                settingsContainer = null;
            }
        }
    }

    public void tickSettings() {
        if (settingsExpanded && settingsContainer != null) {
            module.settings.tick(settingsContainer, theme);
        }
    }

    // Inner pressable widget that handles the module row rendering and click events
    private class WModuleButton extends WPressable implements BaseWidget {
        private double titleWidth;

        private double animationProgress1;
        private double animationProgress2;

        private boolean wasHovered = false;
        private ModuleAnimationMode cachedSlideInDirection = null;
        private ModuleAnimationMode cachedSlideOutDirection = null;

        public WModuleButton() {
            this.tooltip = module.description;

            if (module.isActive()) {
                animationProgress1 = 1;
                animationProgress2 = 1;
            } else {
                animationProgress1 = 0;
                animationProgress2 = 0;
            }
        }

        @Override
        public double pad() {
            return theme().scale(4);
        }

        private ModuleAnimationMode calculateSmartSlideDirection() {
            double centerX = x + width / 2;
            double centerY = y + height / 2;

            if (!theme().hasValidLastHover()) {
                return ModuleAnimationMode.SLIDE_LEFT;
            }

            double lastX = theme().getLastHoveredX();
            double lastY = theme().getLastHoveredY();

            double deltaX = centerX - lastX;
            double deltaY = centerY - lastY;

            double minVerticalThreshold = height;

            boolean isVerticalSignificant = Math.abs(deltaY) >= minVerticalThreshold;
            boolean isHorizontalDominant = Math.abs(deltaX) > Math.abs(deltaY);

            if (isHorizontalDominant) {
                return deltaX > 0 ? ModuleAnimationMode.SLIDE_LEFT : ModuleAnimationMode.SLIDE_RIGHT;
            } else {
                return deltaY > 0 ? ModuleAnimationMode.SLIDE_UP : ModuleAnimationMode.SLIDE_DOWN;
            }
        }

        private ModuleAnimationMode calculateDirectionFromMouse(double mouseX, double mouseY) {
            double centerX = x + width / 2;
            double centerY = y + height / 2;

            double deltaX = mouseX - centerX;
            double deltaY = mouseY - centerY;

            boolean isHorizontalDominant = Math.abs(deltaX) > Math.abs(deltaY) * 12.0;

            if (isHorizontalDominant) {
                return deltaX > 0 ? ModuleAnimationMode.SLIDE_RIGHT : ModuleAnimationMode.SLIDE_LEFT;
            } else {
                return deltaY > 0 ? ModuleAnimationMode.SLIDE_DOWN : ModuleAnimationMode.SLIDE_UP;
            }
        }

        @Override
        protected void onCalculateSize() {
            double pad = pad();

            if (titleWidth == 0) titleWidth = theme().textWidth(title);

            width = pad + titleWidth + pad;
            height = pad + theme().textHeight() + pad;
        }

        @Override
        protected void onPressed(int button) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) module.toggle();
            else if (button == GLFW_MOUSE_BUTTON_RIGHT) toggleSettings();
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double pad = pad();

            boolean isActive = module.isActive();
            boolean shouldFadeIn = isActive || mouseOver;
            ModuleAnimationMode animationMode = theme().moduleAnimationMode.get();

            ModuleAnimationMode effectiveAnimationMode = animationMode;
            if (animationMode == ModuleAnimationMode.SMART_SLIDE) {
                if (mouseOver && !wasHovered) {
                    cachedSlideInDirection = calculateSmartSlideDirection();
                    cachedSlideOutDirection = null;
                }

                if (!mouseOver && wasHovered) {
                    cachedSlideOutDirection = calculateDirectionFromMouse(mouseX, mouseY);
                    theme().updateLastHoveredPosition(x + width / 2, y + height / 2);
                }

                if (shouldFadeIn && cachedSlideInDirection != null) {
                    effectiveAnimationMode = cachedSlideInDirection;
                } else if (!shouldFadeIn && cachedSlideOutDirection != null) {
                    effectiveAnimationMode = cachedSlideOutDirection;
                }

                if (animationProgress1 <= 0 && !mouseOver) {
                    cachedSlideInDirection = null;
                    cachedSlideOutDirection = null;
                }
            }

            wasHovered = mouseOver;

            double fadeInSpeed = theme().moduleFadeInSpeed.get();
            double fadeOutSpeed = theme().moduleFadeOutSpeed.get();

            if (shouldFadeIn && fadeInSpeed == 0) {
                animationProgress1 = 1;
            } else if (!shouldFadeIn && fadeOutSpeed == 0) {
                animationProgress1 = 0;
            } else {
                animationProgress1 += delta * (shouldFadeIn ? fadeInSpeed : fadeOutSpeed) * (shouldFadeIn ? 1 : -1);
                animationProgress1 = MathHelper.clamp(animationProgress1, 0, 1);
            }

            animationProgress2 += delta * (isActive ? 1 : -1);
            animationProgress2 = MathHelper.clamp(animationProgress2, 0, 1);

            Color bgColor = isActive ? theme().moduleActiveBackground.get() : theme().moduleHoveredBackground.get();

            renderer.quad(x, y, width, height, theme().moduleInactiveBackground.get());

            if (animationProgress1 > 0) {
                switch (effectiveAnimationMode) {
                    case FADE -> {
                        Color fadedColor = new Color(bgColor.r, bgColor.g, bgColor.b, (int) (bgColor.a * animationProgress1));
                        renderer.quad(x, y, width, height, fadedColor);
                    }
                    case SLIDE_LEFT -> renderer.quad(x, y, width * animationProgress1, height, bgColor);
                    case SLIDE_RIGHT -> {
                        double slideWidth = width * animationProgress1;
                        renderer.quad(x + width - slideWidth, y, slideWidth, height, bgColor);
                    }
                    case SLIDE_UP -> renderer.quad(x, y, width, height * animationProgress1, bgColor);
                    case SLIDE_DOWN -> {
                        double slideHeight = height * animationProgress1;
                        renderer.quad(x, y + height - slideHeight, width, slideHeight, bgColor);
                    }
                    default -> renderer.quad(x, y, width * animationProgress1, height, bgColor);
                }
            }

            if (animationProgress2 > 0) {
                renderer.quad(x, y + height * (1 - animationProgress2), theme().scale(2), height * animationProgress2, theme().accentColor.get());
            }

            double thickness = theme().scale(theme().moduleOutlineThickness.get());
            if (thickness > 0) {
                Color outlineColor = theme().outlineColor.get(pressed, mouseOver);
                renderer.quad(this.x, this.y, width, thickness, outlineColor);
                renderer.quad(this.x, this.y + height - thickness, width, thickness, outlineColor);
                renderer.quad(this.x, this.y + thickness, thickness, height - 2 * thickness, outlineColor);
                renderer.quad(this.x + width - thickness, this.y + thickness, thickness, height - 2 * thickness, outlineColor);
            }

            double tx = this.x + pad;
            double w = width - pad * 2;

            if (theme().moduleAlignment.get() == AlignmentX.Center) {
                tx += w / 2 - titleWidth / 2;
            } else if (theme().moduleAlignment.get() == AlignmentX.Right) {
                tx += w - titleWidth;
            }

            renderer.text(title, tx, y + pad, theme().textColor.get(), false);
        }
    }
}
