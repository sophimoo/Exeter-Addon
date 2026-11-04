package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.screens.BaseModulesScreen;
import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.utils.ModuleBindUtils;
import me.sophimoo.exeter.gui.widgets.ExeterCustomWidgetHost;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import me.sophimoo.exeter.gui.themes.base.utils.enums.SelectionRenderingMode;
import meteordevelopment.orbit.EventHandler;

public class WBaseModule extends WVerticalList implements BaseWidget {
    private final Module module;
    private final String title;

    private boolean settingsExpanded = false;
    private WSettingsDropdown settingsContainer = null;
    private WModuleButton moduleButton;
    private WContainer moduleSettingsContainer = null;
    private WKeybind keybindWidget = null;
    private boolean subscribedToEvents;
    private int settingsTickCooldown;
    private double inlineDimmingProgress;

    public WBaseModule(Module module, String title) {
        this.module = module;
        this.title = title;
        this.spacing = 0;
    }

    @Override
    public void init() {
        moduleButton = add(new WModuleButton()).expandX().widget();
    }

    private void toggleSettings() {
        settingsExpanded = !settingsExpanded;

        if (settingsContainer == null) {
            settingsContainer = add(new WSettingsDropdown()).expandX().widget();
            settingsContainer.content.spacing = BaseGuiTheme.SEPARATOR_PADDING_Y;

            if (module.settings.groups.stream().anyMatch(group -> group.iterator().hasNext())) {
                moduleSettingsContainer = settingsContainer.add(theme.verticalList()).expandX().widget();
                moduleSettingsContainer.add(theme.settings(module.settings)).expandX();
            }

            WWidget customWidget = module.getWidget(theme);
            if (customWidget != null) {
                WWidget hostedCustomWidget = new ExeterCustomWidgetHost(customWidget);
                settingsContainer.add(theme.horizontalSeparator()).expandX();
                Cell<WWidget> customCell = settingsContainer.add(hostedCustomWidget);
                if (customWidget instanceof WContainer) customCell.expandX();
            }

            addBindSection(settingsContainer);
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

    public double settingsExpansionProgress() {
        return settingsContainer == null ? 0 : dropdownHeightProgress(settingsContainer.animProgress, settingsExpanded);
    }

    public boolean isSettingsHovered(double mouseX, double mouseY) {
        if (!settingsExpanded || settingsContainer == null || !settingsContainer.isOver(mouseX, mouseY)) return false;

        WView view = settingsContainer.getView();
        return view == null || view.isOver(mouseX, mouseY);
    }

    double effectiveInlineDimmingProgress() {
        return inlineDimmingProgress * theme().darkAmount.get();
    }

    @Override
    public boolean render(meteordevelopment.meteorclient.gui.renderer.GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        if (!visible) return true;

        BaseModulesScreen modulesScreen = MeteorClient.mc.currentScreen instanceof BaseModulesScreen screen ? screen : null;
        boolean dim = modulesScreen != null && modulesScreen.shouldDimForInlineSettings(this);
        inlineDimmingProgress = stepAnimationProgress(
            inlineDimmingProgress,
            dim,
            delta,
            theme().hoverInSpeed.get(),
            theme().hoverOutSpeed.get()
        );

        boolean result = super.render(renderer, mouseX, mouseY, delta);
        if (modulesScreen != null) modulesScreen.queueDimmingOverlay(renderer, this, effectiveInlineDimmingProgress());
        return result;
    }

    private void addBindSection(WContainer container) {
        Cell<WSection> bindSectionCell = container.add(theme.section("Bind", false)).expandX();

        keybindWidget = ModuleBindUtils.populateBindSection(bindSectionCell, module, theme);
        keybindWidget.actionOnSet = () -> {
            Modules.get().setModuleToBind(module);
            if (MeteorClient.mc.currentScreen instanceof BaseModulesScreen screen) {
                screen.unfocusSearchTextBox();
            }
        };
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
            if (module.addon != null && module.addon != MeteorClient.ADDON) {
                tooltip += "\nFrom: " + module.addon.name;
            }
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

    private class WSettingsDropdown extends WBaseSettingsPanel {
        private boolean expanded;
        private double animProgress;
        private double expandedHeight;
        private double cachedExpandedHeight = -1;
        private double cachedExpandedWidth = -1;
        private double collapsedWidth;

        public void setExpanded(boolean expanded) {
            if (this.expanded == expanded) return;
            if (expanded && animProgress <= 0) collapsedWidth = WBaseModule.this.width;
            this.expanded = expanded;
            if (expanded) settingsTickCooldown = 0;
        }

        public boolean isFullyExpanded() {
            return animProgress >= 1;
        }

        @Override
        public void calculateSize() {
            outlinePadding = theme().moduleSettingsOutlineThickness.get();
            insetBottom = outlinePadding > 0;
            boolean animating = animProgress > 0 && animProgress < 1;
            double heightProgress = dropdownHeightProgress(animProgress, expanded);

            if (!animating || cachedExpandedHeight < 0 || cachedExpandedWidth < 0) {
                super.calculateSize();
                cachedExpandedWidth = width;
                cachedExpandedHeight = height;
            } else {
                super.calculateSize();
                width = cachedExpandedWidth;
                height = cachedExpandedHeight;
            }

            expandedHeight = cachedExpandedHeight;
            height = Math.round(expandedHeight * heightProgress);
            double expandedWidth = Math.max(collapsedWidth, cachedExpandedWidth);
            width = Math.round(collapsedWidth + (expandedWidth - collapsedWidth) * heightProgress);
        }

        @Override
        public boolean render(meteordevelopment.meteorclient.gui.renderer.GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!visible) return true;

            double previousAnimProgress = animProgress;
            animProgress = stepProgress(animProgress, expanded, delta);

            double heightProgress = dropdownHeightProgress(animProgress, expanded);
            double animatedHeight = expandedHeight * heightProgress;
            boolean animationChanged = previousAnimProgress != animProgress;

            if (animationChanged) invalidate();
            if (animProgress <= 0) return false;

            boolean scissor = animProgress != 1;
            if (scissor) renderer.scissorStart(x, y, width, animatedHeight);

            boolean toReturn = super.render(renderer, mouseX, mouseY, delta);

            if (scissor) renderer.scissorEnd();

            double outlineY = moduleButton.y + moduleButton.height;
            double outlineHeight = Math.max(0, y - outlineY) + Math.min(height, animatedHeight);
            double thickness = Math.min(Math.round(theme.scale(theme().moduleSettingsOutlineThickness.get())), Math.min(width, outlineHeight) / 2);
            if (thickness > 0) {
                renderRowSurface(renderer, x, outlineY, width, outlineHeight, SelectionRenderingMode.FADE,
                    moduleButton.animationProgress, localHoverSurfaceProgress(moduleButton.hoverOverlayProgress),
                    moduleRowSurfaceStyle(module.isActive(), false, moduleButton.mouseOver, null), thickness);
            }

            return toReturn;
        }

        @Override
        protected boolean propagateEvents(WWidget widget) {
            return expanded;
        }
    }
}
