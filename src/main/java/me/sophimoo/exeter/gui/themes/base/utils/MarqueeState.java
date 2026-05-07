package me.sophimoo.exeter.gui.themes.base.utils;

public final class MarqueeState {
    private static final double DEFAULT_SPEED = 35;
    private static final double DEFAULT_EDGE_PAUSE = 0.4;

    private final double speed;
    private final double edgePause;

    private double offset;
    private int direction = 1;
    private double pause;

    public MarqueeState() {
        this(DEFAULT_SPEED, DEFAULT_EDGE_PAUSE);
    }

    public MarqueeState(double speed, double edgePause) {
        this.speed = speed;
        this.edgePause = edgePause;
    }

    public double step(double overflow, boolean animate, double delta) {
        if (overflow <= 0) {
            reset();
            return 0;
        }

        offset = Math.min(offset, overflow);

        if (animate) {
            if (pause > 0) pause = Math.max(0, pause - delta);
            else {
                offset += direction * delta * speed;

                if (offset >= overflow) {
                    offset = overflow;
                    direction = -1;
                    pause = edgePause;
                } else if (offset <= 0) {
                    offset = 0;
                    direction = 1;
                    pause = edgePause;
                }
            }
        } else if (offset > 0) {
            offset = Math.max(0, offset - delta * speed * 2);
            if (offset <= 0) {
                reset();
            }
        }

        return offset;
    }

    public void reset() {
        offset = 0;
        direction = 1;
        pause = 0;
    }
}
