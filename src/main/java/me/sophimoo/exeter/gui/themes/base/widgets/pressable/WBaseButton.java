package me.sophimoo.exeter.gui.themes.base.widgets.pressable;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.WidgetSizeDebug;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;

public class WBaseButton extends WButton implements BaseWidget {
    public WBaseButton(String text, GuiTexture texture) {
        super(text, texture);
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
        renderCenteredTextOrTexture(renderer, text, textWidth, texture, x, y, width, pad, theme().textColor.get());
    }
}
