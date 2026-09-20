package daa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;


public final class PlotGenerator {
    private static final String[] ALGORITHMS = {"MergeSort", "QuickSort", "QuickSelect"};
    private static final String[] INPUTS = {"random", "sorted", "duplicates"};
    private static final Color[] COLORS = {
            new Color(31, 119, 180), new Color(255, 127, 14), new Color(44, 160, 44)};

    private static final int PANEL_W = 560;
    private static final int PANEL_H = 430;
    private static final int TITLE_H = 46;

    private record Series(String name, double[] x, double[] y, int style) {
    }

    private record Panel(String title, String yLabel, boolean logY, List<Series> series) {
    }

    private interface Value {
        double of(Benchmark.Result r);
    }

    private PlotGenerator() {
    }

    public static void generateAll(List<Benchmark.Result> results, Path dir) throws IOException {
        System.setProperty("java.awt.headless", "true");
        Files.createDirectories(dir);

        // Time: compare algorithms on every input type
        List<Panel> time = new ArrayList<>();
        for (String input : INPUTS) {
            time.add(new Panel("Time vs n - " + input + " input", "time, ms (median of 5)", true,
                    byAlgorithm(results, input, Benchmark.Result::timeMs)));
        }
        save(render("Running time", time), dir.resolve("time_vs_n.png"));

        // Depth: compare algorithms on every input type
        List<Panel> depth = new ArrayList<>();
        for (String input : INPUTS) {
            depth.add(new Panel("Max recursion depth vs n - " + input + " input", "max recursion depth", false,
                    byAlgorithm(results, input, Benchmark.Result::maxDepth)));
        }
        save(render("Maximum recursion depth", depth), dir.resolve("depth_vs_n.png"));

        // Ratio: every algorithm has its own normalisation, so one panel per algorithm
        List<Panel> ratio = new ArrayList<>();
        for (String algorithm : ALGORITHMS) {
            boolean select = algorithm.equals("QuickSelect");
            ratio.add(new Panel(
                    algorithm + (select ? ": comparisons / n" : ": comparisons / (n*log2 n)"),
                    select ? "comparisons / n" : "comparisons / (n*log2 n)", false,
                    byInput(results, algorithm, PlotGenerator::ratio)));
        }
        save(render("Ratio of measured cost to expected growth", ratio), dir.resolve("ratio_vs_n.png"));
    }

    // ==== data ===============================================================

    static double ratio(Benchmark.Result r) {
        double n = r.n();
        double expected = r.algorithm().equals("QuickSelect") ? n : n * (Math.log(n) / Math.log(2));
        return r.comparisons() / expected;
    }

    private static List<Series> byAlgorithm(List<Benchmark.Result> all, String input, Value value) {
        List<Series> list = new ArrayList<>();
        for (int i = 0; i < ALGORITHMS.length; i++) {
            list.add(series(all, ALGORITHMS[i], input, ALGORITHMS[i], i, value));
        }
        return list;
    }

    private static List<Series> byInput(List<Benchmark.Result> all, String algorithm, Value value) {
        List<Series> list = new ArrayList<>();
        for (int i = 0; i < INPUTS.length; i++) {
            list.add(series(all, algorithm, INPUTS[i], INPUTS[i], i, value));
        }
        return list;
    }

    private static Series series(List<Benchmark.Result> all, String algorithm, String input,
                                 String name, int style, Value value) {
        List<Benchmark.Result> sel = all.stream()
                .filter(r -> r.algorithm().equals(algorithm) && r.input().equals(input))
                .sorted(Comparator.comparingInt(Benchmark.Result::n))
                .toList();
        double[] x = new double[sel.size()];
        double[] y = new double[sel.size()];
        for (int i = 0; i < sel.size(); i++) {
            x[i] = sel.get(i).n();
            y[i] = value.of(sel.get(i));
        }
        return new Series(name, x, y, style);
    }

    // ==== drawing ============================================================

    private static void save(BufferedImage image, Path path) throws IOException {
        ImageIO.write(image, "png", path.toFile());
    }

    private static BufferedImage render(String mainTitle, List<Panel> panels) {
        int cols = panels.size();
        BufferedImage img = new BufferedImage(cols * PANEL_W, TITLE_H + PANEL_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        centered(g, mainTitle, img.getWidth() / 2, 31);
        for (int i = 0; i < cols; i++) {
            drawPanel(g, panels.get(i), i * PANEL_W, TITLE_H);
        }
        g.dispose();
        return img;
    }

    private static void drawPanel(Graphics2D g, Panel p, int px, int py) {
        int left = 80;
        int right = 24;
        int top = 34;
        int bottom = 92;
        int x0 = px + left;
        int y0 = py + top;
        int w = PANEL_W - left - right;
        int h = PANEL_H - top - bottom;

        // x axis: log10(n) between 10^3 and 10^6, with a little padding so end markers are not clipped
        double lxMin = 2.85;
        double lxMax = 6.15;

        // y axis range
        double yMin = Double.MAX_VALUE;
        double yMax = 0;
        for (Series s : p.series()) {
            for (double v : s.y()) {
                yMin = Math.min(yMin, Math.max(v, 1e-9));
                yMax = Math.max(yMax, v);
            }
        }
        double lyMin;
        double lyMax;
        List<Double> yTicks = new ArrayList<>();
        if (p.logY()) {
            lyMin = Math.floor(Math.log10(yMin));
            lyMax = Math.ceil(Math.log10(yMax));
            if (lyMax == lyMin) {
                lyMax++;
            }
            for (double e = lyMin; e <= lyMax + 1e-9; e++) {
                yTicks.add(e);
            }
        } else {
            double step = niceStep(yMax / 5.0);
            lyMin = 0;
            lyMax = Math.ceil(yMax / step) * step;
            for (double v = 0; v <= lyMax + 1e-9; v += step) {
                yTicks.add(v);
            }
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        centered(g, p.title(), x0 + w / 2, py + 20);

        // grid and tick labels
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.setStroke(new BasicStroke(1f));
        for (double t : yTicks) {
            double yy = y0 + h - (t - lyMin) / (lyMax - lyMin) * h;
            g.setColor(new Color(225, 225, 225));
            g.draw(new Line2D.Double(x0, yy, x0 + w, yy));
            g.setColor(Color.DARK_GRAY);
            String label = p.logY() ? logLabel(t) : trim(t);
            g.drawString(label, x0 - 8 - g.getFontMetrics().stringWidth(label), (float) (yy + 4));
        }
        String[] xLabels = {"1K", "10K", "100K", "1M"};
        for (int e = 3; e <= 6; e++) {
            double xx = x0 + (e - lxMin) / (lxMax - lxMin) * w;
            g.setColor(new Color(225, 225, 225));
            g.draw(new Line2D.Double(xx, y0, xx, y0 + h));
            g.setColor(Color.DARK_GRAY);
            centered(g, xLabels[e - 3], (int) xx, y0 + h + 18);
        }

        g.setColor(Color.BLACK);
        g.drawRect(x0, y0, w, h);

        // axis labels (y label is placed left of the tick labels, so they never overlap)
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        centered(g, "n (array size)", x0 + w / 2, y0 + h + 40);
        AffineTransform old = g.getTransform();
        g.rotate(-Math.PI / 2, px + 16, y0 + h / 2.0);
        centered(g, p.yLabel(), px + 16, y0 + h / 2 + 4);
        g.setTransform(old);

        // lines, then markers on top
        for (Series s : p.series()) {
            g.setColor(COLORS[s.style() % COLORS.length]);
            g.setStroke(new BasicStroke(2.2f));
            double prevX = 0;
            double prevY = 0;
            for (int i = 0; i < s.x().length; i++) {
                double xx = mapX(s.x()[i], x0, w, lxMin, lxMax);
                double yy = mapY(s.y()[i], p.logY(), y0, h, lyMin, lyMax);
                if (i > 0) {
                    g.draw(new Line2D.Double(prevX, prevY, xx, yy));
                }
                prevX = xx;
                prevY = yy;
            }
            for (int i = 0; i < s.x().length; i++) {
                marker(g, s.style(), mapX(s.x()[i], x0, w, lxMin, lxMax),
                        mapY(s.y()[i], p.logY(), y0, h, lyMin, lyMax));
            }
        }

        // legend below the x label
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        int total = 0;
        for (Series s : p.series()) {
            total += 22 + fm.stringWidth(s.name()) + 18;
        }
        int lx = x0 + Math.max(0, (w - total) / 2);
        int ly = py + PANEL_H - 12;
        for (Series s : p.series()) {
            g.setColor(COLORS[s.style() % COLORS.length]);
            g.setStroke(new BasicStroke(2.2f));
            g.draw(new Line2D.Double(lx, ly - 4, lx + 16, ly - 4));
            marker(g, s.style(), lx + 8, ly - 4);
            g.setColor(Color.BLACK);
            g.drawString(s.name(), lx + 22, ly);
            lx += 22 + fm.stringWidth(s.name()) + 18;
        }
    }

    private static double mapX(double n, int x0, int w, double lxMin, double lxMax) {
        return x0 + (Math.log10(n) - lxMin) / (lxMax - lxMin) * w;
    }

    private static double mapY(double v, boolean log, int y0, int h, double lyMin, double lyMax) {
        double val = log ? Math.log10(Math.max(v, 1e-9)) : v;
        return y0 + h - (val - lyMin) / (lyMax - lyMin) * h;
    }

    private static void marker(Graphics2D g, int style, double x, double y) {
        double r = 4.5;
        switch (style % 3) {
            case 0 -> g.fill(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
            case 1 -> g.fill(new Rectangle2D.Double(x - r, y - r, 2 * r, 2 * r));
            default -> {
                Polygon t = new Polygon();
                t.addPoint((int) x, (int) (y - r - 1));
                t.addPoint((int) (x - r - 1), (int) (y + r));
                t.addPoint((int) (x + r + 1), (int) (y + r));
                g.fill(t);
            }
        }
    }

    private static void centered(Graphics2D g, String text, int cx, int baseline) {
        g.drawString(text, cx - g.getFontMetrics().stringWidth(text) / 2, baseline);
    }

    private static double niceStep(double raw) {
        double exp = Math.pow(10, Math.floor(Math.log10(raw)));
        double f = raw / exp;
        double nice = f <= 1 ? 1 : f <= 2 ? 2 : f <= 2.5 ? 2.5 : f <= 5 ? 5 : 10;
        return nice * exp;
    }

    private static String logLabel(double exponent) {
        return trim(Math.pow(10, exponent));
    }

    private static String trim(double v) {
        String s = String.format(Locale.ROOT, "%.3f", v);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
