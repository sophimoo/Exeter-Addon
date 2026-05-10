package me.sophimoo.exeter.gui.themes.base;

import me.sophimoo.exeter.gui.renderer.BlurRendererAccess;
import me.sophimoo.exeter.gui.renderer.WorldFramebufferCapture;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import com.mojang.blaze3d.textures.GpuTextureView;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.renderer.text.VanillaTextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import me.sophimoo.exeter.gui.renderer.GuiTextRendererAccess;

public interface BaseWidget extends meteordevelopment.meteorclient.gui.utils.BaseWidget {
    record ConfirmColors(Color fg, Color bg) {}

    default BaseGuiTheme theme() {
        return (BaseGuiTheme) getTheme();
    }

    default double resolveModuleRowHeight(double defaultHeight) {
        double customHeight = theme().moduleHeight.get();
        if (customHeight > 0) return theme().scale(customHeight);
        return defaultHeight;
    }

    default double resolveItemRowHeight(double defaultHeight) {
        double customHeight = theme().itemHeight.get();
        if (customHeight > 0) return theme().scale(customHeight);
        return defaultHeight;
    }

    default double resolveSeparatorRowHeight(double defaultHeight) {
        double customHeight = theme().separatorHeight.get();
        if (customHeight > 0) return theme().scale(customHeight);
        return defaultHeight;
    }

    default double moduleRowBaseHeight(double extraPx) {
        return resolveModuleRowHeight(theme().rowPadY() + theme().textHeight() + theme().rowPadY() + theme().scaledPx(extraPx));
    }

    default double itemRowBaseHeight(double extraPx) {
        return resolveItemRowHeight(theme().rowPadY() + theme().textHeight() + theme().rowPadY() + theme().scaledPx(extraPx));
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
        if (theme().textShadow.get() && theme().textRenderer() instanceof VanillaTextRenderer && renderer instanceof GuiTextRendererAccess shadowRenderer) {
            shadowRenderer.exeter$queueVanillaShadowText(text, x, y, color);
            return;
        }

        if (theme().textShadow.get()) {
            Color shadowColor = theme().textShadowColor.get();
            int shadowAlpha = (int) ((color.a / 255.0) * shadowColor.a);
            Color adjustedShadowColor = new Color(shadowColor.r, shadowColor.g, shadowColor.b, shadowAlpha);
            double offset = theme().scale(theme().textShadowOffset.get());
            renderer.text(text, x + offset, y + offset, adjustedShadowColor, false);
        }

        renderer.text(text, x, y, color, false);
    }

    default void renderTextWithMarquee(GuiRenderer renderer, MarqueeState marqueeState, String text,
                                       double textAreaX, double textAreaY, double textAreaW, double textAreaH,
                                       double textY, double textWidth, boolean animate, double delta,
                                       boolean marqueeEnabled, double staticTextX, Color color) {
        double overflow = Math.max(0, textWidth - textAreaW);

        if (marqueeEnabled && overflow > 0 && textAreaW > 0) {
            double marqueeOffset = marqueeState.step(overflow, animate, delta);
            renderer.scissorStart(textAreaX, textAreaY, textAreaW, textAreaH);
            renderText(renderer, text, textAreaX - marqueeOffset, textY, color);
            renderer.scissorEnd();
            return;
        }

        marqueeState.reset();
        renderText(renderer, text, staticTextX, textY, color);
    }

    default void renderCenteredTextOrTexture(GuiRenderer renderer, String text, double textWidth, GuiTexture texture,
                                             double x, double y, double width, double pad, Color color) {
        if (text != null) {
            renderText(renderer, text, x + width / 2 - textWidth / 2, y + pad, color);
        } else {
            double ts = theme().textHeight();
            renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, texture, color);
        }
    }

    default void renderCenteredSquare(GuiRenderer renderer, double x, double y, double width, double pad, Color color) {
        double ts = theme().textHeight();
        renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, color);
    }

    default void renderHorizontalGlyph(GuiRenderer renderer, double x, double y, double width, double height,
                                       double pad, double thickness, Color color) {
        renderer.quad(x + pad, y + height / 2 - thickness / 2, width - pad * 2, thickness, color);
    }

    default void renderVerticalGlyph(GuiRenderer renderer, double x, double y, double width, double height,
                                     double pad, double thickness, Color color) {
        renderer.quad(x + width / 2 - thickness / 2, y + pad, thickness, height - pad * 2, color);
    }

    default ConfirmColors confirmedColors(Color normalFg, boolean pressed, boolean mouseOver, boolean pressedOnce) {
        Color normalBg = theme().backgroundColor.get(pressed, mouseOver);
        if (pressedOnce) return new ConfirmColors(normalBg, normalFg);
        return new ConfirmColors(normalFg, normalBg);
    }
}
