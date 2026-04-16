package me.sophimoo.exeter.gui.modal;

import meteordevelopment.meteorclient.gui.WidgetScreen;
import net.minecraft.client.gui.screen.Screen;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public final class ModalScreenOps {
    private ModalScreenOps() {
    }

    public static void open(WidgetScreen screen) {
        open(mc.currentScreen, screen);
    }

    public static void open(Screen currentScreen, WidgetScreen screen) {
        if (screen == null) return;

        if (currentScreen instanceof WidgetScreen current) {
            WidgetScreen target = ((WidgetScreenModalBridge) current).exeter$getModalTarget();
            ((WidgetScreenModalBridge) target).exeter$openModal(screen);
        } else {
            mc.setScreen(screen);
        }
    }

    public static boolean redirectSetScreen(Screen currentScreen, Screen screen) {
        if (!(screen instanceof WidgetScreen widgetScreen)) return false;
        if (!(currentScreen instanceof WidgetScreen current)) return false;

        WidgetScreenModalBridge bridge = (WidgetScreenModalBridge) current;
        if (!bridge.exeter$hasModals()) return false;

        WidgetScreen target = bridge.exeter$getModalTarget();
        ((WidgetScreenModalBridge) target).exeter$openModal(widgetScreen);
        return true;
    }
}
