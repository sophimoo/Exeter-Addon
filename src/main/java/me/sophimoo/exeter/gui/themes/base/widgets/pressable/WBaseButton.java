package me.sophimoo.exeter.gui.themes.base.widgets.pressable;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.MarqueeState;
import me.sophimoo.exeter.gui.themes.base.utils.WidgetSizeDebug;
import me.sophimoo.exeter.gui.widgets.ExeterWidthConstrained;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;

public class WBaseButton extends WButton implements BaseWidget, ExeterWidthConstrained {
    private final MarqueeState marquee = new MarqueeState();
    private double maxWidth;

    public WBaseButton(String text, GuiTexture texture) {
        super(text, texture);
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

        WidgetSizeDebug.log(
            theme(),
            this,
            "Button",
            width,
            height,
            "pad=" + String.format(java.util.Locale.US, "%.2f", pad)
        );

        renderBackground(renderer, this, pressed, mouseOver);
        renderCenteredTextOrTexture(renderer, text, textWidth, texture, x, y, width, pad,
            theme().textColor.get(), marquee, mouseOver, delta);
    }
}
