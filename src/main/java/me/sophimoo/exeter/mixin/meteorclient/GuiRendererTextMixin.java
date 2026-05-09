package me.sophimoo.exeter.mixin.meteorclient;

import me.sophimoo.exeter.gui.renderer.GuiTextRendererAccess;
import me.sophimoo.exeter.gui.renderer.ShadowTextOperation;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.operations.TextOperation;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = GuiRenderer.class, remap = false)
public abstract class GuiRendererTextMixin implements GuiTextRendererAccess {
    @Shadow
    public GuiTheme theme;

    @Shadow
    @Final
    private List<TextOperation> texts;

    @Override
    public void exeter$queueVanillaShadowText(String text, double x, double y, Color color) {
        ShadowTextOperation operation = ShadowTextOperation.get();
        operation.set(x, y, color);
        operation.exeter$set(text, theme.textRenderer(), false, true);
        texts.add(operation);
    }
}
