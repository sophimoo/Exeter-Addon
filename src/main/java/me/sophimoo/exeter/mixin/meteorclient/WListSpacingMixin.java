package me.sophimoo.exeter.mixin.meteorclient;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {WHorizontalList.class, WVerticalList.class}, remap = false)
public abstract class WListSpacingMixin extends WWidget {
    @Inject(method = "spacing", at = @At("RETURN"), cancellable = true)
    private void exeter$roundLayoutSpacing(CallbackInfoReturnable<Double> info) {
        if (theme instanceof BaseGuiTheme) info.setReturnValue((double) Math.round(info.getReturnValueD()));
    }
}
