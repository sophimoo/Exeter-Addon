package me.sophimoo.exeter.gui.themes.base.widgets.pressable;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.widgets.pressable.WConfirmedButton;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBaseConfirmedButton extends WConfirmedButton implements BaseWidget {
    public WBaseConfirmedButton(String text, String confirmText, GuiTexture texture) {
        super(text, confirmText, texture);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();

        Color outline = theme().outlineColor.get(pressed, mouseOver);
        ConfirmColors colors = confirmedColors(theme().textColor.get(), pressed, mouseOver, pressedOnce);

        renderBackground(renderer, this, outline, colors.bg());

        String text = getText();
        renderCenteredTextOrTexture(renderer, text, textWidth, texture, x, y, width, pad, colors.fg());
    }
}
