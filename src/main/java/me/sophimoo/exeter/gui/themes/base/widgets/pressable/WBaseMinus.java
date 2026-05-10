package me.sophimoo.exeter.gui.themes.base.widgets.pressable;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;

public class WBaseMinus extends WMinus implements BaseWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();
        double s = theme().glyphThickness();

        renderBackground(renderer, this, pressed, mouseOver);
        renderHorizontalGlyph(renderer, x, y, width, height, pad, s, theme().minusColor.get());
    }
}
