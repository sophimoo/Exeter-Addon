package me.sophimoo.exeter.gui.themes.base;

import me.sophimoo.exeter.gui.renderer.BlurRendererAccess;
import me.sophimoo.exeter.gui.renderer.WorldFramebufferCapture;
import com.mojang.blaze3d.textures.GpuTextureView;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.render.color.Color;

public interface BaseWidget extends meteordevelopment.meteorclient.gui.utils.BaseWidget {
    default BaseGuiTheme theme() {
        return (BaseGuiTheme) getTheme();
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, Color outlineColor, Color backgroundColor) {
        BaseGuiTheme theme = theme();
        double s = theme.scale(2);

        double innerX = widget.x + s;
        double innerY = widget.y + s;
        double innerWidth = widget.width - s * 2;
        double innerHeight = widget.height - s * 2;

        renderQuadWithOptionalBlur(renderer, innerX, innerY, innerWidth, innerHeight, backgroundColor);

        if (outlineColor != null) {
            renderOutline(renderer, widget.x, widget.y, widget.width, widget.height, s, outlineColor);
        }
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        BaseGuiTheme theme = theme();
        renderBackground(renderer, widget, theme.outlineColor.get(pressed, mouseOver), theme.backgroundColor.get(pressed, mouseOver));
    }

    default void renderQuadWithOptionalBlur(GuiRenderer renderer, double x, double y, double width, double height, Color color) {
        if (theme().widgetBlurStrength.get() > 0) {
            WorldFramebufferCapture capture = WorldFramebufferCapture.getInstance();
            GpuTextureView blurTexture = capture != null ? capture.getBlurredTexture() : null;

            if (blurTexture != null) {
                ((BlurRendererAccess) renderer).blurredQuad(x, y, width, height, blurTexture, color);
                return;
            }
        }

        renderer.quad(x, y, width, height, color);
    }

    default void renderOutline(GuiRenderer renderer, double x, double y, double width, double height, double thickness, Color color) {
        renderer.quad(x, y, width, thickness, color);
        renderer.quad(x, y + height - thickness, width, thickness, color);
        renderer.quad(x, y + thickness, thickness, height - 2 * thickness, color);
        renderer.quad(x + width - thickness, y + thickness, thickness, height - 2 * thickness, color);
    }

    default void renderText(GuiRenderer renderer, String text, double x, double y, Color color) {
        if (theme().textShadow.get()) {
            Color shadowColor = theme().textShadowColor.get();
            int shadowAlpha = (int) ((color.a / 255.0) * shadowColor.a);
            Color adjustedShadowColor = new Color(shadowColor.r, shadowColor.g, shadowColor.b, shadowAlpha);
            double offset = theme().textShadowOffset.get();
            renderer.text(text, x + offset, y + offset, adjustedShadowColor, false);
        }

        renderer.text(text, x, y, color, false);
    }
}
