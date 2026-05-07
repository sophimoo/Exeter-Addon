package me.sophimoo.exeter.gui.renderer;

import me.sophimoo.exeter.gui.themes.base.utils.ModuleGradientDirection;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

/**
 * Utility class for rendering gradient fills using the GUI renderer.
 * Supports multiple gradient directions for smooth color transitions.
 */
public class GradientRenderer {

    /**
     * Renders either a gradient or solid color based on the gradient direction setting.
     * Gradient fades from color1 to color2 in the specified direction.
     *
     * @param renderer The GUI renderer instance
     * @param x X position
     * @param y Y position
     * @param width Width of the area to render
     * @param height Height of the area to render
     * @param color1 Starting color of the gradient
     * @param color2 Ending color of the gradient
     * @param direction Direction of the gradient
     */
    public static void render(GuiRenderer renderer, double x, double y, double width, double height,
                             Color color1, Color color2, ModuleGradientDirection direction) {
        if (direction == ModuleGradientDirection.None || width <= 0 || height <= 0) {
            renderer.quad(x, y, width, height, color2);
            return;
        }

        switch (direction) {
            case Horizontal_LeftToRight -> {
                renderer.quad(x, y, width, height, color1, color2);
            }
            case Horizontal_RightToLeft -> {
                renderer.quad(x, y, width, height, color2, color1);
            }
            case Vertical_TopToBottom -> {
                renderer.quad(x, y, width, height, color1, color1, color2, color2);
            }
            case Vertical_BottomToTop -> {
                renderer.quad(x, y, width, height, color2, color2, color1, color1);
            }
            default -> renderer.quad(x, y, width, height, color2);
        }
    }
}
