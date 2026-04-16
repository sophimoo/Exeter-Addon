package me.sophimoo.exeter.mixin.minecraft;

import me.sophimoo.exeter.gui.modal.ModalScreenOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientModalSetScreenMixin {
    @Shadow
    public Screen currentScreen;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void exeter$redirectSetScreenToModal(Screen screen, CallbackInfo ci) {
        if (ModalScreenOps.redirectSetScreen(currentScreen, screen)) ci.cancel();
    }
}
