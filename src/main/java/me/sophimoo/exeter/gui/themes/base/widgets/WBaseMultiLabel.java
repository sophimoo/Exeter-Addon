package me.sophimoo.exeter.gui.themes.base.widgets;

import me.sophimoo.exeter.gui.themes.base.BaseWidget;
import me.sophimoo.exeter.gui.widgets.ExeterWidthConstrained;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WMultiLabel;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.ArrayList;
import java.util.List;

public class WBaseMultiLabel extends WMultiLabel implements BaseWidget, ExeterWidthConstrained {
    protected List<String> lines = new ArrayList<>(2);
    private double wrapWidth;

    public WBaseMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title, maxWidth);
    }

    @Override
    public void exeter$setMaxWidth(double maxWidth) {
        wrapWidth = maxWidth;
    }

    @Override
    protected void onCalculateSize() {
        width = calculateLines(theme, text, title, resolveMaxWidth(), lines);
        height = theme.textHeight(title) * Math.max(lines.size(), 1);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = theme.textHeight(title);
        Color lineColor = color != null ? color : (title ? theme().titleTextColor.get() : theme().textColor.get());
        for (int i = 0; i < lines.size(); i++) {
            renderText(renderer, lines.get(i), x, y + h * i, lineColor);
        }
    }

    private double resolveMaxWidth() {
        if (maxWidth > 0) return theme.scale(maxWidth);
        if (wrapWidth > 0) return wrapWidth;
        return Double.POSITIVE_INFINITY;
    }

    static double calculateLines(GuiTheme theme, String text, boolean title, double maxWidth, List<String> lines) {
        lines.clear();
        if (Double.isInfinite(maxWidth)) return addUnwrappedLines(theme, text, title, lines);

        double spaceWidth = theme.textWidth(" ", 1, title);
        double hyphenWidth = theme.textWidth("-", 1, title);
        double widestLine = 0;

        for (String textLine : text.split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            double lineWidth = 0;

            for (String word : textLine.split(" ")) {
                if (word.isEmpty()) continue;

                double wordWidth = theme.textWidth(word, word.length(), title);
                double separatorWidth = line.isEmpty() ? 0 : spaceWidth;

                if (lineWidth + separatorWidth + wordWidth > maxWidth) {
                    if (!line.isEmpty()) {
                        widestLine = addLine(lines, line.toString(), lineWidth, widestLine);
                        line.setLength(0);
                        lineWidth = 0;
                    }

                    if (wordWidth > maxWidth) {
                        widestLine = wrapLongWord(theme, title, lines, word, maxWidth, hyphenWidth, widestLine, line);
                        lineWidth = theme.textWidth(line.toString(), line.length(), title);
                        continue;
                    }
                }

                if (!line.isEmpty()) line.append(' ');
                line.append(word);
                lineWidth += (line.length() == word.length() ? 0 : spaceWidth) + wordWidth;
            }

            widestLine = addLine(lines, line.toString(), lineWidth, widestLine);
        }

        return widestLine;
    }

    private static double addUnwrappedLines(GuiTheme theme, String text, boolean title, List<String> lines) {
        double widestLine = 0;

        for (String line : text.split("\n", -1)) {
            lines.add(line);
            widestLine = Math.max(widestLine, theme.textWidth(line, line.length(), title));
        }

        return widestLine;
    }

    private static double addLine(List<String> lines, String line, double lineWidth, double widestLine) {
        lines.add(line);
        return Math.max(widestLine, lineWidth);
    }

    private static double wrapLongWord(GuiTheme theme, boolean title, List<String> lines, String word, double maxWidth, double hyphenWidth, double widestLine, StringBuilder line) {
        int start = 0;

        while (start < word.length()) {
            int end = fittingWordEnd(theme, title, word, start, maxWidth, hyphenWidth);
            boolean continued = end < word.length();
            String segment = word.substring(start, end) + (continued ? "-" : "");
            double segmentWidth = theme.textWidth(segment, segment.length(), title);

            if (continued) widestLine = addLine(lines, segment, segmentWidth, widestLine);
            else line.append(segment);

            start = end;
        }

        return widestLine;
    }

    private static int fittingWordEnd(GuiTheme theme, boolean title, String word, int start, double maxWidth, double hyphenWidth) {
        int bestEnd = Math.min(start + 1, word.length());

        for (int end = start + 1; end <= word.length(); end++) {
            boolean continued = end < word.length();
            double width = theme.textWidth(word, end, title) - theme.textWidth(word, start, title);
            if (continued) width += hyphenWidth;

            if (width > maxWidth) break;
            bestEnd = end;
        }

        return bestEnd;
    }

}
