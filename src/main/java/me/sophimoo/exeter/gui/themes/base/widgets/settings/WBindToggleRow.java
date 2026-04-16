package me.sophimoo.exeter.gui.themes.base.widgets.settings;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class WBindToggleRow extends WBaseSettingToggle {
    public WBindToggleRow(String title, String tooltip, BooleanSupplier getter, Consumer<Boolean> setter, boolean showIndicator) {
        super(title, tooltip, getter, setter, showIndicator);
    }
}
