package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WHorizontalSeparator;

public class WBaseHorizontalSeparator extends WHorizontalSeparator implements BaseWidget {
    public WBaseHorizontalSeparator(String text) {
        super(text);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double s = theme().scale(2);
        double halfHeight = height / 2;

        if (text == null) {
            renderer.quad(x, y + halfHeight - s / 2, width, s, theme().separatorEdges.get());
        }
        else {
            double textWidth = theme().textWidth(text);
            double textHeight = theme().textHeight();
            double textY = y + halfHeight - textHeight / 2;

            double leftWidth = width / 2 - textWidth / 2 - theme().pad() * 2;
            double rightWidth = width / 2 - textWidth / 2 - theme().pad() * 2;

            renderer.quad(x, y + halfHeight - s / 2, leftWidth, s, theme().separatorEdges.get());
            renderer.quad(x + width - rightWidth, y + halfHeight - s / 2, rightWidth, s, theme().separatorEdges.get());

            renderer.text(text, x + width / 2 - textWidth / 2, textY, theme().separatorText.get(), false);
        }
    }
}
