package me.sophimoo.exeter.gui.themes.base.widgets.pressable;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WConfirmedMinus;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBaseConfirmedMinus extends WConfirmedMinus implements BaseWidget {
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();
        double s = theme().scale(3);

        Color outline = theme().outlineColor.get(pressed, mouseOver);
        ConfirmColors colors = confirmedColors(theme().minusColor.get(), pressed, mouseOver, pressedOnce);

        renderBackground(renderer, this, outline, colors.bg());
        renderHorizontalGlyph(renderer, x, y, width, height, pad, s, colors.fg());
    }
}
