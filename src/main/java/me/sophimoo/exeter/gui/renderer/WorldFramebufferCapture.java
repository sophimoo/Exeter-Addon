package me.sophimoo.exeter.gui.renderer;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.ResolutionChangedEvent;
import meteordevelopment.meteorclient.events.render.RenderAfterWorldEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.renderer.FixedUniformStorage;
import meteordevelopment.meteorclient.renderer.MeshRenderer;
import meteordevelopment.meteorclient.renderer.MeteorRenderPipelines;
import meteordevelopment.orbit.listeners.ConsumerListener;
import net.minecraft.client.gl.DynamicUniformStorage;

import java.nio.ByteBuffer;

import static meteordevelopment.meteorclient.MeteorClient.mc;

/**
 * Captures the world framebuffer before GUI rendering for use in widget background blur.
 */
public class WorldFramebufferCapture {
    private static WorldFramebufferCapture INSTANCE;

    private GpuTextureView[] blurFbos;
    private GpuBufferSlice[] blurUbos;

    private int blurIterations;
    private float blurOffset;
    private boolean initialized = false;
    private boolean capturedThisTick = false;
    private float previousOffset = -1;

    private ConsumerListener<RenderAfterWorldEvent> renderListener;
    private ConsumerListener<ResolutionChangedEvent> resolutionListener;
    private ConsumerListener<GameLeftEvent> gameLeftListener;
    private ConsumerListener<TickEvent.Post> tickListener;

    private static final int UNIFORM_SIZE = new Std140SizeCalculator()
        .putVec2()
        .putFloat()
        .get();

    private static final int MAX_BLUR_ITERATIONS = 10;
    private static final FixedUniformStorage<BlurUniformData> UNIFORM_STORAGE =
        new FixedUniformStorage<>("BaseTheme Blur UBO", UNIFORM_SIZE, MAX_BLUR_ITERATIONS + 1);

    private float blurScale;

    private record BlurUniformData(float halfTexelSizeX, float halfTexelSizeY, float offset)
        implements DynamicUniformStorage.Uploadable {
        @Override
        public void write(ByteBuffer buffer) {
            Std140Builder.intoBuffer(buffer)
                .putVec2(halfTexelSizeX, halfTexelSizeY)
                .putFloat(offset);
        }
    }

    public WorldFramebufferCapture(int iterations, float offset, float scale) {
        this.blurIterations = Math.min(iterations, MAX_BLUR_ITERATIONS);
        this.blurOffset = offset;
        this.blurScale = scale;

        renderListener = new ConsumerListener<>(RenderAfterWorldEvent.class, this::onRenderAfterWorld);
        resolutionListener = new ConsumerListener<>(ResolutionChangedEvent.class, this::onResolutionChanged);
        gameLeftListener = new ConsumerListener<>(GameLeftEvent.class, this::onGameLeft);
        tickListener = new ConsumerListener<>(TickEvent.Post.class, this::onTickPost);

        MeteorClient.EVENT_BUS.subscribe(renderListener);
        MeteorClient.EVENT_BUS.subscribe(resolutionListener);
        MeteorClient.EVENT_BUS.subscribe(gameLeftListener);
        MeteorClient.EVENT_BUS.subscribe(tickListener);

        INSTANCE = this;
    }

    public void updateSettings(int iterations, float offset, float scale) {
        int clampedIterations = Math.min(iterations, MAX_BLUR_ITERATIONS);
        boolean needsReinit = this.blurIterations != clampedIterations || this.blurScale != scale;
        this.blurIterations = clampedIterations;
        this.blurOffset = offset;
        this.blurScale = scale;
        previousOffset = -1; // Force uniform update

        if (needsReinit && initialized) {
            for (int i = 0; i < blurFbos.length; i++) {
                if (blurFbos[i] != null) {
                    blurFbos[i].close();
                }
            }
            blurFbos = new GpuTextureView[blurIterations + 1];
            for (int i = 0; i < blurFbos.length; i++) {
                blurFbos[i] = createBlurFbo(i);
            }
        }
    }

    private void ensureInitialized() {
        if (initialized) return;

        int fbWidth = mc.getWindow().getFramebufferWidth();
        int fbHeight = mc.getWindow().getFramebufferHeight();

        if (fbWidth <= 0 || fbHeight <= 0) {
            return;
        }

        blurFbos = new GpuTextureView[blurIterations + 1];
        for (int i = 0; i < blurFbos.length; i++) {
            blurFbos[i] = createBlurFbo(i);
        }

        previousOffset = -1;
        initialized = true;
    }

    private void onRenderAfterWorld(RenderAfterWorldEvent event) {
        if (!(mc.currentScreen instanceof WidgetScreen)) {
            return;
        }

        ensureInitialized();
        if (!initialized) return;

        if (blurIterations > 0) {
            applyBlur();
        }

        capturedThisTick = true;
    }

    private void onTickPost(TickEvent.Post event) {
        capturedThisTick = false;
    }

    private void onGameLeft(GameLeftEvent event) {
        capturedThisTick = false;
    }

    private void onResolutionChanged(ResolutionChangedEvent event) {
        if (!initialized) return;

        for (int i = 0; i < blurFbos.length; i++) {
            if (blurFbos[i] != null) {
                blurFbos[i].close();
            }
            blurFbos[i] = createBlurFbo(i);
        }

        previousOffset = -1;
    }

    private void applyBlur() {
        if (blurFbos == null || blurFbos[0] == null) return;

        if (previousOffset != blurOffset) {
            updateBlurUniforms();
            previousOffset = blurOffset;
        }

        // Render directly from framebuffer like meteor's Blur module does
        GpuTextureView sourceView = mc.getFramebuffer().getColorAttachmentView();

        // Downsample - directly from framebuffer
        renderToFbo(blurFbos[0], sourceView, MeteorRenderPipelines.BLUR_DOWN, blurUbos[0]);

        for (int i = 0; i < blurIterations - 1; i++) {
            renderToFbo(blurFbos[i + 1], blurFbos[i], MeteorRenderPipelines.BLUR_DOWN, blurUbos[i + 1]);
        }

        // Upsample
        for (int i = blurIterations - 1; i >= 1; i--) {
            renderToFbo(blurFbos[i - 1], blurFbos[i], MeteorRenderPipelines.BLUR_UP, blurUbos[i - 1]);
        }
    }

    private void renderToFbo(GpuTextureView target, GpuTextureView source,
                            RenderPipeline pipeline, GpuBufferSlice ubo) {
        MeshRenderer.begin()
            .attachments(target, null)
            .pipeline(pipeline)
            .fullscreen()
            .uniform("BlurData", ubo)
            .sampler("u_Texture", source, RenderSystem.getSamplerCache().get(FilterMode.LINEAR))
            .end();
    }

    private void updateBlurUniforms() {
        if (blurFbos == null) return;

        UNIFORM_STORAGE.clear();

        BlurUniformData[] uboData = new BlurUniformData[blurIterations + 1];
        for (int i = 0; i < uboData.length; i++) {
            if (blurFbos[i] == null) continue;

            GpuTextureView fbo = blurFbos[i];
            uboData[i] = new BlurUniformData(
                0.5f / fbo.getWidth(0),
                0.5f / fbo.getHeight(0),
                blurOffset
            );
        }

        blurUbos = UNIFORM_STORAGE.writeAll(uboData);
    }

    private GpuTextureView createBlurFbo(int level) {
        int baseWidth = (int) (mc.getWindow().getFramebufferWidth() * blurScale);
        int baseHeight = (int) (mc.getWindow().getFramebufferHeight() * blurScale);

        double scale = 1 / Math.pow(2, level);

        int width = (int) (baseWidth * scale);
        int height = (int) (baseHeight * scale);

        width = Math.max(1, width);
        height = Math.max(1, height);

        GpuTexture texture = RenderSystem.getDevice().createTexture(
            "BaseTheme Blur FBO " + level,
            15,
            TextureFormat.RGBA8,
            width,
            height,
            1,
            1
        );

        return RenderSystem.getDevice().createTextureView(texture);
    }

    /**
     * Gets the blurred world texture for sampling during GUI rendering.
     * Returns null if capture hasn't happened yet this frame or if not in-game.
     */
    public GpuTextureView getBlurredTexture() {
        if (!initialized) return null;
        if (!capturedThisTick) return null;
        if (!(mc.currentScreen instanceof WidgetScreen)) return null;

        if (blurIterations > 0 && blurFbos != null && blurFbos.length > 0 && blurFbos[0] != null) {
            return blurFbos[0];
        }
        return null;
    }

    /**
     * Gets the capture instance. Returns null if not initialized.
     */
    public static WorldFramebufferCapture getInstance() {
        return INSTANCE;
    }

    public void close() {
        initialized = false;
        previousOffset = -1;

        if (renderListener != null) {
            MeteorClient.EVENT_BUS.unsubscribe(renderListener);
        }
        if (resolutionListener != null) {
            MeteorClient.EVENT_BUS.unsubscribe(resolutionListener);
        }
        if (gameLeftListener != null) {
            MeteorClient.EVENT_BUS.unsubscribe(gameLeftListener);
        }
        if (tickListener != null) {
            MeteorClient.EVENT_BUS.unsubscribe(tickListener);
        }

        if (blurFbos != null) {
            for (int i = 0; i < blurFbos.length; i++) {
                if (blurFbos[i] != null) {
                    blurFbos[i].close();
                    blurFbos[i] = null;
                }
            }
        }

        if (INSTANCE == this) {
            INSTANCE = null;
        }
    }
}
