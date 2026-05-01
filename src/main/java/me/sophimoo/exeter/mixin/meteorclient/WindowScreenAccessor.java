package me.sophimoo.exeter.mixin.meteorclient;

import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WindowScreen.class, remap = false)
public interface WindowScreenAccessor {
    @Accessor("window")
    WWindow exeter$getWindow();
}
