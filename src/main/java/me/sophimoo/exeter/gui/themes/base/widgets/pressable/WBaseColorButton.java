package me.sophimoo.exeter.gui.themes.base.widgets.pressable;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBaseColorButton extends WButton implements BaseWidget {
    public Color color;

    public WBaseColorButton(Color color) {
        super(null, null);
        this.color = color;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();

        renderBackground(renderer, this, pressed, mouseOver);
        renderCenteredSquare(renderer, x, y, width, pad, color);
    }
}
