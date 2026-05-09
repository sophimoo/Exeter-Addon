package me.sophimoo.exeter.gui.renderer;

import meteordevelopment.meteorclient.gui.renderer.operations.TextOperation;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.misc.Pool;

public class ShadowTextOperation extends TextOperation {
    private static final Pool<ShadowTextOperation> POOL = new Pool<>(ShadowTextOperation::new);

    private String exeter$text;
    private TextRenderer exeter$renderer;
    private boolean exeter$shadow;

    public static ShadowTextOperation get() {
        return POOL.get();
    }

    public ShadowTextOperation exeter$set(String text, TextRenderer renderer, boolean title, boolean shadow) {
        exeter$text = text;
        exeter$renderer = renderer;
        this.title = title;
        exeter$shadow = shadow;
        return this;
    }

    @Override
    public void run(Pool<TextOperation> pool) {
        try {
            exeter$renderer.render(exeter$text, x, y, color, exeter$shadow);
        } finally {
            exeter$text = null;
            exeter$renderer = null;
            exeter$shadow = false;
            title = false;
            POOL.free(this);
        }
    }
}
