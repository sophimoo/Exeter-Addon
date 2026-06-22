package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WHorizontalSeparator;

public class WBaseHorizontalSeparator extends WHorizontalSeparator implements BaseWidget {
    public WBaseHorizontalSeparator(String text) { super(text); }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double s = theme().separatorThickness(), halfHeight = height / 2, sepY = y + halfHeight - s / 2;

        if (text == null) {
            renderer.quad(x, sepY, width, s, theme().separatorInactiveColor.get());
        } else {
            double textWidth = theme().textWidth(text), textHeight = theme().textHeight();
            double sideWidth = width / 2 - textWidth / 2 - theme().pad() * 2;
            renderer.quad(x, sepY, sideWidth, s, theme().separatorInactiveColor.get());
            renderer.quad(x + width - sideWidth, sepY, sideWidth, s, theme().separatorInactiveColor.get());
            renderText(renderer, text, x + width / 2 - textWidth / 2, y + halfHeight - textHeight / 2, theme().separatorTextInactiveColor.get());
        }
    }
}
