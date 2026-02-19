package me.sophimoo.exeter.gui.renderer;

import com.mojang.blaze3d.textures.GpuTextureView;
import meteordevelopment.meteorclient.utils.render.color.Color;

public interface BlurRendererAccess {
    void blurredQuad(double x, double y, double width, double height,
                     GpuTextureView blurredTexture, Color backgroundColor);
}
