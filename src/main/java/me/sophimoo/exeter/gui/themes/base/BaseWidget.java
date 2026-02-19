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

        if (theme.widgetBlurStrength.get() > 0) {
            WorldFramebufferCapture capture = WorldFramebufferCapture.getInstance();
            GpuTextureView blurTexture = capture != null ? capture.getBlurredTexture() : null;

            if (blurTexture != null) {
                ((BlurRendererAccess) renderer).blurredQuad(innerX, innerY, innerWidth, innerHeight, blurTexture, backgroundColor);
            } else {
                renderer.quad(innerX, innerY, innerWidth, innerHeight, backgroundColor);
            }
        } else {
            renderer.quad(innerX, innerY, innerWidth, innerHeight, backgroundColor);
        }

        renderer.quad(widget.x, widget.y, widget.width, s, outlineColor);
        renderer.quad(widget.x, widget.y + widget.height - s, widget.width, s, outlineColor);
        renderer.quad(widget.x, widget.y + s, s, widget.height - s * 2, outlineColor);
        renderer.quad(widget.x + widget.width - s, widget.y + s, s, widget.height - s * 2, outlineColor);
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        BaseGuiTheme theme = theme();
        renderBackground(renderer, widget, theme.outlineColor.get(pressed, mouseOver), theme.backgroundColor.get(pressed, mouseOver));
    }
}
