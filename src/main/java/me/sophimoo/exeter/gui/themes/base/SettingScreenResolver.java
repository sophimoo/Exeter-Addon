package me.sophimoo.exeter.gui.themes.base;

import me.sophimoo.exeter.gui.modal.ModalScreenOps;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.settings.Setting;

import java.lang.reflect.Constructor;

public final class SettingScreenResolver {
    private static final String SCREEN_PACKAGE = "meteordevelopment.meteorclient.gui.screens.settings.";

    private SettingScreenResolver() {
    }

    public static boolean canOpenConventional(Setting<?> setting) {
        return getConstructor(setting) != null;
    }

    public static void tryOpenConventional(GuiTheme theme, Setting<?> setting) {
        Constructor<?> constructor = getConstructor(setting);
        if (constructor == null) return;

        try {
            Object screen = constructor.newInstance(theme, setting);
            if (screen instanceof WidgetScreen widgetScreen) {
                ModalScreenOps.open(widgetScreen);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static Constructor<?> getConstructor(Setting<?> setting) {
        String screenClassName = SCREEN_PACKAGE + setting.getClass().getSimpleName() + "Screen";

        try {
            Class<?> screenClass = Class.forName(screenClassName);
            for (Constructor<?> constructor : screenClass.getConstructors()) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length != 2) continue;
                if (!GuiTheme.class.isAssignableFrom(parameterTypes[0])) continue;
                if (!parameterTypes[1].isAssignableFrom(setting.getClass())) continue;
                return constructor;
            }
        } catch (ClassNotFoundException ignored) {
        }

        return null;
    }
}
