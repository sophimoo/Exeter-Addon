package me.sophimoo.exeter.gui.themes.base.widgets;
import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;
import java.util.ArrayList;
import java.util.List;

public class WBaseMultiLabel extends WMultiLabel implements BaseWidget {

    protected List<String> lines = new ArrayList<>(2);
    public WBaseMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title, maxWidth);
    }

    @Override
    protected void onCalculateSize() {
        lines.clear();
        double maxWidth = this.maxWidth == 0 ? Double.POSITIVE_INFINITY : theme.scale(this.maxWidth);
        double spaceWidth = theme.textWidth(" ", 1, title);
        double widestLine = 0;

        for (String textLine : text.split("\n")) {
            StringBuilder line = new StringBuilder();
            double lineWidth = 0;

            for (String word : textLine.split(" ")) {
                double wordWidth = theme.textWidth(word, word.length(), title);
                double separatorWidth = line.isEmpty() ? 0 : spaceWidth;

                if (lineWidth + separatorWidth + wordWidth > maxWidth) {
                    lines.add(line.toString());
                    widestLine = Math.max(widestLine, lineWidth);
                    line.setLength(0);
                    lineWidth = 0;
                    separatorWidth = 0;
                }

                if (!line.isEmpty()) line.append(' ');
                line.append(word);
                lineWidth += separatorWidth + wordWidth;
            }

            lines.add(line.toString());
            widestLine = Math.max(widestLine, lineWidth);
        }

        width = widestLine;
        height = theme.textHeight(title) * lines.size();
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = theme.textHeight(title);
        Color lineColor = color != null ? color : theme().textColor.get();
        for (int i = 0; i < lines.size(); i++) {
            renderText(renderer, lines.get(i), x, y + h * i, lineColor);
        }
    }

}
