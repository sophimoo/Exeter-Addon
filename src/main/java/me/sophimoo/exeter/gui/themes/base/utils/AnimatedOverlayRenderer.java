package me.sophimoo.exeter.gui.themes.base.utils;

import me.sophimoo.exeter.gui.renderer.GradientRenderer;
import me.sophimoo.exeter.gui.themes.base.utils.enums.SelectionRenderingMode;
import me.sophimoo.exeter.gui.themes.base.utils.enums.ModuleGradientDirection;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;

public final class AnimatedOverlayRenderer {
    private AnimatedOverlayRenderer() {
    }

    public static void render(GuiRenderer renderer,
                              double x, double y, double width, double height,
                              SelectionRenderingMode mode, double progress,
                              Color bgColor, Color gradientColor, ModuleGradientDirection gradientDir) {
        double rx = x;
        double ry = y;
        double rw = width;
        double rh = height;
        Color renderColor = bgColor;
        Color renderGradientColor = gradientColor;

        switch (mode) {
            case FADE -> {
                renderColor = new Color(bgColor.r, bgColor.g, bgColor.b, (int) (bgColor.a * progress));
                if (gradientColor != null) renderGradientColor = new Color(gradientColor.r, gradientColor.g, gradientColor.b, (int) (gradientColor.a * progress));
            }
            case SLIDE_LEFT -> rw = width * progress;
            case SLIDE_RIGHT -> {
                rw = width * progress;
                rx = x + width - rw;
            }
            case SLIDE_UP -> rh = height * progress;
            case SLIDE_DOWN -> {
                rh = height * progress;
                ry = y + height - rh;
            }
            default -> rw = width * progress;
        }

        GradientRenderer.render(renderer, rx, ry, rw, rh, renderGradientColor, renderColor, gradientDir);
    }
}
