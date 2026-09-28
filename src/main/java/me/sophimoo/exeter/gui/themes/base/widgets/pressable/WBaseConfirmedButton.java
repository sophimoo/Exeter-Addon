package me.sophimoo.exeter.gui.themes.base.widgets.pressable;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.widgets.ExeterWidthConstrained;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.widgets.pressable.WConfirmedButton;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBaseConfirmedButton extends WConfirmedButton implements BaseWidget, ExeterWidthConstrained {
    private final MarqueeState marquee = new MarqueeState();
    private double maxWidth;

    public WBaseConfirmedButton(String text, String confirmText, GuiTexture texture) {
        super(text, confirmText, texture);
    }

    @Override
    public void exeter$setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
    }

    @Override
    protected void onCalculateSize() {
        super.onCalculateSize();
        if (maxWidth > 0) width = Math.min(width, maxWidth);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double pad = pad();

        Color outline = theme().outlineColor.get(pressed, mouseOver);
        ConfirmColors colors = confirmedColors(theme().textColor.get(), pressed, mouseOver, pressedOnce);

        renderBackground(renderer, this, outline, colors.bg());

        String text = getText();
        renderCenteredTextOrTexture(renderer, text, textWidth, texture, x, y, width, pad,
            colors.fg(), marquee, mouseOver, delta);
    }
}
