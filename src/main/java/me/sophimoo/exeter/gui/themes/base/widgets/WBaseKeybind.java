package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.widgets.pressable.WBaseButton;
import meteordevelopment.meteorclient.systems.modules.Modules;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.client.input.KeyInput;

public class WBaseKeybind extends WKeybind implements BaseWidget {
    private final Keybind keybind;
    private final Keybind defaultValue;
    private WButton button;
    private boolean listening;

    public WBaseKeybind(Keybind keybind, Keybind defaultValue) {
        super(keybind, defaultValue);
        this.keybind = keybind;
        this.defaultValue = defaultValue;
    }

    @Override
    public void init() {
        button = add(new WBaseButton("", null)).widget();

        button.action = () -> {
            listening = true;
            button.set("...");

            if (actionOnSet != null) actionOnSet.run();
        };

        refreshLabel();
    }

    @Override
    public boolean onClear() {
        if (listening) {
            keybind.reset();
            reset();

            return true;
        }

        return false;
    }

    @Override
    public boolean onKeyPressed(KeyInput input) {
        if (listening) return true;
        return super.onKeyPressed(input);
    }

    @Override
    public boolean onAction(boolean isKey, int value, int modifiers) {
        if (listening && keybind.canBindTo(isKey, value, modifiers)) {
            keybind.set(isKey, value, modifiers);
            reset();

            return true;
        }

        return false;
    }

    @Override
    public void resetBind() {
        keybind.set(defaultValue);
        reset();
    }

    @Override
    public void reset() {
        listening = false;
        refreshLabel();
        if (Modules.get().isBinding()) Modules.get().setModuleToBind(null);
        if (action != null) action.run();
    }

    private void refreshLabel() {
        button.set(keybind.toString());
    }
}
