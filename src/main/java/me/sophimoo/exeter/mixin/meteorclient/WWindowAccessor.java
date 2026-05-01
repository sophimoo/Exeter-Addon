package me.sophimoo.exeter.mixin.meteorclient;

import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = WWindow.class, remap = false)
public interface WWindowAccessor {
    @Accessor("moved")
    void exeter$setMoved(boolean moved);

    @Accessor("movedX")
    void exeter$setMovedX(double movedX);

    @Accessor("movedY")
    void exeter$setMovedY(double movedY);
}
