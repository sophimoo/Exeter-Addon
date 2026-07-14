package me.sophimoo.exeter.mixin.meteorclient;

import me.sophimoo.exeter.gui.modal.WidgetScreenModalBridge;
import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.EditSystemScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WHorizontalSeparator;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EditSystemScreen.class, remap = false)
public abstract class EditSystemScreenMixin {
    @Inject(method = "initWidgets", at = @At("RETURN"))
    private void exeter$padActionRow(CallbackInfo ci) {
        EditSystemScreen<?> self = (EditSystemScreen<?>) (Object) this;
        if (!(self instanceof WindowScreenAccessor accessor)) return;

        GuiTheme theme = ((WidgetScreenModalBridge) self).exeter$getTheme();
        if (!(theme instanceof BaseGuiTheme baseTheme)) return;

        WWindow window = accessor.exeter$getWindow();
        WView view = window.view;
        if (view == null || view.cells.isEmpty()) return;

        double padX = baseTheme.itemPaddingX.get();

        for (Cell<?> cell : view.cells) {
            WWidget widget = cell.widget();
            if (widget instanceof WButton || widget instanceof WHorizontalSeparator) {
                cell.padHorizontal(padX);
            }
        }
    }
}
