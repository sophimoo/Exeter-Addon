package me.sophimoo.exeter.gui.themes.base;

import me.sophimoo.exeter.gui.renderer.WorldFramebufferCapture;
import me.sophimoo.exeter.gui.themes.base.utils.*;
import me.sophimoo.exeter.gui.themes.base.utils.enums.*;
import me.sophimoo.exeter.gui.themes.base.widgets.*;
import me.sophimoo.exeter.gui.themes.base.widgets.input.WBaseDropdown;
import me.sophimoo.exeter.gui.themes.base.widgets.input.WBaseSlider;
import me.sophimoo.exeter.gui.themes.base.widgets.input.WBaseTextBox;
import me.sophimoo.exeter.gui.themes.base.widgets.pressable.*;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.AlignmentY;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.*;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.*;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.listeners.ConsumerListener;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BaseGuiTheme extends GuiTheme {
    // Blur capture instance - managed by the theme
    private WorldFramebufferCapture blurCapture;

    // Interpolation tracking - per-widget hover highlight
    private final java.util.WeakHashMap<Object, InterpolationState> interpolationStates = new java.util.WeakHashMap<>();

    public InterpolationState getInterpolation(Object key) {
        return interpolationStates.computeIfAbsent(key, k -> new InterpolationState());
    }

    private final List<Setting<SettingColor>> accentHueLinkedColors = new ArrayList<>();
    private boolean updatingAccentHueLinkedColors;
    private float lastAppliedAccentHue = Float.NaN;
    private final ConsumerListener<TickEvent.Post> accentHueTickListener;

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgTextColors = settings.createGroup("Text");
    private final SettingGroup sgBackgroundColors = settings.createGroup("Background");
    private final SettingGroup sgOutline = settings.createGroup("Outline");
    private final SettingGroup sgScrollbar = settings.createGroup("Scrollbar");
    private final SettingGroup sgSlider = settings.createGroup("Slider");
    private final SettingGroup sgBlur = settings.createGroup("Blur");
    private final SettingGroup sgModuleAnimation = settings.createGroup("Module Animation");
    private final SettingGroup sgModuleRender = settings.createGroup("Module Rendering");
    private final SettingGroup sgModuleColor = settings.createGroup("Module Colors");
    private final SettingGroup sgSeparator = settings.createGroup("Separator");
    private final SettingGroup sgSettingsColors = settings.createGroup("Settings");
    private final SettingGroup sgTextShadow = settings.createGroup("Text Shadow");
    private final SettingGroup sgStarscript = settings.createGroup("Starscript");
    // https://github.com/X-C-0/catppuccin-addon/blob/d642959fbaa9e5757013ea38f57556eb88c8b822/src/main/java/me/pindour/catppuccin/gui/themes/catppuccin/CatppuccinGuiTheme.java#L70
    private final SettingGroup sgSnapping = settings.createGroup("Snapping");


    // General

    public final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Scale of the GUI.")
        .defaultValue(1)
        .min(0.75)
        .sliderRange(0.75, 4)
        .onSliderRelease()
        .onChanged(aDouble -> {
            if (mc.currentScreen instanceof WidgetScreen) ((WidgetScreen) mc.currentScreen).invalidate();
        })
        .build()
    );

    public final Setting<AlignmentX> categoryAlignment = sgGeneral.add(new EnumSetting.Builder<AlignmentX>()
            .name("category-alignment")
            .description("How category lists are aligned horizontally.")
            .defaultValue(AlignmentX.Left)
            .onChanged(v -> invalidateCurrentScreen())
            .build()
    );

    public final Setting<AlignmentX> categoryTitleAlignment = sgGeneral.add(new EnumSetting.Builder<AlignmentX>()
        .name("category-title-alignment")
        .description("How category window titles are aligned.")
        .defaultValue(AlignmentX.Center)
        .onChanged(v -> invalidateCurrentScreen())
        .build()
    );

    public final Setting<Boolean> categoryIcons = sgGeneral.add(new BoolSetting.Builder()
        .name("category-icons")
        .description("Adds item icons to module categories.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> fixedCategorySize = sgGeneral.add(new BoolSetting.Builder()
        .name("fixed-category-size")
        .description("Forces category windows to a fixed width.")
        .defaultValue(false)
        .onChanged(v -> invalidateCurrentScreen())
        .build()
    );

    public final Setting<Integer> fixedCategoryWidth = sgGeneral.add(new IntSetting.Builder()
        .name("fixed-category-width")
        .description("Width used for category windows when fixed sizing is enabled.")
        .defaultValue(220)
        .range(1, 400)
        .sliderRange(1, 400)
        .onChanged(v -> invalidateCurrentScreen())
        .build()
    );

    public final Setting<Boolean> hideHUD = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-HUD")
        .description("Hide HUD when in GUI.")
        .defaultValue(false)
        .onChanged(v -> {
            if (mc.currentScreen instanceof WidgetScreen) mc.options.hudHidden = v;
        })
        .build()
    );

    public final Setting<Boolean> inlineModuleSettings = sgGeneral.add(new BoolSetting.Builder()
            .name("inline-module-settings")
            .description("Open settings in module list")
            .defaultValue(false)
            .onChanged(v -> invalidateCurrentScreen())
            .build()
    );

    public final Setting<Boolean> modalWindows = sgGeneral.add(new BoolSetting.Builder()
            .name("modal-windows")
            .description("Open elements in modal windows")
            .defaultValue(false)
            .build()
    );

    // Snapping
    // https://github.com/X-C-0/catppuccin-addon/blob/d642959fbaa9e5757013ea38f57556eb88c8b822/src/main/java/me/pindour/catppuccin/gui/themes/catppuccin/CatppuccinGuiTheme.java#L190

    public final Setting<Boolean> snapModuleCategories = sgSnapping.add(new BoolSetting.Builder()
        .name("snap-module-categories")
        .description("Snaps module screen windows to the grid.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Integer> snappingGridSize = sgSnapping.add(new IntSetting.Builder()
        .name("grid-size")
        .description("The size of the snapping grid.")
        .defaultValue(10)
        .range(5, 50)
        .sliderRange(5, 50)
        .build()
    );


    // Padding

    public final Setting<Double> globalPadding = sgGeneral.add(new DoubleSetting.Builder()
            .name("global-padding")
            .description("Global padding applied to all GUI elements.")
            .defaultValue(4)
            .min(0)
            .max(20)
            .sliderRange(0, 20)
            .onChanged(v -> invalidateCurrentScreen())
            .build()
    );

    public final Setting<Double> settingsPaddingX = sgGeneral.add(new DoubleSetting.Builder()
            .name("settings-padding-x")
            .description("Horizontal padding for settings menus.")
            .defaultValue(4)
            .min(0)
            .max(30)
            .sliderRange(0, 30)
            .onChanged(v -> invalidateCurrentScreen())
            .build()
    );

    public final Setting<Boolean> debugWidgetSizes = sgGeneral.add(new BoolSetting.Builder()
        .name("debug-widget-sizes")
        .description("Prints button and setting row sizes to the game log.")
        .defaultValue(false)
        .build()
    );

    // Gui

    public final Setting<Integer> widgetBlurStrength = sgBlur.add(new IntSetting.Builder()
        .name("gui-blur")
        .description("Blur strength behind gui. Higher values = more blur.")
        .defaultValue(0)
        .min(0)
        .max(5)
        .sliderRange(0, 5)
        .onChanged(v -> updateBlurCapture())
        .build()
    );

    public final Setting<Double> blurTextureScale = sgBlur.add(new DoubleSetting.Builder()
        .name("blur-texture-scale")
        .description("Scale of the blur texture. Lower values = blurrier but faster.")
        .defaultValue(0.25)
        .min(0.01)
        .max(1)
        .sliderRange(0.01, 1)
        .onChanged(v -> updateBlurCapture())
        .build()
    );

    public final Setting<Boolean> darkening = sgBlur.add(new BoolSetting.Builder()
            .name("darkening")
            .description("Darkens the background when the GUI is open.")
            .defaultValue(false)
            .build()
    );

    // Module animation

    public final Setting<ModuleAnimationMode> moduleAnimationMode = sgModuleAnimation.add(new EnumSetting.Builder<ModuleAnimationMode>()
        .name("module-animation-mode")
        .description("Animation style for module hover and active states.")
        .defaultValue(ModuleAnimationMode.SLIDE_LEFT)
        .build()
    );

    public final Setting<Double> moduleSelectSpeed = sgModuleAnimation.add(new DoubleSetting.Builder()
            .name("module-select-speed")
            .description("Speed of module select animation.")
            .defaultValue(6)
            .min(0)
            .max(32)
            .sliderRange(0, 32)
            .build()
    );

    public final Setting<Double> moduleDeselectSpeed = sgModuleAnimation.add(new DoubleSetting.Builder()
            .name("module-deselect-speed")
            .description("Speed of module deselect animation.")
            .defaultValue(4)
            .min(0)
            .max(32)
            .sliderRange(0, 32)
            .build()
    );

    // Module rendering

    public final Setting<AlignmentX> moduleAlignment = sgModuleRender.add(new EnumSetting.Builder<AlignmentX>()
            .name("module-title-alignment")
            .description("How module titles are aligned horizontally.")
            .defaultValue(AlignmentX.Center)
            .build()
    );

    public final Setting<AlignmentY> moduleAlignmentY = sgModuleRender.add(new EnumSetting.Builder<AlignmentY>()
            .name("module-vertical-alignment")
            .description("How module titles are aligned vertically.")
            .defaultValue(AlignmentY.Center)
            .build()
    );

    public final Setting<ModuleGradientDirection> moduleGradientDirection = sgModuleRender.add(new EnumSetting.Builder<ModuleGradientDirection>()
        .name("module-gradient-direction")
        .description("Gradient direction for active module background. 'None' uses solid color.")
        .defaultValue(ModuleGradientDirection.NONE)
        .build()
    );

    public final Setting<GradientApplicationMode> gradientApplicationMode = sgModuleRender.add(new EnumSetting.Builder<GradientApplicationMode>()
        .name("module-gradient-apply-to")
        .description("Which module states the gradient should be applied to.")
        .defaultValue(GradientApplicationMode.ACTIVE)
        .build()
    );

    public final Setting<ModuleIndicatorPosition> moduleIndicatorPosition = sgModuleRender.add(new EnumSetting.Builder<ModuleIndicatorPosition>()
        .name("module-indicator-position")
        .description("Position of the active module indicator bar. 'None' to disable.")
        .defaultValue(ModuleIndicatorPosition.LEFT)
        .build()
    );

    public final Setting<Double> moduleIndicatorThickness = sgModuleRender.add(new DoubleSetting.Builder()
        .name("module-indicator-thickness")
        .description("Thickness of the active module indicator bar.")
        .defaultValue(2)
        .min(1)
        .sliderRange(1, 8)
        .build()
    );

    public final Setting<Double> moduleOutlineThickness = sgModuleRender.add(new DoubleSetting.Builder()
            .name("module-outline-thickness")
            .description("Thickness of module outlines.")
            .defaultValue(0)
            .min(0)
            .max(5)
            .sliderRange(0, 5)
            .build()
    );

    public final Setting<Double> moduleSpacing = sgModuleRender.add(new DoubleSetting.Builder()
            .name("module-spacing")
            .description("Spacing between modules in module lists.")
            .defaultValue(0)
            .min(0)
            .max(10)
            .sliderRange(0, 10)
            .build()
    );

    public final Setting<Double> itemHeight = sgSettingsColors.add(new DoubleSetting.Builder()
            .name("item-height")
            .description("Height of settings items.")
            .defaultValue(0)
            .min(0)
            .max(50)
            .sliderRange(0, 50)
            .onChanged(v -> invalidateCurrentScreen())
            .build()
    );

    public final Setting<Double> itemSpacingY = sgSettingsColors.add(new DoubleSetting.Builder()
            .name("item-spacing-y")
            .description("Spacing between items in settings lists.")
            .defaultValue(0)
            .min(0)
            .max(10)
            .sliderRange(0, 10)
            .onChanged(v -> invalidateCurrentScreen())
            .build()
    );

    public final Setting<Double> moduleHeight = sgModuleRender.add(new DoubleSetting.Builder()
            .name("module-height")
            .description("Height of module buttons in module lists.")
            .defaultValue(0)
            .min(0)
            .max(50)
            .sliderRange(0, 50)
            .onChanged(v -> invalidateCurrentScreen())
            .build()
    );


    public final Setting<String> moduleCollapsedIndicator = sgModuleRender.add(new StringSetting.Builder()
        .name("module-collapsed-indicator")
        .description("Text shown on module rows when settings are collapsed.")
        .defaultValue("⏵")
        .onChanged(v -> invalidateCurrentScreen())
        .build()
    );

    public final Setting<String> moduleExpandedIndicator = sgModuleRender.add(new StringSetting.Builder()
        .name("module-expanded-indicator")
        .description("Text shown on module rows when settings are expanded.")
        .defaultValue("⏷")
        .onChanged(v -> invalidateCurrentScreen())
        .build()
    );

    public final Setting<ModuleSettingsIndicator> moduleSettingsIndicator = sgModuleRender.add(new EnumSetting.Builder<ModuleSettingsIndicator>()
        .name("module-settings-indicator")
        .description("Indicator shown on module rows for expanding/collapsing settings.")
        .defaultValue(ModuleSettingsIndicator.DROPDOWN)
        .onChanged(v -> invalidateCurrentScreen())
        .build()
    );

    public final Setting<Boolean> indicatorsOnSeparators = sgModuleRender.add(new BoolSetting.Builder()
        .name("indicators-on-separators")
        .description("Also applies the exeter/meteor indicator to section separators.")
        .defaultValue(false)
        .onChanged(v -> invalidateCurrentScreen())
        .build()
    );

    // Module colors
    public final Setting<SettingColor> moduleInactiveColor = color(sgModuleColor, "module-inactive", "Color of module when inactive.", new SettingColor(40, 40, 40, 0));
    public final Setting<SettingColor> moduleInactiveGradientColor = color(sgModuleColor, "module-inactive-gradient", "Gradient color for inactive modules. 'None' uses inactive color.", new SettingColor(40, 40, 40, 0));
    public final Setting<SettingColor> moduleActiveColor = color(sgModuleColor, "module-active", "Color of module when active.", new SettingColor(70, 70, 70));
    public final Setting<SettingColor> moduleActiveGradientColor = color(sgModuleColor, "module-active-gradient", "Gradient color for active modules. 'None' uses inactive color.", new SettingColor(40, 40, 40, 0));
    public final Setting<SettingColor> moduleHoveredColor = color(sgModuleColor, "module-hovered", "Color of module when hovered.", new SettingColor(60, 60, 60));
    public final Setting<SettingColor> moduleHoveredGradientColor = color(sgModuleColor, "module-hovered-gradient", "Gradient color for hovered modules.", new SettingColor(40, 40, 40, 0));

    // Module text colors
    public final Setting<SettingColor> moduleTextInactiveColor = color(sgModuleColor, "module-text-inactive", "Color of module text when inactive.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> moduleTextActiveColor = color(sgModuleColor, "module-text-active", "Color of module text when active.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> moduleTextHoveredColor = color(sgModuleColor, "module-text-hovered", "Color of module text when hovered.", new SettingColor(255, 255, 255));


    // Colors
    public final Setting<Boolean> followAccentHue = sgColors.add(new BoolSetting.Builder()
        .name("follow-accent-hue")
        .description("Affects all GUI colors, disabling it after restarting will reset a lot of settings")
        .defaultValue(false)
        .onChanged(this::onFollowAccentHueChanged)
        .build()
    );

    public final Setting<SettingColor> accentColor = color("accent", "Main color of the GUI.", new SettingColor(145, 61, 226), false);
    public final Setting<SettingColor> checkboxColor = color("checkbox", "Color of checkbox.", new SettingColor(145, 61, 226));
    public final Setting<SettingColor> plusColor = color("plus", "Color of plus button.", new SettingColor(50, 255, 50));
    public final Setting<SettingColor> minusColor = color("minus", "Color of minus button.", new SettingColor(255, 50, 50));
    public final Setting<SettingColor> favoriteColor = color("favorite", "Color of checked favorite button.", new SettingColor(250, 215, 0));

    // Text
    public final Setting<SettingColor> textColor = color(sgTextColors, "text", "Color of text.", new SettingColor(255, 255, 255), false);
    public final Setting<SettingColor> textSecondaryColor = color(sgTextColors, "text-secondary-text", "Color of secondary text.", new SettingColor(150, 150, 150), false);
    public final Setting<SettingColor> textHighlightColor = color(sgTextColors, "text-highlight", "Color of text highlighting.", new SettingColor(45, 125, 245, 100), false);
    public final Setting<SettingColor> titleTextColor = color(sgTextColors, "title-text", "Color of title text.", new SettingColor(255, 255, 255), false);
    public final Setting<SettingColor> loggedInColor = color(sgTextColors, "logged-in-text", "Color of logged in account name.", new SettingColor(45, 225, 45), false);
    public final Setting<SettingColor> placeholderColor = color(sgTextColors, "placeholder", "Color of placeholder text.", new SettingColor(255, 255, 255, 20), false);
    public final Setting<Boolean> textHoverDisplacement = sgTextColors.add(new BoolSetting.Builder()
        .name("text-hover-displacement")
        .description("Displaces text when hovered")
        .defaultValue(false)
        .build()
    );
    public final Setting<TextHoverDisplacementDirection> textHoverDisplacementDirection = sgTextColors.add(new EnumSetting.Builder<TextHoverDisplacementDirection>()
        .name("text-hover-displacement-direction")
        .description("Direction to displace text on hover")
        .defaultValue(TextHoverDisplacementDirection.RIGHT)
        .build()
    );
    public final Setting<Double> textHoverDisplacementAmount = sgTextColors.add(new DoubleSetting.Builder()
        .name("text-hover-displacement-amount")
        .description("How far hovered module and setting text displaces")
        .defaultValue(2)
        .min(0)
        .max(20)
        .sliderRange(0, 20)
        .build()
    );

    // Text Shadow
    public final Setting<Boolean> textShadow = sgTextShadow.add(new BoolSetting.Builder()
        .name("text-shadow")
        .description("Renders shadow behind text")
        .defaultValue(true)
        .build()
    );

    public final Setting<Double> textShadowOffset = sgTextShadow.add(new DoubleSetting.Builder()
        .name("text-shadow-offset")
        .description("Only affects non-mc font")
        .defaultValue(1.0)
        .min(0.5)
        .max(3.0)
        .sliderRange(0.5, 3.0)
        .build()
    );

    // Background
    public final MultiStateColorSetting backgroundColor = new MultiStateColorSetting(
            sgBackgroundColors,
            "background",
            "", new SettingColor(20, 20, 20, 200),
            "hovered-", new SettingColor(30, 30, 30, 200),
            "pressed-", new SettingColor(40, 40, 40, 200)
    );

    public final Setting<SettingColor> itemBackgroundColor = color(sgSettingsColors, "item-background", "Color of items.", new SettingColor(43, 43, 43, 150));
    public final Setting<SettingColor> itemBackgroundGradientColor = color(sgSettingsColors, "item-background-gradient", "Gradient color of items.", new SettingColor(40, 40, 40, 0));
    public final Setting<SettingColor> itemHoveredBackgroundColor = color(sgSettingsColors, "hovered-item-background", "Color of items when hovered.", new SettingColor(60, 60, 60, 255));
    public final Setting<SettingColor> itemHoveredBackgroundGradientColor = color(sgSettingsColors, "hovered-item-background-gradient", "Gradient color of items when hovered.", new SettingColor(40, 40, 40, 0));

    public final Setting<SettingColor> itemActiveColor = color(sgSettingsColors, "item-active", "Color of items when active.", new SettingColor(70, 70, 70, 200));
    public final Setting<SettingColor> itemActiveGradientColor = color(sgSettingsColors, "item-active-gradient", "Gradient color of items when active.", new SettingColor(40, 40, 40, 0));

    // Settings text colors
    public final Setting<SettingColor> settingsTextInactiveColor = color(sgSettingsColors, "settings-text-inactive", "Color of settings text when inactive.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> settingsTextActiveColor = color(sgSettingsColors, "settings-text-active", "Color of settings text when active.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> settingsTextHoveredColor = color(sgSettingsColors, "settings-text-hovered", "Color of settings text when hovered.", new SettingColor(255, 255, 255));

    // Outline

    public final MultiStateColorSetting outlineColor = new MultiStateColorSetting(
            sgOutline,
            "outline",
            "", new SettingColor(61, 61, 61, 150),
            "hovered-", new SettingColor(60, 60, 60),
            "pressed-", new SettingColor(70, 70, 70, 200)
    );



    public final Setting<SettingColor> windowOutlineColor = color(sgOutline, "window-outline", "Color of window outlines.", new SettingColor(145, 61, 226));

    public final Setting<Double> windowOutlineThickness = sgOutline.add(new DoubleSetting.Builder()
            .name("window-outline-thickness")
            .description("Thickness of window outlines.")
            .defaultValue(0)
            .min(0)
            .max(5)
            .sliderRange(0, 5)
            .build()
    );

    // Separator

    public final Setting<Double> separatorHeight = sgSeparator.add(new DoubleSetting.Builder()
            .name("separator-height")
            .description("Height of separator rows.")
            .defaultValue(0)
            .min(0)
            .max(50)
            .sliderRange(0, 50)
            .onChanged(v -> invalidateCurrentScreen())
            .build()
    );
    public final Setting<Double> separatorPaddingY = sgSeparator.add(new DoubleSetting.Builder()
            .name("separator-padding-y")
            .description("Vertical padding for separators in module settings.")
            .defaultValue(6)
            .min(0)
            .max(30)
            .sliderRange(0, 30)
            .onChanged(v -> invalidateCurrentScreen())
            .build()
    );
    public final Setting<SettingColor> separatorInactiveColor = color(sgSeparator, "separator-inactive", "Color of separator rows when inactive.", new SettingColor(61, 61, 61, 150));
    public final Setting<SettingColor> separatorInactiveGradientColor = color(sgSeparator, "separator-inactive-gradient", "Gradient color of separator rows when inactive.", new SettingColor(40, 40, 40, 0));
    public final Setting<SettingColor> separatorActiveColor = color(sgSeparator, "separator-active", "Color of separator rows when active.", new SettingColor(70, 70, 70));
    public final Setting<SettingColor> separatorActiveGradientColor = color(sgSeparator, "separator-active-gradient", "Gradient color of separator rows when active.", new SettingColor(40, 40, 40, 0));
    public final Setting<SettingColor> separatorHoveredColor = color(sgSeparator, "separator-hovered", "Color of separator rows when hovered.", new SettingColor(60, 60, 60));
    public final Setting<SettingColor> separatorHoveredGradientColor = color(sgSeparator, "separator-hovered-gradient", "Gradient color of separator rows when hovered.", new SettingColor(40, 40, 40, 0));

    // Separator text colors
    public final Setting<SettingColor> separatorTextInactiveColor = color(sgSeparator, "separator-text-inactive", "Color of separator text when inactive.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> separatorTextActiveColor = color(sgSeparator, "separator-text-active", "Color of separator text when active.", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> separatorTextHoveredColor = color(sgSeparator, "separator-text-hovered", "Color of separator text when hovered.", new SettingColor(255, 255, 255));

    // Scrollbar

    public final MultiStateColorSetting scrollbarColor = new MultiStateColorSetting(
            sgScrollbar,
            "Scrollbar",
            "", new SettingColor(61, 61, 61, 150),
            "hovered-", new SettingColor(60, 60, 60),
            "pressed-", new SettingColor(70, 70, 70)
    );

    // Slider

    public final MultiStateColorSetting sliderHandle = new MultiStateColorSetting(
            sgSlider,
            "slider-handle",
            "", new SettingColor(130, 0, 255),
            "hovered-", new SettingColor(140, 30, 255),
            "pressed-", new SettingColor(150, 60, 255),
            "-gradient", new SettingColor(0, 0, 0, 0)
    );
    public final MultiStateColorSetting sliderDirection = new MultiStateColorSetting(
            sgSlider,
            "slider",
            "left-", new SettingColor(145, 61, 226),
            "right-", new SettingColor(50, 50, 50, 0)
    );

    public final Setting<SliderStyle> sliderStyle = sgSlider.add(new EnumSetting.Builder<SliderStyle>()
            .name("slider-style")
            .description("Visual style of setting sliders.")
            .defaultValue(SliderStyle.FULL_BAR)
            .onChanged(v -> invalidateCurrentScreen())
            .build()
    );

    public final Setting<Double> sliderSpacing = sgSlider.add(new DoubleSetting.Builder()
            .name("slider-spacing")
            .description("How many pixels the slider bar is inset from the edges in FullBar mode.")
                .defaultValue(0)
            .min(0)
            .max(10)
            .sliderRange(0, 10)
            .onChanged(v -> invalidateCurrentScreen())
            .build()
    );

    // Starscript

    private final Setting<SettingColor> starscriptText = color(sgStarscript, "starscript-text", "Color of text in Starscript code.", new SettingColor(169, 183, 198), false);
    private final Setting<SettingColor> starscriptBraces = color(sgStarscript, "starscript-braces", "Color of braces in Starscript code.", new SettingColor(150, 150, 150), false);
    private final Setting<SettingColor> starscriptParenthesis = color(sgStarscript, "starscript-parenthesis", "Color of parenthesis in Starscript code.", new SettingColor(169, 183, 198), false);
    private final Setting<SettingColor> starscriptDots = color(sgStarscript, "starscript-dots", "Color of dots in starscript code.", new SettingColor(169, 183, 198), false);
    private final Setting<SettingColor> starscriptCommas = color(sgStarscript, "starscript-commas", "Color of commas in starscript code.", new SettingColor(169, 183, 198), false);
    private final Setting<SettingColor> starscriptOperators = color(sgStarscript, "starscript-operators", "Color of operators in Starscript code.", new SettingColor(169, 183, 198), false);
    private final Setting<SettingColor> starscriptStrings = color(sgStarscript, "starscript-strings", "Color of strings in Starscript code.", new SettingColor(106, 135, 89), false);
    private final Setting<SettingColor> starscriptNumbers = color(sgStarscript, "starscript-numbers", "Color of numbers in Starscript code.", new SettingColor(104, 141, 187), false);
    private final Setting<SettingColor> starscriptKeywords = color(sgStarscript, "starscript-keywords", "Color of keywords in Starscript code.", new SettingColor(204, 120, 50), false);
    private final Setting<SettingColor> starscriptAccessedObjects = color(sgStarscript, "starscript-accessed-objects", "Color of accessed objects (before a dot) in Starscript code.", new SettingColor(152, 118, 170), false);

    public BaseGuiTheme() {
        super("Exeter");

        settingsFactory = new BaseSettingsWidgetFactory(this);

        accentHueTickListener = new ConsumerListener<>(TickEvent.Post.class, event -> tickDynamicColors());
        MeteorClient.EVENT_BUS.subscribe(accentHueTickListener);
    }

    private void updateBlurCapture() {
        int strength = widgetBlurStrength.get();

        if (strength > 0) {
            float offset = strength * 0.5f;
            float scale = blurTextureScale.get().floatValue();
            if (blurCapture == null) {
                blurCapture = new WorldFramebufferCapture(strength, offset, scale);
            } else {
                blurCapture.updateSettings(strength, offset, scale);
            }
        } else if (blurCapture != null) {
            blurCapture.close();
            blurCapture = null;
        }
    }

    private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor color) {
        return color(group, name, description, color, true);
    }

    private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor color, boolean linkAccentHue) {
        Setting<SettingColor> setting = group.add(new ColorSetting.Builder()
            .name(name + "-color")
            .description(description)
            .defaultValue(color)
            .onChanged(c -> onAnyColorChanged())
            .build());

        if (linkAccentHue) accentHueLinkedColors.add(setting);

        return setting;
    }

    private Setting<SettingColor> color(String name, String description, SettingColor color) {
        return color(sgColors, name, description, color);
    }

    private Setting<SettingColor> color(String name, String description, SettingColor color, boolean linkAccentHue) {
        return color(sgColors, name, description, color, linkAccentHue);
    }

    private void onAnyColorChanged() {
        if (!followAccentHue.get() || updatingAccentHueLinkedColors) return;
        applyAccentHueToLinkedColors();
    }

    public void tickDynamicColors() {
        if (!followAccentHue.get() || updatingAccentHueLinkedColors) return;

        float accentHueValue = hue(accentColor.get());
        if (Float.compare(accentHueValue, lastAppliedAccentHue) != 0) {
            applyAccentHueToLinkedColors(accentHueValue);
        }
    }

    private void onFollowAccentHueChanged(boolean enabled) {
        if (updatingAccentHueLinkedColors) return;

        if (enabled) {
            lastAppliedAccentHue = Float.NaN;
            applyAccentHueToLinkedColors();
        } else {
            lastAppliedAccentHue = Float.NaN;
        }

        invalidateCurrentScreen();
    }

    private void applyAccentHueToLinkedColors() {
        applyAccentHueToLinkedColors(hue(accentColor.get()));
    }

    private void applyAccentHueToLinkedColors(float accentHueValue) {
        if (updatingAccentHueLinkedColors) return;

        updatingAccentHueLinkedColors = true;
        try {
            for (Setting<SettingColor> setting : accentHueLinkedColors) {
                SettingColor current = setting.get();
                SettingColor updated = withHue(current, accentHueValue);

                if (current.r != updated.r || current.g != updated.g || current.b != updated.b) {
                    setting.set(updated);
                }
            }
            lastAppliedAccentHue = accentHueValue;
        } finally {
            updatingAccentHueLinkedColors = false;
        }
    }

    private float hue(SettingColor color) {
        float[] hsb = java.awt.Color.RGBtoHSB(color.r, color.g, color.b, null);
        return hsb[0];
    }

    private SettingColor withHue(SettingColor color, float hue) {
        float[] hsb = java.awt.Color.RGBtoHSB(color.r, color.g, color.b, null);
        int rgb = java.awt.Color.HSBtoRGB(hue, hsb[1], hsb[2]);

        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        return new SettingColor(r, g, b, color.a);
    }
    private void invalidateCurrentScreen() {
        if (mc.currentScreen instanceof WidgetScreen screen) screen.invalidate();
    }

    // Widgets

    @Override
    public WBaseWindow window(WWidget icon, String title) {
        return w(new WBaseWindow(icon, title));
    }

    //https://github.com/X-C-0/catppuccin-addon/blob/d642959fbaa9e5757013ea38f57556eb88c8b822/src/main/java/me/pindour/catppuccin/gui/themes/catppuccin/CatppuccinGuiTheme.java#L275
    @Override
    public WBaseWindow window(String title) {
        return window(null, title);
    }

    @Override
    public WLabel label(String text, boolean title, double maxWidth) {
        if (maxWidth == 0 && !text.contains("\n")) return w(new WBaseLabel(text, title));
        return w(new WBaseMultiLabel(text, title, maxWidth));
    }

    @Override
    public WHorizontalSeparator horizontalSeparator(String text) {
        return w(new WBaseHorizontalSeparator(text));
    }

    @Override
    public WVerticalSeparator verticalSeparator() {
        return w(new WVerticalSeparator());
    }

    @Override
    protected WButton button(String text, GuiTexture texture) {
        return w(new WBaseButton(text, texture));
    }

    @Override
    protected WConfirmedButton confirmedButton(String text, String confirmText, GuiTexture texture) {
        return w(new WBaseConfirmedButton(text, confirmText, texture));
    }

    @Override
    public WMinus minus() {
        return w(new WBaseMinus());
    }

    @Override
    public WConfirmedMinus confirmedMinus() {
        return w(new WBaseConfirmedMinus());
    }

    @Override
    public WPlus plus() {
        return w(new WBasePlus());
    }

    @Override
    public WCheckbox checkbox(boolean checked) {
        return w(new WBaseCheckbox(checked));
    }

    @Override
    public WSlider slider(double value, double min, double max) {
        return w(new WBaseSlider(value, min, max));
    }

    @Override
    public WTextBox textBox(String text, String placeholder, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return w(new WBaseTextBox(text, placeholder, filter, renderer));
    }

    @Override
    public <T> WDropdown<T> dropdown(T[] values, T value) {
        return w(new WBaseDropdown<>(values, value));
    }

    @Override
    public WKeybind keybind(meteordevelopment.meteorclient.utils.misc.Keybind keybind, meteordevelopment.meteorclient.utils.misc.Keybind defaultValue) {
        return w(new WBaseKeybind(keybind, defaultValue));
    }

    @Override
    public WTriangle triangle() {
        return w(new WBaseTriangle());
    }

    @Override
    public WTooltip tooltip(String text) {
        return w(new WBaseTooltip(text));
    }

    @Override
    public WView view() {
        return w(new WBaseView());
    }

    @Override
    public WSection section(String title, boolean expanded, WWidget headerWidget) {
        return w(new WBaseSection(title, expanded, headerWidget));
    }

    @Override
    public WAccount account(WidgetScreen screen, Account<?> account) {
        return w(new WBaseAccount(screen, account));
    }

    @Override
    public WWidget module(Module module, String title) {
        if (inlineModuleSettings.get()) return w(new WBaseModule(module, title));
        return w(new WBaseModuleLegacy(module, title));
    }

    @Override
    public WQuad quad(Color color) {
        return w(new WBaseQuad(color));
    }

    @Override
    public WTopBar topBar() {
        return w(new WBaseTopBar());
    }

    @Override
    public WFavorite favorite(boolean checked) {
        return w(new WBaseFavorite(checked));
    }

    // Screens

    @Override
    public TabScreen modulesScreen() {
        return new me.sophimoo.exeter.gui.screens.BaseModulesScreen(this);
    }

    @Override
    public meteordevelopment.meteorclient.gui.WindowScreen moduleScreen(Module module) {
        return new me.sophimoo.exeter.gui.screens.ModuleSettingsScreen(this, module);
    }

    @Override
    public boolean isModulesScreen(Screen screen) {
        return screen instanceof me.sophimoo.exeter.gui.screens.BaseModulesScreen;
    }

    // Colors

    @Override
    public Color textColor() {
        return textColor.get();
    }

    @Override
    public Color textSecondaryColor() {
        return textSecondaryColor.get();
    }

    //     Starscript

    @Override
    public Color starscriptTextColor() {
        return starscriptText.get();
    }

    @Override
    public Color starscriptBraceColor() {
        return starscriptBraces.get();
    }

    @Override
    public Color starscriptParenthesisColor() {
        return starscriptParenthesis.get();
    }

    @Override
    public Color starscriptDotColor() {
        return starscriptDots.get();
    }

    @Override
    public Color starscriptCommaColor() {
        return starscriptCommas.get();
    }

    @Override
    public Color starscriptOperatorColor() {
        return starscriptOperators.get();
    }

    @Override
    public Color starscriptStringColor() {
        return starscriptStrings.get();
    }

    @Override
    public Color starscriptNumberColor() {
        return starscriptNumbers.get();
    }

    @Override
    public Color starscriptKeywordColor() {
        return starscriptKeywords.get();
    }

    @Override
    public Color starscriptAccessedObjectColor() {
        return starscriptAccessedObjects.get();
    }

    // Other

    @Override
    public TextRenderer textRenderer() {
        return TextRenderer.get();
    }

    @Override
    public double pad() {
        return scale(globalPadding.get());
    }

    @Override
    public double scale(double value) {
        return value * scale.get();
    }

    @Override
    public boolean categoryIcons() {
        return categoryIcons.get();
    }

    @Override
    public boolean hideHUD() {
        return hideHUD.get();
    }

    public boolean shouldUseFixedCategoryWidth(String windowId) {
        if (!fixedCategorySize.get()) return false;
        if (windowId == null || windowId.isEmpty()) return false;
        if ("search".equals(windowId) || "favorites".equals(windowId)) return false;

        for (Category category : Modules.loopCategories()) {
            if (category.name.equals(windowId)) return true;
        }

        return false;
    }

    public double scaledFixedCategoryWidth() {
        return Math.round(scale(fixedCategoryWidth.get()));
    }

    public double scaledPx(double value) {
        return scale(value);
    }

    public double rowPadX() {
        return scale(settingsPaddingX.get());
    }

    public double rowPadY() {
        return pad();
    }

    public double separatorThickness() {
        return scale(2);
    }

    public double glyphThickness() {
        return scale(3);
    }

    public double sliderTrackHeight() {
        return scale(3);
    }

    public double sliderBottomGap() {
        return scale(5);
    }

    public double sliderMinTrackWidth() {
        return scale(16);
    }

    public double sliderInset() {
        return scale(sliderSpacing.get());
    }

    public double sliderFullBarMinTrackHeight() {
        return scale(4);
    }

    public class MultiStateColorSetting {
        private final Map<String, Setting<SettingColor>> states = new LinkedHashMap<>();

        public MultiStateColorSetting(SettingGroup group, String name, Object... stateDefinitions) {
            for (int i = 0; i < stateDefinitions.length; i += 2) {
                String affix = (String) stateDefinitions[i];
                SettingColor defaultColor = (SettingColor) stateDefinitions[i + 1];
                states.put(affix, color(group, stateName(name, affix), stateDescription(name, affix), defaultColor));
            }
        }

        public SettingColor get() {
            return get("");
        }

        public SettingColor get(String affix) {
            return states.get(affix).get();
        }

        public SettingColor get(boolean pressed, boolean hovered, boolean bypassDisableHoverColor) {
            if (pressed) return get("pressed-");
            return (hovered && (bypassDisableHoverColor || !disableHoverColor)) ? get("hovered-") : get();
        }

        public SettingColor get(boolean pressed, boolean hovered) {
            return get(pressed, hovered, false);
        }

        private String stateName(String baseName, String affix) {
            return affix.startsWith("-") ? baseName + affix : affix + baseName;
        }

        private String stateDescription(String baseName, String affix) {
            String displayName = baseName.replace('-', ' ');

            if (affix.isEmpty()) return "Color of " + displayName + ".";
            if (affix.endsWith("-")) return "Color of " + affix.substring(0, affix.length() - 1).replace('-', ' ') + " " + displayName + ".";
            if (affix.startsWith("-")) return "Color of " + displayName + " " + affix.substring(1).replace('-', ' ') + ".";

            return "Color of " + (affix + baseName).replace('-', ' ') + ".";
        }
    }
}
