package daa;

public class Metrics {
    private long comparisons;
    private int maxDepth;
    private long startTime;
    private long timeNanos;

    public void reset() {
        comparisons = 0;
        maxDepth = 0;
        startTime = 0;
        timeNanos = 0;
    }

    public void startTimer() {
        startTime = System.nanoTime();
    }

    public void stopTimer() {
        timeNanos = System.nanoTime() - startTime;
    }

    public void addComparison() {
        comparisons++;
    }

    public void updateDepth(int depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    public long getComparisons() {
        return comparisons;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public long getTimeNanos() {
        return timeNanos;
    }

    public double getTimeMillis() {
        return timeNanos / 1_000_000.0;
    }
}
