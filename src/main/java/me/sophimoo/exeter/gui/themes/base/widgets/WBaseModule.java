package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.renderer.GradientRenderer;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.GradientApplicationMode;
import me.sophimoo.exeter.gui.themes.base.ModuleAnimationMode;
import me.sophimoo.exeter.gui.themes.base.ModuleGradientDirection;
import me.sophimoo.exeter.gui.themes.base.AlignmentY;
import me.sophimoo.exeter.gui.themes.base.ModuleIndicatorPosition;
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
    private static final double MARQUEE_SPEED = 35;
    private static final double MARQUEE_EDGE_PAUSE = 0.4;

    private final Module module;
    private final String title;

    private double titleWidth;

    private double animationProgress;
    private double indicatorProgress;
    private double marqueeOffset;
    private int marqueeDirection = 1;
    private double marqueePause;

    // Smart slide tracking
    private boolean wasHovered = false;
    private ModuleAnimationMode cachedSlideInDirection = null;
    private ModuleAnimationMode cachedSlideOutDirection = null;

    public WBaseModule(Module module, String title) {
        this.module = module;
        this.title = title;
        this.tooltip = module.description;

        if (module.isActive()) {
            animationProgress = 1;
            indicatorProgress = 1;
        } else {
            animationProgress = 0;
            indicatorProgress = 0;
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
        
        // Only trigger horizontal if it's truly dominant (4x+ stronger than vertical)
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
        
        // Only trigger horizontal if it's truly dominant (12x+ stronger than vertical)
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

    @Override
    protected void onCalculateSize() {
        double pad = pad();

        if (titleWidth == 0) titleWidth = theme().textWidth(title);

        if (theme().fixedCategorySize.get()) width = pad + pad;
        else width = pad + titleWidth + pad;

        // Apply custom module height if set, otherwise use default
        double customHeight = theme().moduleHeight.get();
        if (customHeight > 0) {
            height = theme().scale(customHeight);
        } else {
            height = pad + theme().textHeight() + pad;
        }
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
            if (animationProgress <= 0 && !mouseOver) {
                cachedSlideInDirection = null;
                cachedSlideOutDirection = null;
            }
        }

        // Track hover state for next frame
        wasHovered = mouseOver;

        double fadeInSpeed = theme().moduleSelectSpeed.get();
        double fadeOutSpeed = theme().moduleDeselectSpeed.get();

        if (shouldFadeIn && fadeInSpeed == 0) {
            animationProgress = 1;
        } else if (!shouldFadeIn && fadeOutSpeed == 0) {
            animationProgress = 0;
        } else {
            animationProgress += delta * (shouldFadeIn ? fadeInSpeed : fadeOutSpeed) * (shouldFadeIn ? 1 : -1);
            animationProgress = MathHelper.clamp(animationProgress, 0, 1);
        }

        // Update indicator animation (separate from main animation)
        indicatorProgress += delta * (isActive ? fadeInSpeed : fadeOutSpeed) * (isActive ? 1 : -1);
        indicatorProgress = MathHelper.clamp(indicatorProgress, 0, 1);

        // Get gradient direction setting
        ModuleGradientDirection gradientDir = theme().moduleGradientDirection.get();
        Color activeGradientColor = theme().moduleActiveGradientColor.get();
        Color inactiveGradientColor = theme().moduleInactiveGradientColor.get();
        GradientApplicationMode applyMode = theme().gradientApplicationMode.get();

        // Always draw inactive background first
        renderer.quad(x, y, width, height, theme().moduleInactiveColor.get());

        // Render inactive module gradient (when mode is INACTIVE or BOTH)
        if (!isActive && applyMode.appliesToInactive() && gradientDir != ModuleGradientDirection.None) {
            renderAnimation(renderer, ModuleAnimationMode.FADE, 1.0, theme().moduleInactiveColor.get(), inactiveGradientColor, gradientDir);
        }

        // Render active/hovered animation overlay
        if (animationProgress > 0) {
            Color bgColor = isActive ? theme().moduleActiveColor.get() : theme().moduleHoveredColor.get();
            Color gradientColor = isActive ? activeGradientColor : theme().moduleHoveredGradientColor.get();

            // Determine if gradient should be applied to this state
            boolean shouldApplyGradient = isActive ? applyMode.appliesToActive() : applyMode.appliesToInactive();
            ModuleGradientDirection effectiveGradientDir = shouldApplyGradient ? gradientDir : ModuleGradientDirection.None;

            renderAnimation(renderer, effectiveAnimationMode, animationProgress, bgColor, gradientColor, effectiveGradientDir);
        }

        double thickness = theme().scale(theme().moduleOutlineThickness.get());
        if (thickness > 0) {
            Color outlineColor = theme().outlineColor.get(pressed, mouseOver);
            renderer.quad(this.x, this.y, width, thickness, outlineColor);
            renderer.quad(this.x, this.y + height - thickness, width, thickness, outlineColor);
            renderer.quad(this.x, this.y + thickness, thickness, height - 2 * thickness, outlineColor);
            renderer.quad(this.x + width - thickness, this.y + thickness, thickness, height - 2 * thickness, outlineColor);
        }

        // Render active module indicator
        if (indicatorProgress > 0) {
            renderIndicator(renderer, indicatorProgress);
        }

        // Determine text color based on module state
        Color textColor;
        if (module.isActive()) {
            textColor = theme().moduleTextActiveColor.get();
        } else if (mouseOver) {
            textColor = theme().moduleTextHoveredColor.get();
        } else {
            textColor = theme().moduleTextInactiveColor.get();
        }

        // Calculate vertical text position based on alignment
        double textY;
        double textHeight = theme().textHeight();
        double availableHeight = height - pad * 2;
        AlignmentY vAlign = theme().moduleAlignmentY.get();
        if (vAlign == AlignmentY.Top) {
            textY = y + pad;
        } else if (vAlign == AlignmentY.Bottom) {
            textY = y + pad + availableHeight - textHeight;
        } else {
            // Center
            textY = y + pad + (availableHeight - textHeight) / 2;
        }

        double textAreaX = this.x + pad;
        double textAreaW = Math.max(0, width - pad * 2);
        double overflow = Math.max(0, titleWidth - textAreaW);

        double textX;
        boolean needsMarquee = overflow > 0 && theme().fixedCategorySize.get();

        if (needsMarquee) {
            if (mouseOver) {
                if (marqueePause > 0) marqueePause = Math.max(0, marqueePause - delta);
                else {
                    marqueeOffset += marqueeDirection * delta * MARQUEE_SPEED;

                    if (marqueeOffset >= overflow) {
                        marqueeOffset = overflow;
                        marqueeDirection = -1;
                        marqueePause = MARQUEE_EDGE_PAUSE;
                    } else if (marqueeOffset <= 0) {
                        marqueeOffset = 0;
                        marqueeDirection = 1;
                        marqueePause = MARQUEE_EDGE_PAUSE;
                    }
                }
            } else {
                if (marqueeOffset > 0) {
                    marqueeOffset = Math.max(0, marqueeOffset - delta * MARQUEE_SPEED * 2);
                    if (marqueeOffset <= 0) {
                        marqueeOffset = 0;
                        marqueeDirection = 1;
                        marqueePause = 0;
                    }
                }
            }

            textX = textAreaX - marqueeOffset;
            renderer.scissorStart(textAreaX, this.y, textAreaW, height);
            renderText(renderer, title, textX, textY, textColor);
            renderer.scissorEnd();
        } else {
            marqueeOffset = 0;
            marqueeDirection = 1;
            marqueePause = 0;

            textX = textAreaX;
            if (theme().moduleAlignment.get() == AlignmentX.Center) {
                textX += textAreaW / 2 - titleWidth / 2;
            }
            else if (theme().moduleAlignment.get() == AlignmentX.Right) {
                textX += textAreaW - titleWidth;
            }

            renderText(renderer, title, textX, textY, textColor);
        }
    }

    /**
     * Renders the active module indicator bar.
     * The indicator slides in from the edge when module is active,
     * and slides out to the same edge when module is deactivated.
     *
     * @param renderer The GUI renderer instance
     * @param progress Animation progress (0.0 = hidden, 1.0 = fully visible)
     */
    private void renderIndicator(GuiRenderer renderer, double progress) {
        ModuleIndicatorPosition position = theme().moduleIndicatorPosition.get();
        if (position == ModuleIndicatorPosition.None) return;

        double thickness = theme().scale(theme().moduleIndicatorThickness.get());
        if (thickness <= 0) return;

        Color accentColor = theme().accentColor.get();
        double size = thickness * progress;

        // Calculate indicator position and dimensions based on position type
        double ix = this.x, iy = this.y, iw = this.width, ih = this.height;

        switch (position) {
            case Left -> iw = size;
            case Right -> { ix = this.x + this.width - size; iw = size; }
            case Top -> ih = size;
            case Bottom -> { iy = this.y + this.height - size; ih = size; }
        }

        renderer.quad(ix, iy, iw, ih, accentColor);
    }

    /**
     * Renders the module animation effect based on the selected animation mode.
     *
     * @param renderer The GUI renderer
     * @param mode The animation mode
     * @param progress Animation progress (0.0 to 1.0)
     * @param bgColor The background color for the animation
     * @param gradientColor The gradient color
     * @param gradientDir The gradient direction
     */
    private void renderAnimation(GuiRenderer renderer, ModuleAnimationMode mode, double progress,
                                   Color bgColor, Color gradientColor, ModuleGradientDirection gradientDir) {
        double rx = x, ry = y, rw = width, rh = height;
        Color renderColor = bgColor;

        switch (mode) {
            case FADE -> renderColor = new Color(bgColor.r, bgColor.g, bgColor.b, (int) (bgColor.a * progress));
            case SLIDE_LEFT -> rw = width * progress;
            case SLIDE_RIGHT -> { rw = width * progress; rx = x + width - rw; }
            case SLIDE_UP -> rh = height * progress;
            case SLIDE_DOWN -> { rh = height * progress; ry = y + height - rh; }
            default -> rw = width * progress;
        }

        GradientRenderer.render(renderer, rx, ry, rw, rh, gradientColor, renderColor, gradientDir);
    }
}
