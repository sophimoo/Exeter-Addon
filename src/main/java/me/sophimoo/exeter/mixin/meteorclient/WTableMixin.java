package me.sophimoo.exeter.mixin.meteorclient;

import me.sophimoo.exeter.gui.widgets.ExeterStackedTable;
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

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;

@Mixin(value = WTable.class, remap = false)
public abstract class WTableMixin extends WContainer implements ExeterStackedTable {
    @Shadow @Final private List<List<Cell<?>>> rows;

    @Shadow protected abstract double horizontalSpacing();

    @Shadow protected abstract double verticalSpacing();

    @Unique private boolean exeter$stacked;
    @Unique private boolean exeter$wrapped;
    @Unique private double exeter$wrapWidth;
    @Unique private final List<Double> exeter$columnWidths = new ArrayList<>();

    @Override
    public void exeter$setStacked(boolean stacked) {
        if (exeter$stacked == stacked) return;
        exeter$stacked = stacked;
        invalidate();
    }

    @Inject(method = "onCalculateSize", at = @At("RETURN"), cancellable = true)
    private void exeter$calculateStackedSize(CallbackInfo info) {
        if (!exeter$stacked) return;

        exeter$wrapWidth = exeter$availableWidth();
        exeter$wrapped = exeter$wrapWidth > 0 && width > exeter$wrapWidth;
        if (!exeter$wrapped) return;

        width = 0;
        height = 0;
        exeter$calculateColumnWidths();

        for (int i = 0; i < rows.size(); i++) {
            List<Cell<?>> row = rows.get(i);
            if (i > 0) height += verticalSpacing();

            int start = 0;
            while (start < row.size()) {
                int end = exeter$lineEnd(row, start, exeter$wrapWidth);
                if (start > 0) height += verticalSpacing();
                width = Math.max(width, exeter$lineWidth(row, start, end));
                height += exeter$lineHeight(row, start, end);
                start = end;
            }
        }

        info.cancel();
    }

    @Inject(method = "onCalculateWidgetPositions", at = @At("HEAD"), cancellable = true)
    private void exeter$calculateStackedWidgetPositions(CallbackInfo info) {
        if (!exeter$wrapped) return;

        double y = this.y;

        for (int i = 0; i < rows.size(); i++) {
            List<Cell<?>> row = rows.get(i);
            if (i > 0) y += verticalSpacing();

            int start = 0;
            while (start < row.size()) {
                int end = exeter$lineEnd(row, start, exeter$wrapWidth);
                if (start > 0) y += verticalSpacing();
                double lineHeight = exeter$lineHeight(row, start, end);
                exeter$positionLine(row, start, end, y, lineHeight);
                y += lineHeight;
                start = end;
            }
        }

        info.cancel();
    }

    @Unique
    private void exeter$positionLine(List<Cell<?>> row, int start, int end, double lineY, double lineHeight) {
        double spacing = horizontalSpacing();
        int expandCount = 0;

        for (int i = start; i < end; i++) if (row.get(i).expandCellX) expandCount++;

        double naturalWidth = exeter$lineWidth(row, start, end);
        double expandXAdd = expandCount > 0 ? Math.max(0, (width - naturalWidth) / expandCount) : 0;
        double x = this.x;

        for (int i = start; i < end; i++) {
            Cell<?> cell = row.get(i);
            if (i > start) x += spacing;

            double cellWidth = exeter$cellWidth(i, cell) + (cell.expandCellX ? expandXAdd : 0);
            cell.x = x;
            cell.y = lineY;
            cell.width = cellWidth;
            cell.height = lineHeight;
            cell.alignWidget();

            x += cellWidth;
        }
    }

    @Unique
    private int exeter$lineEnd(List<Cell<?>> row, int start, double maxWidth) {
        double width = 0;
        for (int end = start; end < row.size(); end++) {
            double nextWidth = exeter$cellWidth(end, row.get(end));
            if (end > start) nextWidth += horizontalSpacing();
            if (end > start && width + nextWidth > maxWidth) return end;
            width += nextWidth;
        }

        return row.size();
    }

    @Unique
    private double exeter$availableWidth() {
        double availableWidth = getWindowWidth();

        WContainer ancestor = parent instanceof WContainer container ? container : null;
        while (ancestor != null) {
            if (ancestor.width > 0) availableWidth = Math.min(availableWidth, ancestor.width);
            ancestor = ancestor.parent instanceof WContainer container ? container : null;
        }

        return Math.max(1, availableWidth);
    }

    @Unique
    private double exeter$lineWidth(List<Cell<?>> line, int start, int end) {
        double width = 0;
        for (int i = start; i < end; i++) {
            if (i > start) width += horizontalSpacing();
            width += exeter$cellWidth(i, line.get(i));
        }

        return width;
    }

    @Unique
    private double exeter$lineHeight(List<Cell<?>> line, int start, int end) {
        double height = 0;
        for (int i = start; i < end; i++) height = Math.max(height, exeter$cellHeight(line.get(i)));
        return height;
    }

    @Unique
    private double exeter$cellWidth(int column, Cell<?> cell) {
        return column < exeter$columnWidths.size()
            ? exeter$columnWidths.get(column)
            : cell.padLeft() + cell.widget().width + cell.padRight();
    }

    @Unique
    private double exeter$cellHeight(Cell<?> cell) {
        return cell.padTop() + cell.widget().height + cell.padBottom();
    }

    @Unique
    private void exeter$calculateColumnWidths() {
        exeter$columnWidths.clear();

        for (List<Cell<?>> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                Cell<?> cell = row.get(i);
                double cellWidth = cell.padLeft() + cell.widget().width + cell.padRight();

                if (i >= exeter$columnWidths.size()) exeter$columnWidths.add(cellWidth);
                else if (cellWidth > exeter$columnWidths.get(i)) exeter$columnWidths.set(i, cellWidth);
            }
        }
    }
}
