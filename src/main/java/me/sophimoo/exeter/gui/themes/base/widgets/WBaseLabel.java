package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.widgets.ExeterWidthConstrained;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBaseLabel extends WLabel implements BaseWidget, ExeterWidthConstrained {
    private final MarqueeState marquee = new MarqueeState();
    private double wrapWidth;
    private double textWidth;

    public WBaseLabel(String text, boolean title) {
        super(text, title);
    }

    @Override
    public void exeter$setMaxWidth(double maxWidth) {
        wrapWidth = maxWidth;
    }

    @Override
    protected void onCalculateSize() {
        if (title) {
            width = theme.textWidth(text, text.length(), false);
            height = theme.textHeight(false);
        } else super.onCalculateSize();

        textWidth = width;
        if (wrapWidth > 0) width = Math.min(width, wrapWidth);
    }

    @Override
    public void set(String text) {
        if (!text.equals(this.text)) invalidate();

        this.text = text;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!text.isEmpty()) {
            Color textColor = color != null ? color : (title ? theme().titleTextColor.get() : theme().textColor.get());
            renderTextWithMarquee(renderer, marquee, text, x, y, width, height, y, textWidth,
                mouseOver, delta, wrapWidth > 0, x, textColor);
        }
    }
}
