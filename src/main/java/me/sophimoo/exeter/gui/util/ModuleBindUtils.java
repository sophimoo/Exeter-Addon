package me.sophimoo.exeter.gui.util;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import me.sophimoo.exeter.gui.themes.base.widgets.settings.WBindControlRow;
import me.sophimoo.exeter.gui.themes.base.widgets.settings.WBindToggleRow;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class ModuleBindUtils {
    private ModuleBindUtils() {}

    public static WKeybind populateBindSection(Cell<WSection> bindSectionCell, double paddingX, double separatorPaddingY, double itemSpacing, Module module, GuiTheme theme) {
        WSection bindSection = bindSectionCell.widget();
        bindSection.spacing = 0;

        WTable table = bindSection.add(theme.table()).expandX().padHorizontal(paddingX).padTop(separatorPaddingY).widget();
        table.verticalSpacing = theme.scale(itemSpacing);

        WKeybind keybindWidget = addKeybindRow(table, module, theme);
        addToggleRows(table, module);
        addSharingButtons(table, module, theme);
        return keybindWidget;
    }

    public static WKeybind addKeybindRow(WTable table, Module module, GuiTheme theme) {
        WContainer bindControls = theme.horizontalList();
        WKeybind keybindWidget = bindControls.add(theme.keybind(module.keybind)).expandX().widget();
        table.add(new WBindControlRow("Keybind", "Set the key used to toggle this module.", bindControls)).expandX();
        table.row();
        return keybindWidget;
    }

    public static void addToggleRows(WTable table, Module module) {
        addToggleRow(table, "Hold Toggle", "Toggle this module when its bind is released.",
            () -> module.toggleOnBindRelease,
            checked -> module.toggleOnBindRelease = checked);
        addToggleRow(table, "Chat Feedback", "Show chat messages when this module toggles.",
            () -> module.chatFeedback,
            checked -> module.chatFeedback = checked);
    }

    public static void addSharingButtons(WTable table, Module module, GuiTheme theme) {
        table.row();

        WHorizontalList sharing = theme.horizontalList();
        sharing.spacing = 0;

        WButton copy = sharing.add(theme.button(GuiRenderer.COPY)).expandX().widget();
        copy.action = () -> copyModuleToClipboard(module);
        copy.tooltip = "Copy module config";

        WButton paste = sharing.add(theme.button(GuiRenderer.PASTE)).expandX().widget();
        paste.action = () -> pasteModuleFromClipboard(module);
        paste.tooltip = "Paste module config";

        WButton reset = sharing.add(theme.button(GuiRenderer.RESET)).expandX().widget();
        reset.action = () -> module.settings.reset();
        reset.tooltip = "Reset all settings to default";

        table.add(sharing).expandX();
    }

    private static void addToggleRow(WTable table, String title, String description, BooleanSupplier getter, Consumer<Boolean> setter) {
        table.add(new WBindToggleRow(title, description, getter, setter, true)).expandX();
        table.row();
    }

    public static void copyModuleToClipboard(Module module) {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", module.name);
        NbtCompound settingsTag = module.settings.toTag();
        if (!settingsTag.isEmpty()) tag.put("settings", settingsTag);
        NbtUtils.toClipboard(tag);
    }

    public static void pasteModuleFromClipboard(Module module) {
        NbtCompound tag = NbtUtils.fromClipboard();
        if (tag == null) return;
        if (!tag.getString("name", "").equals(module.name)) return;
        Optional<NbtCompound> settings = tag.getCompound("settings");
        if (settings.isPresent()) module.settings.fromTag(settings.get());
        else module.settings.reset();
    }
}
