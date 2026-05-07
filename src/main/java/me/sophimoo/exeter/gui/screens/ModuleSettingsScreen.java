package me.sophimoo.exeter.gui.screens;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import me.sophimoo.exeter.gui.themes.base.utils.ModuleBindUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;

public class ModuleSettingsScreen extends WindowScreen {
    private final Module module;
    private final BaseGuiTheme baseTheme;
    private WKeybind keybindWidget;
    private WContainer settingsContainer;

    public ModuleSettingsScreen(GuiTheme theme, Module module) {
        super(theme, module.name);
        this.module = module;
        this.baseTheme = theme instanceof BaseGuiTheme bt ? bt : null;
    }

    @Override
    public void initWidgets() {
        WVerticalList wrapper = new WVerticalList();
        wrapper.spacing = 0;
        wrapper.theme = theme;

        WWidget settingsWidget = theme.settings(module.settings);
        if (settingsWidget instanceof WContainer swContainer && !swContainer.cells.isEmpty()) {
            settingsContainer = swContainer;
            wrapper.add(settingsWidget).expandX();
        }

        addBindSection(wrapper);
        add(wrapper).expandX();
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (settingsContainer != null) {
            module.settings.tick(settingsContainer, theme);
        }
    }

    @Override
    public void removed() {
        MeteorClient.EVENT_BUS.unsubscribe(this);
        super.removed();
    }

    @EventHandler
    private void onModuleBindChanged(ModuleBindChangedEvent event) {
        if (event.module == module && keybindWidget != null) {
            keybindWidget.reset();
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !Modules.get().isBinding();
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        if (Modules.get().isBinding()) return false;
        return super.keyPressed(input);
    }

    private void addBindSection(WVerticalList container) {
        double paddingX = baseTheme != null ? baseTheme.moduleSettingsPaddingX.get() : 6;
        double separatorPaddingY = baseTheme != null ? baseTheme.scale(baseTheme.separatorPaddingY.get()) : 6;
        double itemSpacing = baseTheme != null ? baseTheme.itemSpacing.get() : 0;

        Cell<WSection> bindSectionCell = container.add(theme.section("Bind", true))
            .expandX()
            .padTop(separatorPaddingY)
            .padHorizontal(paddingX)
            .padBottom(separatorPaddingY);

        keybindWidget = ModuleBindUtils.populateBindSection(bindSectionCell, paddingX, separatorPaddingY, itemSpacing, module, theme);
        keybindWidget.actionOnSet = () -> Modules.get().setModuleToBind(module);
    }
}
