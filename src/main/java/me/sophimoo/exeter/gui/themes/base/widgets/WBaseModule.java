package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.screens.BaseModulesScreen;
import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.ModuleBindUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.MathHelper;

public class WBaseModule extends WVerticalList implements BaseWidget {
    private final Module module;
    private final String title;

    private boolean settingsExpanded = false;
    private WSettingsDropdown settingsContainer = null;
    private Cell<WSettingsDropdown> settingsContainerCell = null;
    private WContainer moduleSettingsContainer = null;
    private boolean hasModuleSettingsContent;
    private WKeybind keybindWidget = null;
    private boolean subscribedToEvents;
    private int settingsTickCooldown;

    public WBaseModule(Module module, String title) {
        this.module = module;
        this.title = title;
        this.spacing = 0;
    }

    @Override
    public void init() {
        add(new WModuleButton()).expandX();
    }

    private void toggleSettings() {
        settingsExpanded = !settingsExpanded;

        if (settingsContainer == null) {
            double paddingY = (theme instanceof BaseGuiTheme baseTheme) ? baseTheme.scale(baseTheme.separatorPaddingY.get()) : 6;

            settingsContainer = new WSettingsDropdown();
            settingsContainer.theme = theme;
            settingsContainer.spacing = 0;

            moduleSettingsContainer = settingsContainer.add(theme.verticalList()).expandX().widget();

            if (moduleSettingsContainer instanceof WVerticalList verticalList) verticalList.spacing = 0;

            WWidget settingsWidget = theme.settings(module.settings);

            hasModuleSettingsContent = hasSettingsContent(settingsWidget);

            moduleSettingsContainer.add(settingsWidget).expandX();

            addBindSection(settingsContainer);

            settingsContainerCell = add(settingsContainer).expandX().padTop(0);
        }

        settingsContainer.setExpanded(settingsExpanded);
        if (MeteorClient.mc.currentScreen instanceof BaseModulesScreen modulesScreen) {
            modulesScreen.requestExpandedModulesRefresh();
        }

        if (settingsExpanded && keybindWidget != null) keybindWidget.reset();
        setBindEventSubscription(settingsExpanded);
    }

    private void setBindEventSubscription(boolean subscribe) {
        if (subscribe == subscribedToEvents) return;

        if (subscribe) MeteorClient.EVENT_BUS.subscribe(this);
        else MeteorClient.EVENT_BUS.unsubscribe(this);

        subscribedToEvents = subscribe;
    }

    public void tickSettings() {
        if (settingsExpanded && moduleSettingsContainer != null && settingsContainer != null && settingsContainer.isFullyExpanded()) {
            if (settingsTickCooldown > 0) {
                settingsTickCooldown--;
                return;
            }

            module.settings.tick(moduleSettingsContainer, theme);
            settingsTickCooldown = 1;
        }
    }

    public boolean isSettingsExpanded() {
        return settingsExpanded;
    }

    private void addBindSection(WVerticalList container) {
        double paddingX = (theme instanceof BaseGuiTheme baseTheme) ? baseTheme.settingsPaddingX.get() : 6;
        double separatorPaddingY = (theme instanceof BaseGuiTheme baseTheme) ? baseTheme.scale(baseTheme.separatorPaddingY.get()) : 6;
        double itemSpacing = (theme instanceof BaseGuiTheme baseTheme) ? baseTheme.itemSpacingY.get() : 0;

        Cell<WSection> bindSectionCell = container.add(theme.section("Bind", false)).expandX().padHorizontal(paddingX).padBottom(separatorPaddingY);
        if (hasModuleSettingsContent) bindSectionCell.padTop(separatorPaddingY);

        keybindWidget = ModuleBindUtils.populateBindSection(bindSectionCell, paddingX, separatorPaddingY, itemSpacing, module, theme);
        keybindWidget.actionOnSet = () -> {
            Modules.get().setModuleToBind(module);
            if (MeteorClient.mc.currentScreen instanceof BaseModulesScreen screen) {
                screen.unfocusSearchTextBox();
            }
        };
    }

    private boolean hasSettingsContent(WWidget settingsWidget) {
        if (settingsWidget instanceof WContainer container) return !container.cells.isEmpty();
        return true;
    }

    @EventHandler
    private void onModuleBindChanged(ModuleBindChangedEvent event) {
        if (event.module == module && keybindWidget != null) {
            keybindWidget.reset();
        }
    }

    private class WModuleButton extends WBaseModuleRow {
        public WModuleButton() {
            super(WBaseModule.this.module, WBaseModule.this.title);
        }

        @Override
        protected boolean isSettingsExpanded() {
            return WBaseModule.this.settingsExpanded;
        }

        @Override
        protected void onRightClick() {
            WBaseModule.this.toggleSettings();
        }
    }

    private class WSettingsDropdown extends WVerticalList {
        private boolean expanded;
        private double animProgress;
        private double expandedHeight;
        private double cachedExpandedHeight = -1;
        private double cachedExpandedWidth = -1;

        public void setExpanded(boolean expanded) {
            if (this.expanded == expanded) return;
            this.expanded = expanded;
            if (expanded) settingsTickCooldown = 0;
        }

        public boolean isFullyExpanded() {
            return animProgress >= 1;
        }

        @Override
        public void calculateSize() {
            boolean animating = animProgress > 0 && animProgress < 1;
            double heightProgress = dropdownHeightProgress(animProgress, expanded);

            if (!animating || cachedExpandedHeight < 0 || cachedExpandedWidth < 0) {
                super.calculateSize();
                cachedExpandedWidth = width;
                cachedExpandedHeight = height;
            } else {
                width = cachedExpandedWidth;
                height = cachedExpandedHeight;
            }

            expandedHeight = cachedExpandedHeight;
            height = Math.round(expandedHeight * heightProgress);
        }

        @Override
        public boolean render(meteordevelopment.meteorclient.gui.renderer.GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!visible) return true;

            double previousAnimProgress = animProgress;
            animProgress = stepProgress(animProgress, expanded, delta);

            double heightProgress = dropdownHeightProgress(animProgress, expanded);
            double animatedHeight = expandedHeight * heightProgress;
            boolean animationChanged = previousAnimProgress != animProgress;
            if (settingsContainerCell != null) {
                int padTopPx = (heightProgress > 0) ? MathHelper.floor(theme().scale(theme().separatorPaddingY.get()) + 0.5) : 0;
                if (settingsContainerCell.padTop() != padTopPx) {
                    settingsContainerCell.padTop(padTopPx);
                    animationChanged = true;
                }
            }

            if (animationChanged) invalidate();
            if (animProgress <= 0) return false;

            boolean scissor = animProgress != 1;
            if (scissor) renderer.scissorStart(x, y, width, animatedHeight);
            boolean toReturn = super.render(renderer, mouseX, mouseY, delta);
            if (scissor) renderer.scissorEnd();

            return toReturn;
        }

        @Override
        protected boolean propagateEvents(WWidget widget) {
            return expanded;
        }
    }
}
