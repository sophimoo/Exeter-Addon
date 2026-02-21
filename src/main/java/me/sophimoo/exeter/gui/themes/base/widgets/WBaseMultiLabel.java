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
        String[] textLines = text.split("\n");
        double maxLineWidth = 0;
        if (this.maxWidth == 0) {
            for (String line : textLines) {
                lines.add(line);
                double lineWidth = theme.textWidth(line, line.length(), title);
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            double lineWidth = 0;
            double spaceWidth = theme.textWidth(" ", 1, title);
            double maxWidth = theme.scale(this.maxWidth);
            int iInLine = 0;
            for (String line : textLines) {
                for (String word : line.split(" ")) {
                    double wordWidth = theme.textWidth(word, word.length(), title);
                    double toAdd = wordWidth;
                    if (iInLine > 0) toAdd += spaceWidth;
                    if (lineWidth + toAdd > maxWidth) {
                        lines.add(sb.toString());
                        sb.setLength(0);
                        sb.append(word);
                        lineWidth = wordWidth;
                        iInLine = 1;
                    } else {
                        if (iInLine > 0) {
                            sb.append(' ');
                            lineWidth += spaceWidth;
                        }
                        sb.append(word);
                        lineWidth += wordWidth;
                        iInLine++;
                    }
                    maxLineWidth = Math.max(maxLineWidth, lineWidth);
                }
                lines.add(sb.toString());
                sb.setLength(0);
                lineWidth = 0;
                iInLine = 0;
            }
            if (!sb.isEmpty()) lines.add(sb.toString());
        }
        width = maxLineWidth;
        height = theme.textHeight(title) * lines.size();
    }
    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        double h = theme.textHeight(title);
        Color defaultColor = theme().textColor.get();
        for (int i = 0; i < lines.size(); i++) {
            renderer.text(lines.get(i), x, y + h * i, color != null ? color : defaultColor, false);
        }
    }
}