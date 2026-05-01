package me.sophimoo.exeter.mixin.meteorclient;

import me.sophimoo.exeter.gui.modal.WidgetScreenModalBridge;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import me.sophimoo.exeter.gui.themes.base.BaseGuiTheme;
@Mixin(value = WidgetScreen.class, remap = false)
public abstract class WidgetScreenModalMixin implements WidgetScreenModalBridge {
    @org.spongepowered.asm.mixin.Shadow
    protected GuiTheme theme;

    @Unique
    private final GuiRenderer exeter$overlayRenderer = new GuiRenderer();

    @Unique
    private static final Color exeter$overlayColor = new Color(0, 0, 0, 120);

    @Unique
    private final List<WidgetScreen> exeter$modals = new ArrayList<>(2);

    @Unique
    private WidgetScreen exeter$modalHost;

    @Unique
    private static WWidget exeter$getWindow(WidgetScreen screen) {
        if (!(screen instanceof WindowScreen windowScreen)) return null;
        return ((WindowScreenAccessor) windowScreen).exeter$getWindow();
    }

    @Unique
    private static void exeter$moveWindow(WWidget window, double targetX, double targetY) {
        ((WWindowAccessor) window).exeter$setMoved(true);
        ((WWindowAccessor) window).exeter$setMovedX(targetX);
        ((WWindowAccessor) window).exeter$setMovedY(targetY);
        window.move(targetX - window.x, targetY - window.y);
    }

    @Unique
    private static void exeter$positionWindowTopLeftAtCursor(WidgetScreen modal) {
        WWidget window = exeter$getWindow(modal);
        if (window == null) return;

        double targetX = Math.max(0, Math.min(mc.mouse.getX(), Utils.getWindowWidth() - window.width));
        double targetY = Math.max(0, Math.min(mc.mouse.getY(), Utils.getWindowHeight() - window.height));
        exeter$moveWindow(window, targetX, targetY);
    }

    @Unique
    private static void exeter$clampWindowToScreen(WidgetScreen modal) {
        WWidget window = exeter$getWindow(modal);
        if (window == null) return;

        double maxX = Math.max(0, Utils.getWindowWidth() - window.width);
        double maxY = Math.max(0, Utils.getWindowHeight() - window.height);

        double clampedX = Math.max(0, Math.min(window.x, maxX));
        double clampedY = Math.max(0, Math.min(window.y, maxY));

        if (clampedX != window.x || clampedY != window.y) {
            exeter$moveWindow(window, clampedX, clampedY);
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

        double scale = mc.getWindow().getScaleFactor();
        if (scale <= 0) return;

        double mouseX = mc.mouse.getX() / scale;
        double mouseY = mc.mouse.getY() / scale;
        screen.mouseMoved(mouseX, mouseY);
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

    @Inject(method = "renderCustom", at = @At("RETURN"))
    private void exeter$renderModals(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (exeter$modals.isEmpty()) return;

        boolean scissorWasEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        Utils.unscaledProjection();

        if (!(theme instanceof BaseGuiTheme baseTheme) || baseTheme.modalDarkening.get()) {
            exeter$overlayRenderer.theme = theme;
            exeter$overlayRenderer.begin(context);
            exeter$overlayRenderer.quad(0, 0, Utils.getWindowWidth(), Utils.getWindowHeight(), exeter$overlayColor);
            exeter$overlayRenderer.end();
        }

        Utils.scaledProjection();
        if (scissorWasEnabled) GL11.glEnable(GL11.GL_SCISSOR_TEST);

        for (WidgetScreen modal : new ArrayList<>(exeter$modals)) {
            exeter$clampWindowToScreen(modal);
            modal.tick();
            modal.renderCustom(context, mouseX, mouseY, delta);
        }
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
