package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBaseLabel extends WLabel implements BaseWidget {
    public WBaseLabel(String text, boolean title) {
        super(text, title);
    }

    @Override
    protected void onCalculateSize() {
        if (title) {
            width = theme.textWidth(text, text.length(), false);
            height = theme.textHeight(false);
            return;
        }

        super.onCalculateSize();
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!text.isEmpty()) {
            Color textColor = color != null ? color : (title ? theme().titleTextColor.get() : theme().textColor.get());
            renderText(renderer, text, x, y, textColor);
        }
    }
}
