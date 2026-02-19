package me.sophimoo.exeter.mixin.meteorclient;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import me.sophimoo.exeter.gui.renderer.BlurQuadRequest;
import me.sophimoo.exeter.gui.renderer.BlurRendererAccess;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.Scissor;
import meteordevelopment.meteorclient.gui.renderer.operations.TextOperation;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.render.color.Color;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(value = GuiRenderer.class, remap = false)
public abstract class GuiRendererBlurMixin implements BlurRendererAccess {
    @Shadow
    @Final
    private Renderer2D r;
    @Shadow
    @Final
    private Renderer2D rTex;
    @Shadow
    @Final
    private List<TextOperation> texts;
    @Shadow
    @Final
    private List<Runnable> postTasks;
    @Shadow
    @Final
    private it.unimi.dsi.fastutil.Stack<Scissor> scissorStack;
    @Shadow
    private static Texture TEXTURE;

    @Shadow
    public abstract void beginRender();

    @Unique
    private static final Color WHITE = new Color(255, 255, 255);

    @Unique
    private final List<BlurQuadRequest> blurQueue = new ArrayList<>();

    @Unique
    private GpuTextureView currentBlurTexture = null;

    @Unique
    private boolean isFlushingBlur = false;

    @Unique
    private void flushBlurredQuads() {
        if (blurQueue.isEmpty() || currentBlurTexture == null || isFlushingBlur) return;

        isFlushingBlur = true;
        try {
            boolean hasScissor = !scissorStack.isEmpty();
            Scissor activeScissor = hasScissor ? scissorStack.top() : null;

            if (activeScissor != null) activeScissor.push();
            r.end();
            rTex.end();
            r.render();
            rTex.render("u_Texture", TEXTURE.getGlTextureView(), TEXTURE.getSampler());
            if (activeScissor != null) activeScissor.pop();

            if (hasScissor) {
                activeScissor.push();
            }

            rTex.begin();
            for (BlurQuadRequest request : blurQueue) {
                rTex.texQuad(request.x(), request.y(), request.width(), request.height(),
                        0, request.u1(), request.v1(), request.u2(), request.v2(), WHITE);
            }
            rTex.end();
            rTex.render(currentBlurTexture, RenderSystem.getSamplerCache().get(FilterMode.LINEAR));

            if (hasScissor) {
                activeScissor.pop();
            }

            if (hasScissor) {
                activeScissor.push();
            }
            r.begin();
            for (BlurQuadRequest request : blurQueue) {
                if (request.backgroundColor().a > 0) {
                    r.quad(request.x(), request.y(), request.width(), request.height(), request.backgroundColor());
                }
            }
            r.end();
            r.render();
            if (hasScissor) {
                activeScissor.pop();
            }

            beginRender();

            blurQueue.clear();
        } finally {
            isFlushingBlur = false;
        }
    }

    @Inject(method = "end", at = @At("HEAD"))
    private void onEnd(CallbackInfo ci) {
        flushBlurredQuads();
        currentBlurTexture = null;
    }

    @Inject(method = "endRender(Lmeteordevelopment/meteorclient/gui/renderer/Scissor;)V", at = @At("HEAD"))
    private void onEndRender(Scissor scissor, CallbackInfo ci) {
        if (!isFlushingBlur) {
            flushBlurredQuads();
        }
    }

    @Inject(method = "scissorEnd", at = @At("HEAD"))
    private void onScissorEnd(CallbackInfo ci) {
        flushBlurredQuads();
    }

    @Inject(method = "quad(DDDDLmeteordevelopment/meteorclient/utils/render/color/Color;)V", at = @At("HEAD"))
    private void onQuad(double x, double y, double width, double height, Color color, CallbackInfo ci) {
        flushBlurredQuads();
    }

    @Override
    public void blurredQuad(double x, double y, double width, double height,
                           GpuTextureView blurredTexture, Color backgroundColor) {
        if (blurredTexture == null) {
            flushBlurredQuads();
            r.quad(x, y, width, height, backgroundColor);
            return;
        }

        if (currentBlurTexture != null && currentBlurTexture != blurredTexture) {
            flushBlurredQuads();
        }
        currentBlurTexture = blurredTexture;

        int screenWidth = mc.getWindow().getFramebufferWidth();
        int screenHeight = mc.getWindow().getFramebufferHeight();

        float u1 = (float) (x / screenWidth);
        float v1 = 1.0f - (float) (y / screenHeight);
        float u2 = (float) ((x + width) / screenWidth);
        float v2 = 1.0f - (float) ((y + height) / screenHeight);

        blurQueue.add(new BlurQuadRequest(x, y, width, height, u1, v1, u2, v2, backgroundColor));
    }
}
