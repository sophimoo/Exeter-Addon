package me.sophimoo.exeter.gui.themes.base;

import me.sophimoo.exeter.gui.screens.settings.ExeterColorSettingScreen;
import me.sophimoo.exeter.gui.modal.ModalScreenOps;
import me.sophimoo.exeter.gui.themes.base.utils.CompactNumberTextBoxes;
import me.sophimoo.exeter.gui.themes.base.utils.SettingScreenResolver;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.settings.*;
import me.sophimoo.exeter.gui.themes.base.widgets.pressable.WBaseColorButton;
import me.sophimoo.exeter.gui.themes.base.widgets.input.WBaseBlockPosEdit;
import me.sophimoo.exeter.gui.themes.base.widgets.settings.WBaseSettingControlRow;
import me.sophimoo.exeter.gui.themes.base.widgets.settings.WBaseSettingSlider;
import me.sophimoo.exeter.gui.themes.base.widgets.settings.WBaseSettingToggle;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.WItem;
import meteordevelopment.meteorclient.gui.widgets.WItemWithLabel;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.WLabel;

import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.elements.keyboard.KeyboardHud;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.resource.language.I18n;
import org.apache.commons.lang3.Strings;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BaseSettingsWidgetFactory extends SettingsWidgetFactory {
    public BaseSettingsWidgetFactory(GuiTheme theme) {
        super(theme);

        register(BoolSetting.class, this::boolW);
        register(IntSetting.class, this::intW);
        register(DoubleSetting.class, this::doubleW);
        register(StringSetting.class, this::stringW);
        register(ProvidedStringSetting.class, this::providedStringW);
        register(ColorSetting.class, this::colorW);
        register(KeybindSetting.class, this::keybindW);
        register(BlockSetting.class, this::blockW);
        registerSelectScreenSetting(BlockListSetting.class, BlockListSettingScreen::new);
        register(ItemSetting.class, this::itemW);
        registerSelectScreenSetting(ItemListSetting.class, ItemListSettingScreen::new);
        registerSelectScreenSetting(EntityTypeListSetting.class, EntityTypeListSettingScreen::new);
        registerSelectScreenSetting(EnchantmentListSetting.class, EnchantmentListSettingScreen::new);
        registerSelectScreenSetting(ModuleListSetting.class, ModuleListSettingScreen::new);
        registerSelectScreenSetting(PacketListSetting.class, PacketBoolSettingScreen::new);
        registerSelectScreenSetting(ParticleTypeListSetting.class, ParticleTypeListSettingScreen::new);
        registerSelectScreenSetting(SoundEventListSetting.class, SoundEventListSettingScreen::new);
        registerSelectScreenSetting(StatusEffectAmplifierMapSetting.class, StatusEffectAmplifierMapSettingScreen::new);
        registerSelectScreenSetting(StatusEffectListSetting.class, StatusEffectListSettingScreen::new);
        registerSelectScreenSetting(StorageBlockListSetting.class, StorageBlockListSettingScreen::new);
        registerSelectScreenSetting(ScreenHandlerListSetting.class, ScreenHandlerSettingScreen::new);
        register(PotionSetting.class, this::potionW);
        register(StringListSetting.class, this::stringListW);
        register(BlockPosSetting.class, this::blockPosW);
        register(ColorListSetting.class, this::colorListW);
        register(FontFaceSetting.class, this::fontW);
        register(Vector3dSetting.class, this::vector3dW);
        register(KeyboardHud.CustomKeyListSetting.class, this::customKeyListW);

        // this requires direct factories.put() due to java generics inference limitations with inbred type parameters
        factories.put(EnumSetting.class, (table, setting) -> enumWRaw(table, (EnumSetting) setting));
        factories.put(GenericSetting.class, (table, setting) -> genericWRaw(table, (GenericSetting) setting));
        factories.put(BlockDataSetting.class, (table, setting) -> blockDataWRaw(table, (BlockDataSetting) setting));
    }

    // my life was better before i used java

    private <T extends Setting<?>> void register(Class<? extends T> settingClass, WidgetBuilder<T> builder) {
        factories.put(settingClass, (table, setting) -> builder.create(table, settingClass.cast(setting)));
    }

    private <T extends Setting<?>> void registerSelectScreenSetting(Class<? extends T> settingClass, SelectScreenFactory<T> screenFactory) {
        register(settingClass, (table, setting) -> selectW(table, setting, () -> openScreen(screenFactory.create(theme, setting))));
    }

    @Override
    public WWidget create(GuiTheme theme, Settings settings, String filter) {
        WVerticalList list = theme.verticalList();
        if (theme instanceof BaseGuiTheme baseTheme) list.spacing = baseTheme.separatorPaddingY.get();

        for (SettingGroup group : settings.groups) {
            group(list, group, filter);
        }

        list.calculateSize();
        list.minWidth = list.width;

        return list;
    }

    private void group(WVerticalList list, SettingGroup group, String filter) {
        double padding = moduleSettingsPaddingX();
        double separatorPadding = separatorPaddingY();
        WSection section = list.add(theme.section(group.name, group.sectionExpanded)).expandX().padHorizontal(padding).widget();
        section.spacing = 0;
        section.action = () -> group.sectionExpanded = section.isExpanded();

        WTable table = section.add(theme.table()).expandX().padHorizontal(padding).padTop(separatorPadding).widget();
        table.verticalSpacing = itemSpacing();

        for (Setting<?> setting : group) {
            if (!Strings.CI.contains(setting.title, filter)) continue;

            boolean visible = setting.isVisible();
            setting.lastWasVisible = visible;
            if (!visible) continue;

            Factory factory = getFactory(setting.getClass());
            if (factory != null) factory.create(table, setting);
            else {
                WButton button = theme.button(GuiRenderer.EDIT);
                button.action = () -> SettingScreenResolver.tryOpenConventional(theme, setting);
                if (SettingScreenResolver.canOpenConventional(setting)) addControlRow(table, setting, button);
            }
        }

        if (table.cells.isEmpty() && !list.cells.isEmpty()) list.remove(list.cells.get(list.cells.size() - 1));
    }

    private void boolW(WTable table, BoolSetting setting) {
        addRow(table, new WBaseSettingToggle(setting));
    }

    private void intW(WTable table, IntSetting setting) {
        if (!setting.noSlider && setting.sliderMin != setting.sliderMax) {
            addRow(table, new WBaseSettingSlider(setting));
            return;
        }

        WTextBox textBox = CompactNumberTextBoxes.create(theme, Integer.toString(setting.get()), CompactNumberTextBoxes::isIntChar, box -> {
            try {
                int value = Integer.parseInt(box.get().trim());
                setting.set(value);
            } catch (NumberFormatException ignored) {
            }
            box.set(Integer.toString(setting.get()));
        });

        addControlRow(table, setting, textBox);
    }

    private void doubleW(WTable table, DoubleSetting setting) {
        if (!setting.noSlider && setting.sliderMin != setting.sliderMax) {
            addRow(table, new WBaseSettingSlider(setting));
            return;
        }

        WTextBox textBox = CompactNumberTextBoxes.create(theme, String.format(Locale.US, "%." + setting.decimalPlaces + "f", setting.get()), CompactNumberTextBoxes::isDoubleChar, box -> {
            try {
                double value = Double.parseDouble(box.get().trim());
                setting.set(value);
            } catch (NumberFormatException ignored) {
            }
            box.set(String.format(Locale.US, "%." + setting.decimalPlaces + "f", setting.get()));
        });

        addControlRow(table, setting, textBox);
    }

    private void stringW(WTable table, StringSetting setting) {
        CharFilter filter = setting.filter == null ? (text, c) -> true : setting.filter;
        WTextBox textBox = theme.textBox(setting.get(), setting.placeholder, filter, setting.renderer);
        textBox.minWidth = setting.wide ? Utils.getWindowWidth() - Utils.getWindowWidth() / 4.0 : 75;
        textBox.action = () -> setting.set(textBox.get());
        addControlRow(table, setting, textBox);
    }

    private void stringListW(WTable table, StringListSetting setting) {
        WTable nested = theme.table();
        StringListSetting.fillTable(theme, nested, setting);
        addControlRow(table, setting.title, setting.description, nested, true);
    }

    private <T extends Enum<?>> void enumW(WTable table, EnumSetting<T> setting) {
        WDropdown<T> dropdown = theme.dropdown(setting.get());
        dropdown.action = () -> setting.set(dropdown.get());
        addControlRow(table, setting, dropdown);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void enumWRaw(WTable table, EnumSetting setting) {
        enumW(table, setting);
    }

    private void providedStringW(WTable table, ProvidedStringSetting setting) {
        WDropdown<String> dropdown = theme.dropdown(setting.supplier.get(), setting.get());
        dropdown.action = () -> setting.set(dropdown.get());
        addControlRow(table, setting, dropdown);
    }

    private void genericW(WTable table, GenericSetting<?> setting) {
        WButton button = theme.button(GuiRenderer.EDIT);
        button.action = () -> openScreen(setting.createScreen(theme));
        addControlRow(table, setting, button);
    }

    @SuppressWarnings("rawtypes")
    private void genericWRaw(WTable table, GenericSetting setting) {
        genericW(table, setting);
    }

    private void colorW(WTable table, ColorSetting setting) {
        WBaseColorButton colorButton = new WBaseColorButton(setting.get());
        addControlRow(table, setting, colorButton);

        colorButton.action = () -> {
            ExeterColorSettingScreen screen = new ExeterColorSettingScreen(theme, setting);
            screen.onClosed(() -> colorButton.color = setting.get());
            openScreen(screen);
        };
    }

    private void keybindW(WTable table, KeybindSetting setting) {
        WKeybind keybind = theme.keybind(setting.get(), setting.getDefaultValue());
        keybind.action = setting::onChanged;
        setting.widget = keybind;
        addControlRow(table, setting, keybind);
    }

    private void blockW(WTable table, BlockSetting setting) {
        WHorizontalList list = theme.horizontalList();
        WItem item = list.add(theme.item(setting.get().asItem().getDefaultStack())).widget();

        WButton select = list.add(theme.button("Select")).widget();
        select.action = () -> {
            BlockSettingScreen screen = new BlockSettingScreen(theme, setting);
            screen.onClosed(() -> item.set(setting.get().asItem().getDefaultStack()));
            openScreen(screen);
        };

        addControlRow(table, setting, list);
    }

    private void blockPosW(WTable table, BlockPosSetting setting) {
        WBaseBlockPosEdit edit = new WBaseBlockPosEdit(setting.get());
        edit.actionOnRelease = () -> {
            if (!setting.set(edit.get())) edit.set(setting.get());
        };
        addControlRow(table, setting.title, setting.description, edit, true);
    }

    private void itemW(WTable table, ItemSetting setting) {
        WHorizontalList list = theme.horizontalList();
        WItem item = list.add(theme.item(setting.get().asItem().getDefaultStack())).widget();

        WButton select = list.add(theme.button("Select")).widget();
        select.action = () -> {
            ItemSettingScreen screen = new ItemSettingScreen(theme, setting);
            screen.onClosed(() -> item.set(setting.get().getDefaultStack()));
            openScreen(screen);
        };

        addControlRow(table, setting, list);
    }

    private void blockDataW(WTable table, BlockDataSetting<?> setting) {
        WButton button = theme.button(GuiRenderer.EDIT);
        button.action = () -> openScreen(new BlockDataSettingScreen<>(theme, setting));
        addControlRow(table, setting, button);
    }

    @SuppressWarnings("rawtypes")
    private void blockDataWRaw(WTable table, BlockDataSetting setting) {
        blockDataW(table, setting);
    }

    private void potionW(WTable table, PotionSetting setting) {
        WHorizontalList list = theme.horizontalList();
        WItemWithLabel item = list.add(theme.itemWithLabel(setting.get().potion, I18n.translate(setting.get().potion.getItem().getTranslationKey()))).widget();

        WButton button = list.add(theme.button("Select")).widget();
        button.action = () -> {
            WidgetScreen screen = new PotionSettingScreen(theme, setting);
            screen.onClosed(() -> item.set(setting.get().potion));
            openScreen(screen);
        };

        addControlRow(table, setting, list);
    }

    private void fontW(WTable table, FontFaceSetting setting) {
        WHorizontalList list = theme.horizontalList();
        WLabel label = list.add(theme.label(setting.get().info.family())).widget();

        WButton button = list.add(theme.button("Select")).widget();
        button.action = () -> {
            WidgetScreen screen = new FontFaceSettingScreen(theme, setting);
            screen.onClosed(() -> label.set(setting.get().info.family()));
            openScreen(screen);
        };

        addControlRow(table, setting, list);
    }

    private void colorListW(WTable table, ColorListSetting setting) {
        WTable tab = theme.table();
        WTable t = tab.add(theme.table()).expandX().widget();
        tab.row();
        colorListWFill(t, setting);

        WPlus add = tab.add(theme.plus()).expandCellX().widget();
        add.action = () -> {
            setting.get().add(new SettingColor());
            setting.onChanged();
            t.clear();
            colorListWFill(t, setting);
        };

        addControlRow(table, setting, tab);
    }

    private void colorListWFill(WTable t, ColorListSetting setting) {
        int i = 0;
        for (SettingColor color : setting.get()) {
            int index = i;

            t.add(theme.label(i + ":"));
            WBaseColorButton colorButton = t.add(new WBaseColorButton(color)).widget();
            colorButton.action = () -> {
                SettingColor defaultValue = index < setting.getDefaultValue().size() ? setting.getDefaultValue().get(index) : new SettingColor();

                ColorSetting set = new ColorSetting(setting.name, setting.description, defaultValue, settingColor -> {
                    setting.get().get(index).set(settingColor);
                    setting.onChanged();
                }, null, null);
                set.set(setting.get().get(index));
                ExeterColorSettingScreen screen = new ExeterColorSettingScreen(theme, set);
                screen.onClosed(() -> colorButton.color = setting.get().get(index));
                openScreen(screen);
            };

            WMinus remove = t.add(theme.minus()).expandCellX().right().widget();
            remove.action = () -> {
                setting.get().remove(index);
                setting.onChanged();
                t.clear();
                colorListWFill(t, setting);
            };

            t.row();
            i++;
        }
    }

    private void vector3dW(WTable table, Vector3dSetting setting) {
        addVectorComponent(table, setting, "X", () -> setting.get().x, val -> setting.get().x = val);
        addVectorComponent(table, setting, "Y", () -> setting.get().y, val -> setting.get().y = val);
        addVectorComponent(table, setting, "Z", () -> setting.get().z, val -> setting.get().z = val);
    }

    private void addVectorComponent(WTable table, Vector3dSetting setting, String axis, Supplier<Double> getter, Consumer<Double> setter) {
        if (!setting.noSlider && setting.sliderMin != setting.sliderMax) {
            addRow(table, new WBaseSettingSlider(createVectorComponentSetting(setting, axis, getter, setter)));
            return;
        }

        WTextBox textBox = CompactNumberTextBoxes.create(theme, String.format(Locale.US, "%." + setting.decimalPlaces + "f", getter.get()), CompactNumberTextBoxes::isDoubleChar, box -> {
            try {
                setter.accept(Double.parseDouble(box.get().trim()));
                setting.onChanged();
            } catch (NumberFormatException ignored) {
            }

            box.set(String.format(Locale.US, "%." + setting.decimalPlaces + "f", getter.get()));
        });
        addControlRow(table, setting.title + " " + axis, setting.description, textBox);
    }

    private DoubleSetting createVectorComponentSetting(Vector3dSetting parent, String axis, Supplier<Double> getter, Consumer<Double> setter) {
        DoubleSetting.Builder builder = new DoubleSetting.Builder()
            .name(parent.name + "-" + axis.toLowerCase(Locale.ROOT))
            .description(parent.description)
            .defaultValue(getter.get())
            .range(parent.min, parent.max)
            .sliderRange(parent.sliderMin, parent.sliderMax)
            .decimalPlaces(parent.decimalPlaces)
            .onChanged(value -> {
                setter.accept(value);
                parent.onChanged();
            });

        return builder.build();
    }

    private void customKeyListW(WTable table, KeyboardHud.CustomKeyListSetting setting) {
        WTable nested = theme.table();
        KeyboardHud.fillTable(theme, nested, setting);
        addControlRow(table, setting, nested);
    }

    private void selectW(WTable table, Setting<?> setting, Runnable action) {
        WContainer container = theme.horizontalList();

        WButton button = container.add(theme.button("Select")).widget();
        button.action = action;

        addControlRow(table, setting, container);
    }

    private void addControlRow(WTable table, Setting<?> setting, WWidget control) {
        addControlRow(table, setting.title, setting.description, control);
    }

    private void addControlRow(WTable table, String title, String description, WWidget control) {
        addControlRow(table, title, description, control, false);
    }

    private void addControlRow(WTable table, String title, String description, WWidget control, boolean forceVerticalLayout) {
        addRow(table, new WBaseSettingControlRow(title, description, control, forceVerticalLayout));
    }

    private void addRow(WTable table, WWidget rowWidget) {
        table.add(rowWidget).expandX();
        table.row();
    }

    private void openScreen(WidgetScreen screen) {
        if (theme instanceof BaseGuiTheme baseTheme && !baseTheme.modalWindows.get()) {
            mc.setScreen(screen);
        } else {
            ModalScreenOps.open(screen);
        }
    }

    private double itemSpacing() {
        BaseGuiTheme baseTheme = baseTheme();
        return baseTheme != null ? baseTheme.itemSpacingY.get() : 0;
    }

    private BaseGuiTheme baseTheme() {
        return theme instanceof BaseGuiTheme baseTheme ? baseTheme : null;
    }

    private double moduleSettingsPaddingX() {
        BaseGuiTheme baseTheme = baseTheme();
        return baseTheme != null ? baseTheme.settingsPaddingX.get() : 6;
    }

    private double separatorPaddingY() {
        BaseGuiTheme baseTheme = baseTheme();
        return baseTheme != null ? baseTheme.separatorPaddingY.get() : 3;
    }

    @FunctionalInterface
    private interface WidgetBuilder<T extends Setting<?>> {
        void create(WTable table, T setting);
    }

    @FunctionalInterface
    private interface SelectScreenFactory<T extends Setting<?>> {
        WidgetScreen create(GuiTheme theme, T setting);
    }
}
