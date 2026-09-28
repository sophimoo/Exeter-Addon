package me.sophimoo.exeter.mixin.meteorclient;

import me.sophimoo.exeter.gui.widgets.ExeterStackedLayout;
import me.sophimoo.exeter.gui.widgets.ExeterWidthConstrained;
import me.sophimoo.exeter.gui.widgets.ExeterWrappingList;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WHorizontalList.class, remap = false)
public abstract class WHorizontalListMixin extends WContainer implements ExeterWrappingList {
    @Shadow protected double calculatedWidth;
    @Shadow protected int fillXCount;
    @Shadow protected abstract double spacing();

    @Unique private boolean exeter$wrapping;
    @Unique private boolean exeter$stacked;
    @Unique private double exeter$wrapWidth;

    @Override
    public void exeter$setWrapping(boolean wrapping) {
        if (exeter$wrapping == wrapping) return;
        exeter$wrapping = wrapping;
        invalidate();
    }

    @Inject(method = "onCalculateSize", at = @At("RETURN"))
    private void exeter$calculateWrappedSize(CallbackInfo info) {
        if (!exeter$wrapping) return;

        exeter$wrapWidth = ExeterStackedLayout.availableWidth(this);
        exeter$applyChildMaxWidths(0);
        exeter$calculateHorizontalSize();

        exeter$stacked = exeter$wrapWidth > 0 && width > exeter$wrapWidth;
        if (!exeter$stacked) return;

        exeter$applyChildMaxWidths(exeter$wrapWidth);
        width = 0;
        height = 0;

        for (int start = 0, line = 0; start < cells.size(); line++) {
            int end = ExeterStackedLayout.lineEnd(cells, start, exeter$wrapWidth, spacing());
            if (line > 0) height += spacing();
            width = Math.max(width, ExeterStackedLayout.lineWidth(cells, start, end, spacing()));
            height += ExeterStackedLayout.lineHeight(cells, start, end);
            start = end;
        }
    }

    @Inject(method = "onCalculateWidgetPositions", at = @At("HEAD"), cancellable = true)
    private void exeter$calculateWrappedPositions(CallbackInfo info) {
        if (!exeter$stacked) return;

        double y = this.y;
        for (int start = 0; start < cells.size();) {
            int end = ExeterStackedLayout.lineEnd(cells, start, exeter$wrapWidth, spacing());
            double rowHeight = ExeterStackedLayout.lineHeight(cells, start, end);
            double rowWidth = ExeterStackedLayout.lineWidth(cells, start, end, spacing());
            int expandCount = 0;
            for (int i = start; i < end; i++) if (cells.get(i).expandCellX) expandCount++;

            double fillWidth = expandCount > 0 ? Math.max(0, (width - rowWidth) / expandCount) : 0;
            double x = this.x;

            for (int i = start; i < end; i++) {
                Cell<?> cell = cells.get(i);
                if (i > start) x += spacing();
                cell.x = x + cell.padLeft();
                cell.y = y + cell.padTop();
                cell.width = cell.widget().width + (cell.expandCellX ? fillWidth : 0);
                cell.height = rowHeight - cell.padTop() - cell.padBottom();
                cell.alignWidget();
                x += ExeterStackedLayout.cellWidth(cell);
            }

            y += rowHeight + spacing();
            start = end;
        }

        info.cancel();
    }

    @Unique
    private void exeter$calculateHorizontalSize() {
        width = 0;
        height = 0;
        fillXCount = 0;

        for (int i = 0; i < cells.size(); i++) {
            Cell<?> cell = cells.get(i);

            if (i > 0) width += spacing();

            width += ExeterStackedLayout.cellWidth(cell);
            height = Math.max(height, ExeterStackedLayout.cellHeight(cell));

            if (cell.expandCellX) fillXCount++;
        }

        calculatedWidth = width;
    }

    @Unique
    private void exeter$applyChildMaxWidths(double availableWidth) {
        for (Cell<?> cell : cells) {
            if (cell.widget() instanceof ExeterWidthConstrained constrained) {
                constrained.exeter$setMaxWidth(Math.max(0, availableWidth - cell.padLeft() - cell.padRight()));
                cell.widget().calculateSize();
            }
        }
    }

}
