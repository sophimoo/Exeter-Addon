package me.sophimoo.exeter.mixin.meteorclient;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.settings.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "meteordevelopment.meteorclient.gui.tabs.builtin.GuiTab$GuiScreen", remap = false)
public abstract class GuiTabScreenMixin extends WindowTabScreen {
    @Unique
    private WVerticalList exeter$settingsContainer;

    protected GuiTabScreenMixin(GuiTheme theme, Tab tab) {
        super(theme, tab);
    }

    @Redirect(method = "initWidgets", at = @At(value = "INVOKE",
        target = "Lmeteordevelopment/meteorclient/gui/GuiTheme;settings(Lmeteordevelopment/meteorclient/settings/Settings;)Lmeteordevelopment/meteorclient/gui/widgets/WWidget;"))
    private WWidget exeter$wrapSettings(GuiTheme theme, Settings settings) {
        WWidget widget = theme.settings(settings);
        if (!(theme instanceof BaseGuiTheme)) return widget;

        // Settings.tick replaces the generated widget, not the tab's other controls.
        exeter$settingsContainer = theme.verticalList();
        exeter$settingsContainer.add(widget).expandX();
        return exeter$settingsContainer;
    }

    @Override
    public void tick() {
        super.tick();
        if (exeter$settingsContainer != null) {
            theme.settings.tick(exeter$settingsContainer, theme);
        }
    }
}
