package me.sophimoo.exeter.mixin.meteorclient;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Cell.class, remap = false)
public abstract class CellMixin {
    @Shadow @Final private WWidget widget;

    @Inject(method = "s", at = @At("RETURN"), cancellable = true)
    private void exeter$roundLayoutPadding(double value, CallbackInfoReturnable<Double> info) {
        if (widget.theme instanceof BaseGuiTheme) info.setReturnValue((double) Math.round(info.getReturnValueD()));
    }
}
