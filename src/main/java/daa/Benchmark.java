package daa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class Benchmark {
    private static final int[] SIZES = {1_000, 10_000, 100_000, 1_000_000};
    private static final String[] INPUTS = {"random", "sorted", "duplicates"};
    private static final int RUNS = 5;

    private Benchmark() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        Path output = Path.of("results.csv");
        List<Result> results = new ArrayList<>();

        System.out.println("Running benchmark: 3 algorithms x 3 inputs x 4 sizes x 5 runs");
        warmUp();

        for (String inputType : INPUTS) {
            for (int n : SIZES) {
                int[] base = createInput(inputType, n);
                for (String algorithm : new String[]{"MergeSort", "QuickSort", "QuickSelect"}) {
                    Result result = benchmarkCase(algorithm, base, inputType);
                    results.add(result);
                    System.out.printf(Locale.US,
                            "%s %-10s n=%-8d time=%8.3f ms comparisons=%d depth=%d%n",
                            algorithm, inputType, n, result.timeMs, result.comparisons, result.maxDepth);
                }
            }
        }

        writeCsv(results, output);
        PlotGenerator.generateAll(results, Path.of("plots"));
        System.out.println("Saved " + output.toAbsolutePath());
        System.out.println("Saved plots to " + Path.of("plots").toAbsolutePath());
    }

    private static Result benchmarkCase(String algorithm, int[] base, String inputType) {
        double[] times = new double[RUNS];
        long[] comparisons = new long[RUNS];
        int[] depths = new int[RUNS];

        for (int run = 0; run < RUNS; run++) {
            int[] a = base.clone();
            Metrics metrics = new Metrics();
            metrics.startTimer();
            if (algorithm.equals("MergeSort")) {
                MergeSort.sort(a, metrics);
            } else if (algorithm.equals("QuickSort")) {
                QuickSort.sort(a, metrics);
            } else {
                QuickSelect.select(a, a.length / 2, metrics);
            }
            metrics.stopTimer();
            times[run] = metrics.getTimeMillis();
            comparisons[run] = metrics.getComparisons();
            depths[run] = metrics.getMaxDepth();
        }

        return new Result(algorithm, inputType, base.length,
                median(times), median(comparisons), median(depths));
    }

    private static int[] createInput(String type, int n) {
        long seed = 20260920L + n * 31L + type.hashCode();
        return switch (type) {
            case "random" -> InputGenerator.random(n, seed);
            case "sorted" -> InputGenerator.sorted(n, seed);
            case "duplicates" -> InputGenerator.duplicates(n, seed);
            default -> throw new IllegalArgumentException("Unknown input type: " + type);
        };
    }

    private static void warmUp() {
        for (int i = 0; i < 5; i++) {
            int[] a = InputGenerator.random(20_000, 1234 + i);
            Metrics m1 = new Metrics();
            MergeSort.sort(a.clone(), m1);
            Metrics m2 = new Metrics();
            QuickSort.sort(a.clone(), m2);
            Metrics m3 = new Metrics();
            QuickSelect.select(a.clone(), a.length / 2, m3);
        }
    }

    private static double median(double[] values) {
        double[] copy = values.clone();
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    private static long median(long[] values) {
        long[] copy = values.clone();
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    private static int median(int[] values) {
        int[] copy = values.clone();
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    private static void writeCsv(List<Result> results, Path output) throws IOException {
        StringBuilder sb = new StringBuilder("algorithm,input,n,time_ms,comparisons,max_depth\n");
        for (Result r : results) {
            sb.append(r.algorithm).append(',')
                    .append(r.input).append(',')
                    .append(r.n).append(',')
                    .append(String.format(Locale.US, "%.6f", r.timeMs)).append(',')
                    .append(r.comparisons).append(',')
                    .append(r.maxDepth).append('\n');
        }
        Files.writeString(output, sb.toString());
    }

    public record Result(String algorithm, String input, int n,
                         double timeMs, long comparisons, int maxDepth) {
    }
}
