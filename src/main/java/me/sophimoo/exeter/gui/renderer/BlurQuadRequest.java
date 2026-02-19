package me.sophimoo.exeter.gui.renderer;

import meteordevelopment.meteorclient.utils.render.color.Color;

public record BlurQuadRequest(double x, double y, double width, double height,
                              float u1, float v1, float u2, float v2,
                              Color backgroundColor) {}
