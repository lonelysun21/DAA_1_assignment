package daa;

public final class QuickSort {
    private QuickSort() {
    }

    public static void sort(int[] a, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        if (a.length < 2) {
            return;
        }
        sort(a, 0, a.length - 1, 1, metrics);
    }

    private static void sort(int[] a, int left, int right, int depth, Metrics metrics) {
        while (left < right) {
            metrics.updateDepth(depth);
            int[] equalRange = QuickPartition.partition(a, left, right, metrics);
            int lessRight = equalRange[0] - 1;
            int greaterLeft = equalRange[1] + 1;

            int leftSize = lessRight - left + 1;
            int rightSize = right - greaterLeft + 1;

            if (leftSize < rightSize) {
                if (left < lessRight) {
                    sort(a, left, lessRight, depth + 1, metrics);
                }
                left = greaterLeft;
            } else {
                if (greaterLeft < right) {
                    sort(a, greaterLeft, right, depth + 1, metrics);
                }
                right = lessRight;
            }
        }
    }
}
