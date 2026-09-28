package me.sophimoo.exeter.mixin.meteorclient;

import me.sophimoo.exeter.gui.widgets.ExeterStackedTable;
import me.sophimoo.exeter.gui.widgets.ExeterStackedLayout;
import me.sophimoo.exeter.gui.widgets.ExeterWidthConstrained;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = WTable.class, remap = false)
public abstract class WTableMixin extends WContainer implements ExeterStackedTable {
    @Shadow @Final private List<List<Cell<?>>> rows;

    @Shadow protected abstract double horizontalSpacing();

    @Shadow protected abstract double verticalSpacing();

    @Unique private boolean exeter$stacked;
    @Unique private boolean exeter$wrapped;
    @Unique private double exeter$wrapWidth;

    @Override
    public void exeter$setStacked(boolean stacked) {
        if (exeter$stacked == stacked) return;
        exeter$stacked = stacked;
        invalidate();
    }

    @Inject(method = "onCalculateSize", at = @At("RETURN"), cancellable = true)
    private void exeter$calculateStackedSize(CallbackInfo info) {
        if (!exeter$stacked) return;

        exeter$wrapWidth = ExeterStackedLayout.availableWidth(this);
        exeter$wrapped = width > exeter$wrapWidth;
        if (!exeter$wrapped) return;

        exeter$constrainCells();
        width = 0;
        height = 0;
        int lineCount = 0;

        for (List<Cell<?>> row : rows) {
            for (int start = 0; start < row.size();) {
                int end = ExeterStackedLayout.lineEnd(row, start, exeter$wrapWidth, horizontalSpacing());
                if (lineCount++ > 0) height += verticalSpacing();
                width = Math.max(width, ExeterStackedLayout.lineWidth(row, start, end, horizontalSpacing()));
                height += ExeterStackedLayout.lineHeight(row, start, end);
                start = end;
            }
        }

        info.cancel();
    }

    @Inject(method = "onCalculateWidgetPositions", at = @At("HEAD"), cancellable = true)
    private void exeter$calculateStackedWidgetPositions(CallbackInfo info) {
        if (!exeter$wrapped) return;

        double y = this.y;

        for (List<Cell<?>> row : rows) {
            for (int start = 0; start < row.size();) {
                int end = ExeterStackedLayout.lineEnd(row, start, exeter$wrapWidth, horizontalSpacing());
                double lineHeight = ExeterStackedLayout.lineHeight(row, start, end);
                double lineWidth = ExeterStackedLayout.lineWidth(row, start, end, horizontalSpacing());
                int expandCount = 0;
                for (int i = start; i < end; i++) if (row.get(i).expandCellX) expandCount++;

                double fillWidth = expandCount > 0 ? Math.max(0, (width - lineWidth) / expandCount) : 0;
                double x = this.x;
                for (int i = start; i < end; i++) {
                    Cell<?> cell = row.get(i);
                    if (i > start) x += horizontalSpacing();
                    cell.x = x;
                    cell.y = y;
                    cell.width = ExeterStackedLayout.cellWidth(cell) + (cell.expandCellX ? fillWidth : 0);
                    cell.height = lineHeight;
                    cell.alignWidget();
                    x += cell.width;
                }

                y += lineHeight + verticalSpacing();
                start = end;
            }
        }

        info.cancel();
    }

    @Unique
    private void exeter$constrainCells() {
        for (List<Cell<?>> row : rows) {
            for (Cell<?> cell : row) {
                if (cell.widget() instanceof ExeterWidthConstrained constrained) {
                    constrained.exeter$setMaxWidth(Math.max(0, exeter$wrapWidth - cell.padLeft() - cell.padRight()));
                    cell.widget().calculateSize();
                }
            }
        }
    }

}
