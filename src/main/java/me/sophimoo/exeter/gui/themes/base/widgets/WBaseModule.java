package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.themes.base.GradientApplicationMode;
import me.sophimoo.exeter.gui.themes.base.MarqueeState;
import me.sophimoo.exeter.gui.themes.base.ModuleAnimationMode;
import me.sophimoo.exeter.gui.themes.base.ModuleGradientDirection;
import me.sophimoo.exeter.gui.themes.base.AlignmentY;
import me.sophimoo.exeter.gui.themes.base.AnimatedOverlayRenderer;
import me.sophimoo.exeter.gui.themes.base.ModuleIndicatorPosition;
import me.sophimoo.exeter.gui.themes.base.SmartSlideAnimationState;
import me.sophimoo.exeter.gui.screens.BaseModulesScreen;
import me.sophimoo.exeter.gui.themes.base.widgets.settings.WBindControlRow;
import me.sophimoo.exeter.gui.themes.base.widgets.settings.WBindToggleRow;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class WBaseModule extends WVerticalList implements BaseWidget {
    private static final double EXETER_ICON_SCALE = 1.4;
    private static final double EXETER_ICON_ROTATION_SPEED = 120;
    private static Texture exeterIndicatorTexture;

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

    private static Texture getExeterIndicatorTexture() {
        if (exeterIndicatorTexture == null) {
            exeterIndicatorTexture = Texture.readResource("/assets/base-addon/icon.png", false, FilterMode.LINEAR);
        }

        return exeterIndicatorTexture;
    }

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
        double paddingX = (theme instanceof BaseGuiTheme baseTheme) ? baseTheme.moduleSettingsPaddingX.get() : 6;
        double separatorPaddingY = (theme instanceof BaseGuiTheme baseTheme) ? baseTheme.scale(baseTheme.separatorPaddingY.get()) : 6;
        double itemSpacing = (theme instanceof BaseGuiTheme baseTheme) ? baseTheme.itemSpacing.get() : 0;

        Cell<WSection> bindSectionCell = container.add(theme.section("Bind", false)).expandX().padHorizontal(paddingX).padBottom(separatorPaddingY);
        if (hasModuleSettingsContent) bindSectionCell.padTop(separatorPaddingY);

        WSection bindSection = bindSectionCell.widget();
        bindSection.spacing = 0;

        WTable table = bindSection.add(theme.table()).expandX().padHorizontal(paddingX).padTop(separatorPaddingY).widget();
        table.verticalSpacing = theme.scale(itemSpacing);

        WContainer bindControls = theme.horizontalList();
        keybindWidget = bindControls.add(theme.keybind(module.keybind)).expandX().widget();
        keybindWidget.actionOnSet = new Runnable() {
            @Override
            public void run() {
                Modules.get().setModuleToBind(module);
                // Unfocus any focused text boxes so they don't intercept key events
                unfocusTextBoxes(WBaseModule.this);
            }
        };

        table.add(new WBindControlRow("Keybind", "Set the key used to toggle this module.", bindControls)).expandX();
        table.row();
        addBindToggleRow(table, "Hold Toggle", "Toggle this module when its bind is released.",
            () -> module.toggleOnBindRelease,
            checked -> module.toggleOnBindRelease = checked);
        addBindToggleRow(table, "Chat Feedback", "Show chat messages when this module toggles.",
            () -> module.chatFeedback,
            checked -> module.chatFeedback = checked);
        table.row();

        // Copy/Paste/Reset buttons
        WContainer sharing = theme.horizontalList();
        
        WButton copy = sharing.add(theme.button(GuiRenderer.COPY)).expandX().widget();
        copy.action = () -> {
            if (copyModuleToClipboard()) {
                // Optional: show feedback
            }
        };
        copy.tooltip = "Copy module config";

        WButton paste = sharing.add(theme.button(GuiRenderer.PASTE)).expandX().widget();
        paste.action = () -> pasteModuleFromClipboard();
        paste.tooltip = "Paste module config";
        
        WButton reset = sharing.add(theme.button(GuiRenderer.RESET)).expandX().widget();
        reset.action = () -> module.settings.reset();
        reset.tooltip = "Reset all settings to default";

        table.add(sharing).expandX();
    }

    private void addBindToggleRow(WTable table, String title, String description, BooleanSupplier getter, Consumer<Boolean> setter) {
        table.add(new WBindToggleRow(title, description, getter, setter, true)).expandX();
        table.row();
    }

    private boolean hasSettingsContent(WWidget settingsWidget) {
        if (settingsWidget instanceof WContainer container) return !container.cells.isEmpty();
        return true;
    }

    private boolean copyModuleToClipboard() {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", module.name);
        NbtCompound settingsTag = module.settings.toTag();
        if (!settingsTag.isEmpty()) tag.put("settings", settingsTag);
        return NbtUtils.toClipboard(tag);
    }

    private boolean pasteModuleFromClipboard() {
        NbtCompound tag = NbtUtils.fromClipboard();
        if (tag == null) return false;
        if (!tag.getString("name", "").equals(module.name)) return false;
        Optional<NbtCompound> settings = tag.getCompound("settings");
        if (settings.isPresent()) module.settings.fromTag(settings.get());
        else module.settings.reset();
        return true;
    }

    private void unfocusTextBoxes(WWidget widget) {
        if (widget instanceof meteordevelopment.meteorclient.gui.widgets.input.WTextBox textBox) {
            if (textBox.isFocused()) textBox.setFocused(false);
        }
        if (widget instanceof WContainer container) {
            for (meteordevelopment.meteorclient.gui.utils.Cell<?> cell : container.cells) {
                unfocusTextBoxes(cell.widget());
            }
        }
    }

    @EventHandler
    private void onModuleBindChanged(ModuleBindChangedEvent event) {
        if (event.module == module && keybindWidget != null) {
            keybindWidget.reset();
        }
    }

    // Inner pressable widget that handles the module row rendering and click events
    private class WModuleButton extends WPressable implements BaseWidget {
        private double titleWidth;

        private double animationProgress;
        private double hoverOverlayProgress;
        private double indicatorProgress;
        private double exeterIconRotation;
        private final MarqueeState marquee = new MarqueeState();
        private final SmartSlideAnimationState smartSlide = new SmartSlideAnimationState();

        private record RenderSettings(
            ModuleGradientDirection gradientDir,
            Color activeGradientColor,
            Color inactiveGradientColor,
            GradientApplicationMode applyMode,
            double thickness,
            Color baseColor,
            boolean renderBaseGradient,
            Color overlayColor,
            Color overlayGradient,
            boolean renderOverlayGradient,
            Color hoveredOverlayColor,
            Color hoveredOverlayGradient,
            boolean renderHoveredOverlayGradient,
            Color outlineColor
        ) {}

        public WModuleButton() {
            this.tooltip = module.description;

            if (module.isActive()) {
                animationProgress = 1;
                indicatorProgress = 1;
            } else {
                animationProgress = 0;
                indicatorProgress = 0;
            }

            hoverOverlayProgress = 0;
        }

        @Override
        public double pad() {
            return theme().pad();
        }

        @Override
        protected void onCalculateSize() {
            double pad = pad();
            ModuleRowLayout layout = computeRowLayout();

            if (titleWidth == 0) titleWidth = theme().textWidth(title);

            if (theme().fixedCategorySize.get()) width = pad + pad + layout.settingsIconWidth + layout.settingsIconGap;
            else width = pad + titleWidth + layout.settingsIconWidth + layout.settingsIconGap + pad;
            height = resolveModuleRowHeight(pad + theme().textHeight() + pad);
        }

        @Override
        protected void onPressed(int button) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) module.toggle();
            else if (button == GLFW_MOUSE_BUTTON_RIGHT) toggleSettings();
        }

        private ModuleAnimationMode updateAnimationProgresses(double mouseX, double mouseY, double delta) {
            boolean isActive = module.isActive();
            boolean shouldFadeIn = isActive || mouseOver;
            ModuleAnimationMode animationMode = theme().moduleAnimationMode.get();
            ModuleAnimationMode effectiveAnimationMode = smartSlide.resolveMode(
                animationMode,
                mouseOver,
                shouldFadeIn,
                mouseX,
                mouseY,
                x,
                y,
                width,
                height,
                theme(),
                animationProgress
            );

            double fadeInSpeed = theme().moduleSelectSpeed.get();
            double fadeOutSpeed = theme().moduleDeselectSpeed.get();

            animationProgress = smartSlide.stepProgress(animationProgress, shouldFadeIn, delta, fadeInSpeed, fadeOutSpeed);
            hoverOverlayProgress = smartSlide.stepProgress(hoverOverlayProgress, mouseOver, delta, fadeInSpeed, fadeOutSpeed);

            // Update indicator animation (separate from main animation)
            indicatorProgress += delta * (isActive ? fadeInSpeed : fadeOutSpeed) * (isActive ? 1 : -1);
            indicatorProgress = MathHelper.clamp(indicatorProgress, 0, 1);
            
return effectiveAnimationMode;
        }
        
        private RenderSettings computeRenderSettings(boolean isActive) {
            ModuleGradientDirection gradientDir = theme().moduleGradientDirection.get();
            Color activeGradientColor = theme().moduleActiveGradientColor.get();
            Color inactiveGradientColor = theme().moduleInactiveGradientColor.get();
            GradientApplicationMode applyMode = theme().gradientApplicationMode.get();
            double thickness = theme().scale(theme().moduleOutlineThickness.get());
            Color baseColor = theme().moduleInactiveColor.get();
            boolean renderBaseGradient = !isActive && applyMode.appliesToInactive() && gradientDir != ModuleGradientDirection.None;
            Color overlayColor = isActive ? theme().moduleActiveColor.get() : theme().moduleHoveredColor.get();
            Color overlayGradient = isActive ? activeGradientColor : theme().moduleHoveredGradientColor.get();
            boolean renderOverlayGradient = isActive ? applyMode.appliesToActive() : applyMode.appliesToInactive();
            Color hoveredOverlayColor = theme().moduleHoveredColor.get();
            Color hoveredOverlayGradient = theme().moduleHoveredGradientColor.get();
            boolean renderHoveredOverlayGradient = applyMode.appliesToInactive();
            Color outlineColor = theme().outlineColor.get(pressed, mouseOver);
            
return new RenderSettings(
                gradientDir, activeGradientColor, inactiveGradientColor, applyMode, thickness,
                baseColor, renderBaseGradient, overlayColor, overlayGradient, renderOverlayGradient,
                hoveredOverlayColor, hoveredOverlayGradient, renderHoveredOverlayGradient, outlineColor
            );
        }
        
        private void renderAnimatedOverlay(GuiRenderer renderer, ModuleAnimationMode animationMode, double progress,
                                          Color color, Color gradient, boolean renderGradient, ModuleGradientDirection gradientDir) {
            AnimatedOverlayRenderer.render(
                renderer,
                x,
                y,
                width,
                height,
                animationMode,
                progress,
                color,
                gradient,
renderGradient ? gradientDir : ModuleGradientDirection.None
            );
        }
        
        private void renderTitle(GuiRenderer renderer, double pad, ModuleRowLayout layout, Color textColor, double textY, double delta) {
            double textAreaX = this.x + pad;
            double textAreaW = Math.max(0, width - pad * 2 - layout.settingsIconWidth - layout.settingsIconGap);
            double overflow = Math.max(0, titleWidth - textAreaW);
            boolean needsMarquee = overflow > 0 && theme().fixedCategorySize.get();

            double staticTextX = textAreaX;
            if (theme().moduleAlignment.get() == AlignmentX.Center) {
                staticTextX += textAreaW / 2 - titleWidth / 2;
            }
            else if (theme().moduleAlignment.get() == AlignmentX.Right) {
                staticTextX += textAreaW - titleWidth;
            }

            renderTextWithMarquee(renderer, marquee, title, textAreaX, this.y, textAreaW, height, textY, titleWidth,
mouseOver, delta, needsMarquee, staticTextX, textColor);
        }
        
        private void renderSettingsIcon(GuiRenderer renderer, double pad, ModuleRowLayout layout, double textY, double delta) {
            if (!layout.showIndicator) return;
            
            double settingsIconX = this.x + width - pad - layout.settingsIconWidth;
            
            if (layout.useExeterIndicator) {
                Texture iconTexture = getExeterIndicatorTexture();
                if (iconTexture != null) {
                    double baseIconSize = Math.max(1, Math.min(layout.settingsIconWidth, height - pad * 2));
                    if (settingsExpanded) {
                        exeterIconRotation = (exeterIconRotation + delta * EXETER_ICON_ROTATION_SPEED) % 360;
                    }
                    
                    double iconSize = baseIconSize;
                    double iconY = this.y + (height - iconSize) / 2;
                    double iconX = settingsIconX + (layout.settingsIconWidth - iconSize) / 2;
                    
                    renderer.texture(iconX, iconY, iconSize, iconSize, exeterIconRotation, iconTexture);
                }
            } else {
                String settingsIcon = settingsExpanded ? layout.expandedIndicator : layout.collapsedIndicator;
                Color settingsIconColor = theme().textColor();
                renderText(renderer, settingsIcon, settingsIconX, textY, settingsIconColor);
            }
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double pad = pad();
            ModuleRowLayout layout = computeRowLayout();

            ModuleAnimationMode effectiveAnimationMode = updateAnimationProgresses(mouseX, mouseY, delta);
            boolean isActive = module.isActive();

            RenderSettings settings = computeRenderSettings(isActive);

            renderAnimatedOverlay(renderer, ModuleAnimationMode.FADE, 1,
                settings.baseColor(), settings.inactiveGradientColor(),
                settings.renderBaseGradient(), settings.gradientDir());

            if (animationProgress > 0) {
                renderAnimatedOverlay(renderer, effectiveAnimationMode, animationProgress,
                    settings.overlayColor(), settings.overlayGradient(),
                    settings.renderOverlayGradient(), settings.gradientDir());
            }

            if (isActive && hoverOverlayProgress > 0) {
                renderAnimatedOverlay(renderer, effectiveAnimationMode, hoverOverlayProgress,
                    settings.hoveredOverlayColor(), settings.hoveredOverlayGradient(),
                    settings.renderHoveredOverlayGradient(), settings.gradientDir());
            }

            if (settings.thickness() > 0 && settings.outlineColor() != null) {
                renderOutline(renderer, x, y, width, height, settings.thickness(), settings.outlineColor());
            }

            // Render active module indicator
            if (indicatorProgress > 0) {
                renderIndicator(renderer, indicatorProgress);
            }

            // Determine text color based on module state
            Color textColor = resolveTextColor();
            double textY = resolveTextY(pad);
            renderTitle(renderer, pad, layout, textColor, textY, delta);

            renderSettingsIcon(renderer, pad, layout, textY, delta);
        }

        private ModuleRowLayout computeRowLayout() {
            String collapsedIndicator = safeIndicator(theme().moduleCollapsedIndicator.get());
            String expandedIndicator = safeIndicator(theme().moduleExpandedIndicator.get());
            boolean useExeterIndicator = theme().exeterIndicator.get();
            boolean showIndicator = useExeterIndicator || theme().showModuleIndicator.get();
            double settingsIconWidth = showIndicator
                ? (useExeterIndicator
                    ? theme().textHeight() * EXETER_ICON_SCALE
                    : Math.max(theme().textWidth(collapsedIndicator), theme().textWidth(expandedIndicator)))
                : 0;
            double settingsIconGap = showIndicator ? pad() : 0;
            return new ModuleRowLayout(collapsedIndicator, expandedIndicator, useExeterIndicator, showIndicator, settingsIconWidth, settingsIconGap);
        }

        private String safeIndicator(String value) {
            return value == null ? "" : value;
        }

        private Color resolveTextColor() {
            if (module.isActive()) return theme().moduleTextActiveColor.get();
            if (mouseOver) return theme().moduleTextHoveredColor.get();
            return theme().moduleTextInactiveColor.get();
        }

        private double resolveTextY(double pad) {
            double textHeight = theme().textHeight();
            double availableHeight = height - pad * 2;
            AlignmentY vAlign = theme().moduleAlignmentY.get();
            if (vAlign == AlignmentY.Top) return y + pad;
            if (vAlign == AlignmentY.Bottom) return y + pad + availableHeight - textHeight;
            return y + pad + (availableHeight - textHeight) / 2;
        }

        

        private class ModuleRowLayout {
            private final String collapsedIndicator;
            private final String expandedIndicator;
            private final boolean useExeterIndicator;
            private final boolean showIndicator;
            private final double settingsIconWidth;
            private final double settingsIconGap;

            private ModuleRowLayout(String collapsedIndicator, String expandedIndicator, boolean useExeterIndicator, boolean showIndicator, double settingsIconWidth, double settingsIconGap) {
                this.collapsedIndicator = collapsedIndicator;
                this.expandedIndicator = expandedIndicator;
                this.useExeterIndicator = useExeterIndicator;
                this.showIndicator = showIndicator;
                this.settingsIconWidth = settingsIconWidth;
                this.settingsIconGap = settingsIconGap;
            }
        }


        private void renderIndicator(GuiRenderer renderer, double progress) {
            ModuleIndicatorPosition position = theme().moduleIndicatorPosition.get();
            if (position == ModuleIndicatorPosition.None) return;

            double thickness = theme().scale(theme().moduleIndicatorThickness.get());
            if (thickness <= 0) return;

            Color accentColor = theme().accentColor.get();
            double size = thickness * progress;

            double ix = this.x, iy = this.y, iw = this.width, ih = this.height;

            switch (position) {
                case Left -> iw = size;
                case Right -> { ix = this.x + this.width - size; iw = size; }
                case Top -> ih = size;
                case Bottom -> { iy = this.y + this.height - size; ih = size; }
            }

            renderer.quad(ix, iy, iw, ih, accentColor);
        }

    }

    private class WSettingsDropdown extends WVerticalList {
        private boolean expanded;
        private double animProgress;
        private double expandedHeight;
        private int lastAnimatedHeightPx = -1;
        private int lastPadTopPx = -1;

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
            if (expanded) settingsTickCooldown = 0;
        }

        public boolean isFullyExpanded() {
            return animProgress >= 1;
        }

        @Override
        protected void onCalculateSize() {
            super.onCalculateSize();
            expandedHeight = height;
            height = expandedHeight * animProgress;
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!visible) return true;

            animProgress += (expanded ? 1 : -1) * delta * 14;
            animProgress = MathHelper.clamp(animProgress, 0, 1);

            double animatedHeight = expandedHeight * animProgress;
            int animatedHeightPx = MathHelper.floor(animatedHeight + 0.5);
            boolean animating = animProgress > 0 && animProgress < 1;
            int relayoutHeightPx = quantizeForAnimation(animatedHeightPx, animating);

            int padTopPx = 0;
            boolean relayoutNeeded = false;
            if (settingsContainerCell != null) {
                padTopPx = MathHelper.floor(theme().scale(theme().separatorPaddingY.get()) * animProgress + 0.5);
                int relayoutPadTopPx = quantizeForAnimation(padTopPx, animating);
                if (relayoutPadTopPx != lastPadTopPx) {
                    settingsContainerCell.padTop(relayoutPadTopPx);
                    lastPadTopPx = relayoutPadTopPx;
                    relayoutNeeded = true;
                }
            }

            if (relayoutHeightPx != lastAnimatedHeightPx) {
                lastAnimatedHeightPx = relayoutHeightPx;
                relayoutNeeded = true;
            }
            if (relayoutNeeded) invalidate();
            if (animProgress <= 0) return false;

            boolean scissor = animProgress != 1;
            if (scissor) renderer.scissorStart(x, y, width, animatedHeight);
            boolean toReturn = super.render(renderer, mouseX, mouseY, delta);
            if (scissor) renderer.scissorEnd();

            return toReturn;
        }

        private int quantizeForAnimation(int valuePx, boolean animating) {
            return animating ? (valuePx / 2) * 2 : valuePx;
        }

        @Override
        protected boolean propagateEvents(WWidget widget) {
            return expanded;
        }
    }
}
