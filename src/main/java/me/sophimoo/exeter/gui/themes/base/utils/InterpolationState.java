package me.sophimoo.exeter.gui.themes.base.utils;

public final class InterpolationState {
    private static final long HoverGraceMs = 250;
    private static final double MorphSpeedMultiplier = 2.0;

    private double fromX, fromY, fromW, fromH;
    private double toX, toY, toW, toH;
    private double currentX, currentY, currentW, currentH;

    private double transitionProgress;
    private double fadeProgress;
    private boolean fadingOut;
    private boolean visible;
    private boolean hasTarget;

    private long lastUpdateMs = -1;
    private long lastNotifyMs = -1;

    public void notifyHover(double x, double y, double w, double h, boolean hovered) {
        if (!hovered) return;

        lastNotifyMs = System.currentTimeMillis();
        fadingOut = false;

        if (!hasTarget) {
            setCurrentBounds(x, y, w, h);
            transitionProgress = 1.0;
            fadeProgress = 0.0;
            visible = true;
            hasTarget = true;
        } else if (toX != x || toY != y || toW != w || toH != h) {
            setFromCurrentBounds();
            setTargetBounds(x, y, w, h);
            transitionProgress = 0.0;
        }
    }

    public void update(double delta, double morphSpeed, double fadeInSpeed, double fadeOutSpeed) {
        long now = System.currentTimeMillis();
        if (now == lastUpdateMs) return;
        lastUpdateMs = now;

        boolean isHovered = now - lastNotifyMs < HoverGraceMs;

        if (!isHovered) {
            fadingOut = true;
        }

        if (!fadingOut && fadeInSpeed == 0) {
            fadeProgress = 1.0;
        } else if (fadingOut && fadeOutSpeed == 0) {
            fadeProgress = 0.0;
        } else if (fadingOut) {
            fadeProgress = Math.max(0.0, fadeProgress - fadeOutSpeed * delta);
        } else {
            fadeProgress = Math.min(1.0, fadeProgress + fadeInSpeed * delta);
        }

        if (fadeProgress <= 0 && fadingOut) {
            visible = false;
            hasTarget = false;
            return;
        }

        visible = true;

        if (transitionProgress < 1.0) {
            transitionProgress = Math.min(1.0, transitionProgress + morphSpeed * delta * MorphSpeedMultiplier);
        }

        double eased = 1.0 - Math.pow(1.0 - transitionProgress, 3);
        currentX = fromX + (toX - fromX) * eased;
        currentY = fromY + (toY - fromY) * eased;
        currentW = fromW + (toW - fromW) * eased;
        currentH = fromH + (toH - fromH) * eased;
    }

    public double getFadeProgress() {
        return visible ? fadeProgress : 0.0;
    }

    public double[] getIntersection(double x, double y, double w, double h) {
        if (!visible) return null;
        double ix = Math.max(currentX, x);
        double iy = Math.max(currentY, y);
        double iw = Math.min(currentX + currentW, x + w) - ix;
        double ih = Math.min(currentY + currentH, y + h) - iy;
        if (iw <= 0.0 || ih <= 0.0) return null;
        return new double[] { ix, iy, iw, ih };
    }

    private void setCurrentBounds(double x, double y, double w, double h) {
        currentX = fromX = toX = x;
        currentY = fromY = toY = y;
        currentW = fromW = toW = w;
        currentH = fromH = toH = h;
    }

    private void setFromCurrentBounds() {
        fromX = currentX;
        fromY = currentY;
        fromW = currentW;
        fromH = currentH;
    }

    private void setTargetBounds(double x, double y, double w, double h) {
        toX = x;
        toY = y;
        toW = w;
        toH = h;
    }
}
