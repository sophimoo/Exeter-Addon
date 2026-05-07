package me.sophimoo.exeter.gui.themes.base.utils;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;

public final class WidgetSizeDebug {
    private static final Map<Object, String> LAST_LOGGED = Collections.synchronizedMap(new IdentityHashMap<>());

    private WidgetSizeDebug() {
    }

    public static void log(BaseGuiTheme theme, Object owner, String widgetType, double width, double height, String details) {
        if (theme == null || !theme.debugWidgetSizes.get()) return;

        String suffix = (details == null || details.isBlank()) ? "" : " " + details;
        String snapshot = String.format(Locale.US, "w=%.2f h=%.2f%s", width, height, suffix);
        String previous = LAST_LOGGED.put(owner, snapshot);
        if (snapshot.equals(previous)) return;

        System.out.println("[Exeter:WidgetSize] " + widgetType + " " + snapshot);
    }
}
