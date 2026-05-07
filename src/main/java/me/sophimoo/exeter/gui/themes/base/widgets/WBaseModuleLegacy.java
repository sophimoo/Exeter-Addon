package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.modal.ModalScreenOps;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WBaseModuleLegacy extends WBaseModuleRow {
    public WBaseModuleLegacy(meteordevelopment.meteorclient.systems.modules.Module module, String title) {
        super(module, title);
    }

    @Override
    protected boolean isSettingsExpanded() {
        return false;
    }

    @Override
    protected void onRightClick() {
        if (theme().modalWindows.get()) {
            ModalScreenOps.open(theme().moduleScreen(module));
        } else {
            mc.setScreen(theme().moduleScreen(module));
        }
    }
}
