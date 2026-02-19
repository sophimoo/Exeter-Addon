package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBaseMultiLabel extends WLabel implements BaseWidget {
    private final double maxWidth;

    public WBaseMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title);
        this.maxWidth = maxWidth;
    }

    @Override
    protected void onCalculateSize() {
        String[] texts = text.split("\n");

        double textWidth = 0;
        double textHeight = 0;

        for (String t : texts) {
            textWidth = Math.max(textWidth, theme().textWidth(t, t.length(), title));
            textHeight += theme().textHeight();
        }

        width = maxWidth > 0 ? maxWidth : textWidth;
        height = textHeight;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (text.isEmpty()) return;

        String[] texts = text.split("\n");
        double textY = y;

        Color textColor = this.color != null ? this.color : (title ? theme().titleTextColor.get() : theme().textColor.get());

        for (String t : texts) {
            renderer.text(t, x, textY, textColor, title);
            textY += theme().textHeight();
        }
    }
}
