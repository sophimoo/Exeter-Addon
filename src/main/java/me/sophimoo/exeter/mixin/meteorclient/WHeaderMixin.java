package me.sophimoo.exeter.mixin.meteorclient;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.pressable.WTriangle;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// https://github.com/X-C-0/catppuccin-addon/blob/d642959fbaa9e5757013ea38f57556eb88c8b822/src/main/java/me/pindour/catppuccin/mixin/meteorclient/WHeaderMixin.java
@Mixin(targets = "meteordevelopment.meteorclient.gui.widgets.containers.WWindow$WHeader", remap = false)
public abstract class WHeaderMixin {
    @Shadow
    @Final
    WWindow this$0;

    @WrapWithCondition(
        method = "render",
        at = @At(
            value = "FIELD",
            target = "Lmeteordevelopment/meteorclient/gui/widgets/pressable/WTriangle;rotation:D",
            opcode = Opcodes.PUTFIELD
        )
    )
    private boolean exeter$shouldSetTriangleRotation(WTriangle triangle, double value) {
        return !(this$0.theme instanceof BaseGuiTheme);
    }
}
