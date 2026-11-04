package me.sophimoo.exeter.gui.screens;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import me.sophimoo.exeter.gui.themes.base.utils.ModuleBindUtils;
import me.sophimoo.exeter.gui.widgets.ExeterCustomWidgetHost;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;

public class ModuleSettingsScreen extends WindowScreen {
    private final Module module;
    private final BaseGuiTheme baseTheme;
    private WKeybind keybindWidget;
    private WContainer settingsContainer;

    public ModuleSettingsScreen(GuiTheme theme, Module module) {
        super(theme, module.title);
        this.module = module;
        this.baseTheme = theme instanceof BaseGuiTheme bt ? bt : null;
    }

    @Override
    public void initWidgets() {
        WVerticalList description = add(theme.verticalList()).expandX()
            .pad(baseTheme != null ? baseTheme.globalPadding.get() : 0).widget();

        if (module.addon != null && module.addon != MeteorClient.ADDON) {
            WHorizontalList addon = add(theme.horizontalList()).expandX().widget();
            addon.add(theme.label("From: ").color(theme.textSecondaryColor()));
            addon.add(theme.label(module.addon.name).color(module.addon.color));
        }

        WVerticalList wrapper = theme.verticalList();
        if (baseTheme != null) wrapper.spacing = BaseGuiTheme.SEPARATOR_PADDING_Y;
        settingsContainer = wrapper.add(theme.verticalList()).expandX().widget();
        settingsContainer.add(theme.settings(module.settings)).expandX();

        WWidget customWidget = module.getWidget(theme);
        if (customWidget != null) {
            WWidget hostedCustomWidget = new ExeterCustomWidgetHost(customWidget);
            double padX = baseTheme != null ? BaseGuiTheme.ITEM_PADDING_X : 0;
            wrapper.add(theme.horizontalSeparator()).expandX().padHorizontal(padX);
            Cell<WWidget> cell = wrapper.add(hostedCustomWidget);
            if (customWidget instanceof WContainer) cell.expandX();
        }

        addBindSection(wrapper);
        add(wrapper).expandX();

        window.calculateSize();
        window.calculateWidgetPositions();
        description.add(theme.label(module.description, Math.max(1, description.width / theme.scale(1)))).expandX();

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
        Cell<WSection> bindSectionCell = container.add(theme.section("Bind", true))
            .expandX();

        keybindWidget = ModuleBindUtils.populateBindSection(bindSectionCell, module, theme);
        keybindWidget.actionOnSet = () -> Modules.get().setModuleToBind(module);
    }
}
