package me.sophimoo.exeter.gui.modal;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
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
        if (current instanceof HudEditorScreen) return false;

        GuiTheme theme = ((WidgetScreenModalBridge) current).exeter$getTheme();

        if (!(theme instanceof BaseGuiTheme baseTheme) || !baseTheme.modalWindows.get()) return false;
        if (!(widgetScreen instanceof WindowScreen)) return false;

        WidgetScreen target = ((WidgetScreenModalBridge) current).exeter$getModalTarget();
        ((WidgetScreenModalBridge) target).exeter$openModal(widgetScreen);

        return true;
    }
}
