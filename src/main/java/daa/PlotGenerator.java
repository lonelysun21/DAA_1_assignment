package daa;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
    private static final int W = 1100;
    private static final int H = 700;
    private static final int LEFT = 90;
    private static final int RIGHT = 35;
    private static final int TOP = 70;
    private static final int BOTTOM = 90;

    private PlotGenerator() {}

    public static void generateAll(List<Benchmark.Result> results, Path dir) throws IOException {
        Files.createDirectories(dir);
        draw(results, dir.resolve("time_vs_n.png"), Metric.TIME, "Time vs n", "Time (ms)");
        draw(results, dir.resolve("depth_vs_n.png"), Metric.DEPTH, "Max recursion depth vs n", "Max depth");
        draw(results, dir.resolve("ratio_vs_n.png"), Metric.RATIO, "Ratio vs n", "Comparisons / expected growth");
    }

    private enum Metric { TIME, DEPTH, RATIO }

    private static void draw(List<Benchmark.Result> all, Path path, Metric metric,
                             String title, String yLabel) throws IOException {
        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString(title, LEFT, 35);
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.drawString("n", W / 2, H - 25);
        g.rotate(-Math.PI / 2);
        g.drawString(yLabel, -H / 2 - 40, 25);
        g.rotate(Math.PI / 2);

        int x0 = LEFT, y0 = H - BOTTOM, x1 = W - RIGHT, y1 = TOP;
        g.drawLine(x0, y0, x1, y0);
        g.drawLine(x0, y0, x0, y1);

        List<String> seriesNames = new ArrayList<>();
        for (String algorithm : new String[]{"MergeSort", "QuickSort", "QuickSelect"}) {
            for (String input : new String[]{"random", "sorted", "duplicates"}) {
                seriesNames.add(algorithm + ":" + input);
            }
        }

        double maxX = 1_000_000;
        double maxY = 0;
        for (Benchmark.Result r : all) {
            maxY = Math.max(maxY, value(r, metric));
        }
        if (maxY == 0) maxY = 1;
        maxY *= 1.10;

        int[] xs = {1000, 10000, 100000, 1000000};
        for (int n : xs) {
            int x = x0 + (int) ((Math.log10(n) - 3) / 3.0 * (x1 - x0));
            g.setColor(new Color(225, 225, 225));
            g.drawLine(x, y0, x, y1);
            g.setColor(Color.BLACK);
            g.drawString(String.format(Locale.US, "%d", n), x - 20, y0 + 25);
        }
        for (int i = 0; i <= 5; i++) {
            double yVal = maxY * i / 5.0;
            int y = y0 - (int) (i / 5.0 * (y0 - y1));
            g.setColor(new Color(235, 235, 235));
            g.drawLine(x0, y, x1, y);
            g.setColor(Color.BLACK);
            g.drawString(String.format(Locale.US, "%.2f", yVal), 15, y + 5);
        }

        int legendY = 55;
        int legendX = LEFT + 80;
        int idx = 0;
        for (String series : seriesNames) {
            String[] parts = series.split(":");
            List<Benchmark.Result> seriesData = all.stream()
                    .filter(r -> r.algorithm().equals(parts[0]) && r.input().equals(parts[1]))
                    .sorted(Comparator.comparingInt(Benchmark.Result::n)).toList();
            if (seriesData.isEmpty()) continue;
            int r = (idx * 70) % 255;
            int b = (idx * 130 + 70) % 255;
            int c = (idx * 190 + 40) % 255;
            Color color = new Color(r, b, c);
            g.setColor(color);
            g.setStroke(new BasicStroke(2.5f));
            for (int i = 1; i < seriesData.size(); i++) {
                Benchmark.Result a = seriesData.get(i - 1);
                Benchmark.Result bRes = seriesData.get(i);
                g.drawLine(mapX(a.n(), x0, x1), mapY(value(a, metric), maxY, y0, y1),
                        mapX(bRes.n(), x0, x1), mapY(value(bRes, metric), maxY, y0, y1));
            }
            int lx = legendX + (idx % 3) * 300;
            int ly = legendY + (idx / 3) * 20;
            g.drawLine(lx, ly - 5, lx + 25, ly - 5);
            g.setColor(Color.BLACK);
            g.drawString(series, lx + 30, ly);
            idx++;
        }

        g.dispose();
        ImageIO.write(image, "png", path.toFile());
    }

    private static int mapX(int n, int x0, int x1) {
        return x0 + (int) ((Math.log10(n) - 3) / 3.0 * (x1 - x0));
    }

    private static int mapY(double value, double maxY, int y0, int y1) {
        return y0 - (int) (value / maxY * (y0 - y1));
    }

    private static double value(Benchmark.Result r, Metric metric) {
        return switch (metric) {
            case TIME -> r.timeMs();
            case DEPTH -> r.maxDepth();
            case RATIO -> {
                double expected = r.algorithm().equals("QuickSelect")
                        ? r.n()
                        : r.n() * (Math.log(r.n()) / Math.log(2));
                yield r.comparisons() / expected;
            }
        };
    }
}
