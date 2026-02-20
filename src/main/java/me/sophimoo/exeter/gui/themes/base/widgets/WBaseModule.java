package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.ModuleAnimationMode;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WBaseModule extends WPressable implements BaseWidget {
    private final Module module;
    private final String title;

    private double titleWidth;

    private double animationProgress1;

    private double animationProgress2;

    // Smart slide tracking
    private boolean wasHovered = false;
    private ModuleAnimationMode cachedSlideInDirection = null;
    private ModuleAnimationMode cachedSlideOutDirection = null;

    public WBaseModule(Module module, String title) {
        this.module = module;
        this.title = title;
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

    /**
     * Calculates the smart slide direction based on the position of the last hovered module.
     * Returns the opposite direction (if last was to the left, slide from right, etc.)
     */
    private ModuleAnimationMode calculateSmartSlideDirection() {
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        if (!theme().hasValidLastHover()) {
            // No valid last position, default to slide left
            return ModuleAnimationMode.SLIDE_LEFT;
        }
        
        double lastX = theme().getLastHoveredX();
        double lastY = theme().getLastHoveredY();
        
        // Calculate deltas
        double deltaX = centerX - lastX;
        double deltaY = centerY - lastY;
        
        // Minimum threshold to consider vertical movement (prevents jitter from causing horizontal slides)
        double minVerticalThreshold = height;
        
        // Only trigger horizontal if it's truly dominant (4x+ stronger than vertical)
        boolean isVerticalSignificant = Math.abs(deltaY) >= minVerticalThreshold;
        boolean isHorizontalDominant = Math.abs(deltaX) > Math.abs(deltaY);
        
        // Only use horizontal if it's dominant, otherwise default to vertical
        if (isHorizontalDominant) {
            // Horizontal movement - animate from opposite direction
            // If last was to the right (deltaX < 0), slide from right
            // If last was to the left (deltaX > 0), slide from left
            return deltaX > 0 ? ModuleAnimationMode.SLIDE_LEFT : ModuleAnimationMode.SLIDE_RIGHT;
        } else {
            // Vertical movement - animate from opposite direction
            // If last was below (deltaY < 0), slide from bottom
            // If last was above (deltaY > 0), slide from top
            return deltaY > 0 ? ModuleAnimationMode.SLIDE_UP : ModuleAnimationMode.SLIDE_DOWN;
        }
    }

    /**
     * Calculates slide-out direction based on where the mouse is heading.
     * This determines which direction to slide out towards.
     */
    private ModuleAnimationMode calculateDirectionFromMouse(double mouseX, double mouseY) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        
        // Calculate deltas (mouse position relative to module center)
        double deltaX = mouseX - centerX;
        double deltaY = mouseY - centerY;
        
        // Minimum threshold to consider vertical movement
        double minVerticalThreshold = height * 0.3;
        
        // Only trigger horizontal if it's truly dominant (12x+ stronger than vertical)
        boolean isVerticalSignificant = Math.abs(deltaY) >= minVerticalThreshold;
        boolean isHorizontalDominant = Math.abs(deltaX) > Math.abs(deltaY) * 12.0;
        
        // Only use horizontal if it's dominant, otherwise default to vertical
        if (isHorizontalDominant) {
            // Mouse is heading horizontally - slide out in that direction
            return deltaX > 0 ? ModuleAnimationMode.SLIDE_RIGHT : ModuleAnimationMode.SLIDE_LEFT;
        } else {
            // Mouse is heading vertically - slide out in that direction
            return deltaY > 0 ? ModuleAnimationMode.SLIDE_DOWN : ModuleAnimationMode.SLIDE_UP;
        }
    }

    /**
     * Returns the inverse/opposite direction for slide-out animations.
     */
    private ModuleAnimationMode getInverseDirection(ModuleAnimationMode direction) {
        return switch (direction) {
            case SLIDE_LEFT -> ModuleAnimationMode.SLIDE_RIGHT;
            case SLIDE_RIGHT -> ModuleAnimationMode.SLIDE_LEFT;
            case SLIDE_UP -> ModuleAnimationMode.SLIDE_DOWN;
            case SLIDE_DOWN -> ModuleAnimationMode.SLIDE_UP;
            default -> direction;
        };
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
        else if (button == GLFW_MOUSE_BUTTON_RIGHT) mc.setScreen(theme().moduleScreen(module));
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();

        boolean isActive = module.isActive();
        boolean shouldFadeIn = isActive || mouseOver;
        ModuleAnimationMode animationMode = theme().moduleAnimationMode.get();

        // Smart slide direction calculation
        ModuleAnimationMode effectiveAnimationMode = animationMode;
        if (animationMode == ModuleAnimationMode.SMART_SLIDE) {
            // Check if we just started hovering
            if (mouseOver && !wasHovered) {
                // Calculate slide-in direction based on last hovered position
                cachedSlideInDirection = calculateSmartSlideDirection();
                cachedSlideOutDirection = null;
            }

            // Check if we just stopped hovering
            if (!mouseOver && wasHovered) {
                // Calculate slide-out direction based on current mouse position (where user is heading)
                cachedSlideOutDirection = calculateDirectionFromMouse(mouseX, mouseY);
                // Update last hovered position for the next module
                theme().updateLastHoveredPosition(x + width / 2, y + height / 2);
            }

            // Use appropriate cached direction based on whether fading in or out
            if (shouldFadeIn && cachedSlideInDirection != null) {
                effectiveAnimationMode = cachedSlideInDirection;
            } else if (!shouldFadeIn && cachedSlideOutDirection != null) {
                effectiveAnimationMode = cachedSlideOutDirection;
            }

            // Clear cache when animation fully completes
            if (animationProgress1 <= 0 && !mouseOver) {
                cachedSlideInDirection = null;
                cachedSlideOutDirection = null;
            }
        }

        // Track hover state for next frame
        wasHovered = mouseOver;

        double fadeInSpeed = theme().moduleFadeInSpeed.get();
        double fadeOutSpeed = theme().moduleFadeOutSpeed.get();

        animationProgress1 += delta * (shouldFadeIn ? fadeInSpeed : fadeOutSpeed) * (shouldFadeIn ? 1 : -1);
        animationProgress1 = MathHelper.clamp(animationProgress1, 0, 1);

        animationProgress2 += delta * (isActive ? 1 : -1);
        animationProgress2 = MathHelper.clamp(animationProgress2, 0, 1);

        Color bgColor = isActive ? theme().moduleActiveBackground.get() : theme().moduleHoveredBackground.get();

        // Always draw inactive background first
        renderer.quad(x, y, width, height, theme().moduleInactiveBackground.get());

        // Apply animation based on selected mode
        if (animationProgress1 > 0) {
            switch (effectiveAnimationMode) {
                case FADE -> {
                    // Fade effect - overlays with faded color
                    Color fadedColor = new Color(bgColor.r, bgColor.g, bgColor.b, (int) (bgColor.a * animationProgress1));
                    renderer.quad(x, y, width, height, fadedColor);
                }
                case SLIDE_LEFT -> {
                    // Slide from left - animates width from left
                    renderer.quad(x, y, width * animationProgress1, height, bgColor);
                }
                case SLIDE_RIGHT -> {
                    // Slide from right - animates width from right
                    double slideWidth = width * animationProgress1;
                    renderer.quad(x + width - slideWidth, y, slideWidth, height, bgColor);
                }
                case SLIDE_UP -> {
                    // Slide from top - animates height from top
                    renderer.quad(x, y, width, height * animationProgress1, bgColor);
                }
                case SLIDE_DOWN -> {
                    // Slide from bottom - animates height from bottom
                    double slideHeight = height * animationProgress1;
                    renderer.quad(x, y + height - slideHeight, width, slideHeight, bgColor);
                }
                default -> {
                    // Default to slide left for unknown modes
                    renderer.quad(x, y, width * animationProgress1, height, bgColor);
                }
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

        double x = this.x + pad;
        double w = width - pad * 2;

        if (theme().moduleAlignment.get() == AlignmentX.Center) {
            x += w / 2 - titleWidth / 2;
        }
        else if (theme().moduleAlignment.get() == AlignmentX.Right) {
            x += w - titleWidth;
        }

        renderer.text(title, x, y + pad, theme().textColor.get(), false);
    }
}
