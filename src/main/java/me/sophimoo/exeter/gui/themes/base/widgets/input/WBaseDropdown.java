package me.sophimoo.exeter.gui.themes.base.widgets.input;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.MarqueeState;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBaseDropdown<T> extends WDropdown<T> implements BaseWidget {
    private final MarqueeState marquee = new MarqueeState();

    public WBaseDropdown(T[] values, T value) {
        super(values, value);
    }

    @Override
    protected WDropdownRoot createRootWidget() {
        return new WRoot();
    }

    @Override
    protected WDropdownValue createValueWidget() {
        return new WValue();
    }

    @Override
    protected void onCalculateSize() {
        double pad = pad();

        maxValueWidth = 0;
        for (T value : values) {
            double valueWidth = theme.textWidth(value.toString());
            maxValueWidth = Math.max(maxValueWidth, valueWidth);
        }

        root.calculateSize();

        width = pad + maxValueWidth + pad;
        height = pad + theme().textHeight() + pad;

        root.width = width;
    }

    @Override
    protected void onCalculateWidgetPositions() {
        double pad = pad();

        if (parent != null && parent.width > 0) {
            double maxWidth = Math.max(theme().scale(40), parent.width - pad * 2);
            width = Math.min(width, maxWidth);
            root.width = width;
        }

        super.onCalculateWidgetPositions();
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();

        renderBackground(renderer, this, pressed, mouseOver);

        String text = get().toString();
        double textX = x + pad;
        double textW = Math.max(0, width - pad * 2);
        double textH = theme().textHeight();
        double textY = y + (height - textH) / 2;

        double textWidth = theme().textWidth(text);
        double centeredTextX = textX + Math.max(0, (textW - textWidth) / 2);
        renderTextWithMarquee(renderer, marquee, text, textX, y, textW, height, textY, textWidth,
            mouseOver, delta, true, centeredTextX, theme().textColor.get());
    }

    private static class WRoot extends WDropdownRoot implements BaseWidget {
        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double s = theme().scale(2);
            Color c = theme().outlineColor.get();

            renderer.quad(x, y + height - s, width, s, c);
            renderer.quad(x, y, s, height - s, c);
            renderer.quad(x + width - s, y, s, height - s, c);
        }
    }

    private class WValue extends WDropdownValue implements BaseWidget {
        private final MarqueeState valueMarquee = new MarqueeState();

        @Override
        protected void onCalculateSize() {
            double pad = pad();

            width = pad + theme().textWidth(value.toString()) + pad;
            height = pad + theme().textHeight() + pad;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            Color baseColor = theme().backgroundColor.get(pressed, mouseOver, true);
            int boostedAlpha = Math.min(255, baseColor.a + baseColor.a / 2);
            Color color = new Color(baseColor.r, baseColor.g, baseColor.b, boostedAlpha);

            renderer.quad(this, color);

            String text = value.toString();
            double pad = pad();
            double textX = x + pad;
            double textW = Math.max(0, width - pad * 2);
            double textH = theme().textHeight();
            double textY = y + (height - textH) / 2;

            double valueTextWidth = theme().textWidth(text);
            renderTextWithMarquee(renderer, valueMarquee, text, textX, y, textW, height, textY, valueTextWidth,
                mouseOver, delta, true, textX, theme().textColor.get());
        }
    }
}
