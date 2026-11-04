package me.sophimoo.exeter.gui.renderer;

import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleGradientDirection;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.math.MathHelper;

/**
 * Utility class for rendering gradient fills using the GUI renderer.
 * Supports multiple gradient directions for smooth color transitions.
 */
public class GradientRenderer {

    public static Color interpolateColor(Color from, Color to, double progress) {
        progress = MathHelper.clamp(progress, 0, 1);
        return new Color(
            (int) Math.round(MathHelper.lerp(progress, from.r, to.r)),
            (int) Math.round(MathHelper.lerp(progress, from.g, to.g)),
            (int) Math.round(MathHelper.lerp(progress, from.b, to.b)),
            (int) Math.round(MathHelper.lerp(progress, from.a, to.a))
        );
    }

    public static void renderOutline(GuiRenderer renderer, double x, double y, double width, double height,
                                     double thickness, Color color1, Color color2, ModuleGradientDirection direction) {

        if (width <= 0 || height <= 0 || thickness <= 0) return;

        thickness = Math.min(thickness, Math.min(width, height) / 2);

        boolean horizontal = direction == ModuleGradientDirection.HORIZONTAL_LEFT_TO_RIGHT || direction == ModuleGradientDirection.HORIZONTAL_RIGHT_TO_LEFT;
        boolean reversed = direction == ModuleGradientDirection.HORIZONTAL_RIGHT_TO_LEFT || direction == ModuleGradientDirection.VERTICAL_BOTTOM_TO_TOP;

        if (direction == ModuleGradientDirection.NONE) color1 = color2;

        for (int edge = 0; edge < 4; edge++) {

            double edgeX = edge == 3 ? width - thickness : 0;
            double edgeY = edge == 1 ? height - thickness : edge >= 2 ? thickness : 0;
            double edgeWidth = edge < 2 ? width : thickness;
            double edgeHeight = edge < 2 ? thickness : height - 2 * thickness;

            if (edgeWidth <= 0 || edgeHeight <= 0) continue;

            double from = horizontal ? edgeX / width : edgeY / height;
            double to = horizontal ? (edgeX + edgeWidth) / width : (edgeY + edgeHeight) / height;

            Color first = interpolateColor(color1, color2, reversed ? 1 - to : from);
            Color last = interpolateColor(color1, color2, reversed ? 1 - from : to);

            render(renderer, x + edgeX, y + edgeY, edgeWidth, edgeHeight, first, last, direction);
        }
    }

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

        if (width <= 0 || height <= 0) return;

        if (direction == ModuleGradientDirection.NONE) {
            renderer.quad(x, y, width, height, color2);
            return;
        }

        switch (direction) {
            case HORIZONTAL_LEFT_TO_RIGHT -> {
                renderer.quad(x, y, width, height, color1, color2);
            }
            case HORIZONTAL_RIGHT_TO_LEFT -> {
                renderer.quad(x, y, width, height, color2, color1);
            }
            case VERTICAL_TOP_TO_BOTTOM -> {
                renderer.quad(x, y, width, height, color1, color1, color2, color2);
            }
            case VERTICAL_BOTTOM_TO_TOP -> {
                renderer.quad(x, y, width, height, color2, color2, color1, color1);
            }
            default -> renderer.quad(x, y, width, height, color2);
        }
    }
}
