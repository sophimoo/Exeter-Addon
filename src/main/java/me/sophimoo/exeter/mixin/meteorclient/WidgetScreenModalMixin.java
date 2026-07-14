package me.sophimoo.exeter.mixin.meteorclient;

import me.sophimoo.exeter.gui.modal.WidgetScreenModalBridge;
import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
import me.sophimoo.exeter.gui.themes.base.widgets.WBaseWindow;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;
@Mixin(value = WidgetScreen.class, remap = false)
public abstract class WidgetScreenModalMixin implements WidgetScreenModalBridge {
    @Shadow
    protected GuiTheme theme;

    @Unique
    private final GuiRenderer exeter$overlayRenderer = new GuiRenderer();

    @Unique
    private final GuiRenderer exeter$modalTooltipRenderer = new GuiRenderer();

    @Unique
    private WWidget exeter$modalTooltipWidget;

    @Unique
    private double exeter$modalTooltipTimer;

    @Unique
    private static final Color exeter$OVERLAY_COLOR = new Color(0, 0, 0, 120);

    @Unique
    private final List<WidgetScreen> exeter$modals = new ArrayList<>(2);

    @Unique
    private WidgetScreen exeter$modalHost;

    @Unique
    private static double exeter$mouseX() {
        return mc.mouse.getScaledX(mc.getWindow()) * mc.getWindow().getScaleFactor();
    }

    @Unique
    private static double exeter$mouseY() {
        return mc.mouse.getScaledY(mc.getWindow()) * mc.getWindow().getScaleFactor();
    }

    @Unique
    private static WBaseWindow exeter$getWindow(WidgetScreen screen) {
        if (!(screen instanceof WindowScreen windowScreen)) return null;
        WWidget window = ((WindowScreenAccessor) windowScreen).exeter$getWindow();
        return window instanceof WBaseWindow baseWindow ? baseWindow : null;
    }

    @Unique
    private static void exeter$positionWindowTopLeftAtCursor(WidgetScreen modal) {
        WBaseWindow window = exeter$getWindow(modal);
        if (window == null) return;

        double targetX = Math.max(0, Math.min(exeter$mouseX(), Utils.getWindowWidth() - window.width));
        double targetY = Math.max(0, Math.min(exeter$mouseY(), Utils.getWindowHeight() - window.height));
        window.moveTo(targetX, targetY);
    }

    @Unique
    private static void exeter$clampWindowToScreen(WidgetScreen modal) {
        WBaseWindow window = exeter$getWindow(modal);
        if (window == null) return;

        double maxX = Math.max(0, Utils.getWindowWidth() - window.width);
        double maxY = Math.max(0, Utils.getWindowHeight() - window.height);

        double clampedX = Math.max(0, Math.min(window.x, maxX));
        double clampedY = Math.max(0, Math.min(window.y, maxY));

        if (clampedX != window.x || clampedY != window.y) {
            window.moveTo(clampedX, clampedY);
        }
    }

    @Unique
    private static boolean exeter$isInsideModalContent(WidgetScreen modal, double mouseX, double mouseY) {
        WWidget window = exeter$getWindow(modal);
        if (window != null) {
            return mouseX >= window.x && mouseX <= window.x + window.width &&
                mouseY >= window.y && mouseY <= window.y + window.height;
        }
        return true;
    }

    @Unique
    private static WidgetScreen exeter$topModal(List<WidgetScreen> modals) {
        if (modals.isEmpty()) return null;
        return modals.get(modals.size() - 1);
    }

    @Unique
    private static void exeter$syncHoverState(WidgetScreen screen) {
        if (screen == null) return;

        screen.mouseMoved(mc.mouse.getScaledX(mc.getWindow()), mc.mouse.getScaledY(mc.getWindow()));
    }

    @Unique
    private boolean exeter$shouldRenderDarkening() {
        WidgetScreen self = (WidgetScreen) (Object) this;
        if (self instanceof HudEditorScreen) return false;
        return !(theme instanceof BaseGuiTheme baseTheme) || baseTheme.darkening.get();
    }

    @Unique
    private void exeter$renderOverlay(DrawContext context) {
        exeter$overlayRenderer.theme = theme;
        exeter$overlayRenderer.begin(context);
        exeter$overlayRenderer.quad(0, 0, Utils.getWindowWidth(), Utils.getWindowHeight(), exeter$OVERLAY_COLOR);
        exeter$overlayRenderer.end();
    }

    @Unique
    private static WWidget exeter$findTooltipWidget(WWidget widget, double mouseX, double mouseY) {
        if (widget == null || !widget.visible || !widget.isOver(mouseX, mouseY)) return null;

        if (widget instanceof WContainer container) {
            for (int i = container.cells.size() - 1; i >= 0; i--) {
                WWidget hovered = exeter$findTooltipWidget(container.cells.get(i).widget(), mouseX, mouseY);
                if (hovered != null) return hovered;
            }
        }

        return widget.tooltip == null || widget.tooltip.isBlank() ? null : widget;
    }

    @Unique
    private void exeter$renderModalTooltip(DrawContext context, WidgetScreen modal, float delta) {
        double mouseX = exeter$mouseX();
        double mouseY = exeter$mouseY();
        WWidget hovered = exeter$findTooltipWidget(exeter$getWindow(modal), mouseX, mouseY);

        if (hovered != exeter$modalTooltipWidget) {
            exeter$modalTooltipWidget = hovered;
            exeter$modalTooltipTimer = 0;
        } else if (hovered != null) {
            exeter$modalTooltipTimer += delta / 20;
        }

        exeter$modalTooltipRenderer.theme = ((WidgetScreenModalBridge) modal).exeter$getTheme();
        if (hovered != null && exeter$modalTooltipTimer >= 1) {
            exeter$modalTooltipRenderer.tooltip(hovered.tooltip);
        }

        Utils.unscaledProjection();
        exeter$modalTooltipRenderer.renderTooltip(context, mouseX, mouseY, delta / 20);
        Utils.scaledProjection();
    }

    @Redirect(
        method = "renderCustom",
        at = @At(
            value = "INVOKE",
            target = "Lmeteordevelopment/meteorclient/gui/renderer/GuiRenderer;renderTooltip(Lnet/minecraft/client/gui/DrawContext;DDD)Z"
        )
    )
    private boolean exeter$renderActiveTooltip(GuiRenderer renderer, DrawContext context, double mouseX, double mouseY, double delta) {
        if (!exeter$modals.isEmpty() || exeter$modalHost != null) {
            renderer.tooltip = null;
            return false;
        }

        return renderer.renderTooltip(context, mouseX, mouseY, delta);
    }

    @Override
    public void exeter$openModal(WidgetScreen modal) {
        WidgetScreen self = (WidgetScreen) (Object) this;
        if (modal == null || modal == self) return;

        ((WidgetScreenModalBridge) modal).exeter$setModalHost(self);
        modal.parent = self;
        modal.animProgress = 0;
        modal.init(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
        exeter$positionWindowTopLeftAtCursor(modal);

        exeter$modals.add(modal);
    }

    @Override
    public WidgetScreen exeter$getModalTarget() {
        WidgetScreen modal = exeter$topModal(exeter$modals);
        if (modal == null) return (WidgetScreen) (Object) this;
        return ((WidgetScreenModalBridge) modal).exeter$getModalTarget();
    }

    @Override
    public boolean exeter$hasModals() {
        return !exeter$modals.isEmpty();
    }

    @Override
    public void exeter$closeModal(WidgetScreen modal) {
        exeter$modals.remove(modal);
    }

    @Override
    public void exeter$setModalHost(WidgetScreen host) {
        exeter$modalHost = host;
    }

    @Override
    public GuiTheme exeter$getTheme() {
        return theme;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void exeter$mouseClicked(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        WidgetScreen modal = exeter$topModal(exeter$modals);
        if (modal == null) return;

        WidgetScreen target = ((WidgetScreenModalBridge) modal).exeter$getModalTarget();

        double s = mc.getWindow().getScaleFactor();
        double mouseX = click.x() * s;
        double mouseY = click.y() * s;

        if (!exeter$isInsideModalContent(target, mouseX, mouseY)) {
            target.close();
            cir.setReturnValue(true);
            return;
        }

        cir.setReturnValue(modal.mouseClicked(click, doubled));
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void exeter$mouseReleased(Click click, CallbackInfoReturnable<Boolean> cir) {
        WidgetScreen modal = exeter$topModal(exeter$modals);
        if (modal == null) return;
        cir.setReturnValue(modal.mouseReleased(click));
    }

    @Inject(method = "mouseMoved", at = @At("HEAD"), cancellable = true)
    private void exeter$mouseMoved(double mouseX, double mouseY, CallbackInfo ci) {
        WidgetScreen modal = exeter$topModal(exeter$modals);
        if (modal == null) return;
        modal.mouseMoved(mouseX, mouseY);
        ci.cancel();
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void exeter$mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        WidgetScreen modal = exeter$topModal(exeter$modals);
        if (modal == null) return;
        cir.setReturnValue(modal.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));
    }

    @Inject(method = "keyReleased", at = @At("HEAD"), cancellable = true)
    private void exeter$keyReleased(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        WidgetScreen modal = exeter$topModal(exeter$modals);
        if (modal == null) return;
        cir.setReturnValue(modal.keyReleased(input));
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void exeter$keyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        WidgetScreen modal = exeter$topModal(exeter$modals);
        if (modal == null) return;
        cir.setReturnValue(modal.keyPressed(input));
    }

    @Inject(method = "keyRepeated", at = @At("HEAD"), cancellable = true)
    private void exeter$keyRepeated(KeyInput input, CallbackInfo ci) {
        WidgetScreen modal = exeter$topModal(exeter$modals);
        if (modal == null) return;
        modal.keyRepeated(input);
        ci.cancel();
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void exeter$charTyped(CharInput input, CallbackInfoReturnable<Boolean> cir) {
        WidgetScreen modal = exeter$topModal(exeter$modals);
        if (modal == null) return;
        cir.setReturnValue(modal.charTyped(input));
    }

    @Inject(method = "renderCustom", at = @At("HEAD"))
    private void exeter$renderDarkening(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (exeter$modalHost != null || !exeter$modals.isEmpty() || !exeter$shouldRenderDarkening()) return;

        boolean scissorWasEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        if (scissorWasEnabled) GL11.glDisable(GL11.GL_SCISSOR_TEST);

        Utils.unscaledProjection();
        exeter$renderOverlay(context);
        Utils.scaledProjection();

        if (scissorWasEnabled) GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    @Inject(method = "renderCustom", at = @At("RETURN"))
    private void exeter$renderModals(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (exeter$modals.isEmpty()) return;

        Utils.unscaledProjection();
        WidgetScreen topModal = exeter$topModal(exeter$modals);

        if (exeter$shouldRenderDarkening()) {
            boolean scissorWasEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
            if (scissorWasEnabled) GL11.glDisable(GL11.GL_SCISSOR_TEST);

            exeter$renderOverlay(context);

            if (scissorWasEnabled) GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }

        for (WidgetScreen modal : new ArrayList<>(exeter$modals)) {
            exeter$clampWindowToScreen(modal);
            modal.tick();
            modal.renderCustom(context, mouseX, mouseY, delta);
        }

        if (topModal != null && !((WidgetScreenModalBridge) topModal).exeter$hasModals()) {
            exeter$renderModalTooltip(context, topModal, delta);
        }

        Utils.scaledProjection();
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void exeter$resizeModals(int width, int height, CallbackInfo ci) {
        for (WidgetScreen modal : exeter$modals) {
            modal.resize(width, height);
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void exeter$cleanupAndCloseModal(CallbackInfo ci) {
        WidgetScreen self = (WidgetScreen) (Object) this;

        if (!exeter$modals.isEmpty()) {
            for (WidgetScreen modal : new ArrayList<>(exeter$modals)) {
                ((WidgetScreenModalBridge) modal).exeter$setModalHost(null);
                modal.taskAfterRender = null;
                modal.removed();
            }

            exeter$modals.clear();
        }

        if (exeter$modalHost == null) return;
        if (!exeter$modals.isEmpty()) return;
        if (self.taskAfterRender == null) return;

        WidgetScreen host = exeter$modalHost;
        self.taskAfterRender = () -> {
            if (((WidgetScreenModalBridge) host).exeter$hasModals()) {
                ((WidgetScreenModalBridge) host).exeter$closeModal(self);
            }

            WidgetScreen target = ((WidgetScreenModalBridge) host).exeter$getModalTarget();
            exeter$syncHoverState(target);
        };
        exeter$modalHost = null;
    }

    @Inject(method = "closeInternal", at = @At("HEAD"), cancellable = true)
    private void exeter$closeInternal(CallbackInfo ci) {
        if (exeter$modalHost == null) return;

        WidgetScreen self = (WidgetScreen) (Object) this;
        self.taskAfterRender = () -> {
        };
        self.removed();
        ci.cancel();
    }
}
