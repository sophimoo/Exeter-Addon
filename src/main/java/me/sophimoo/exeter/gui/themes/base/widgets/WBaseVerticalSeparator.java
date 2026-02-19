package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WVerticalSeparator;

public class WBaseVerticalSeparator extends WVerticalSeparator implements BaseWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double s = theme().scale(2);
        renderer.quad(x + width / 2 - s / 2, y, s, height, theme().separatorCenter.get());
    }
}
