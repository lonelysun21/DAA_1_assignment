package daa;

public final class QuickSelect {
    private QuickSelect() {
    }
    public static int select(int[] a, int k) {
        return select(a, k, new Metrics());
    }

    public static int select(int[] a, int k, Metrics metrics) {
        if (a == null || a.length == 0) {
            throw new IllegalArgumentException("Array must not be empty");
        }
        if (k < 0 || k >= a.length) {
            throw new IllegalArgumentException("k must be in range [0, n-1]");
        }

        int left = 0;
        int right = a.length - 1;
        int depth = 1;

        while (left <= right) {
            metrics.updateDepth(depth);
            if (left == right) {
                return a[left];
            }

            int[] equalRange = QuickPartition.partition(a, left, right, metrics);
            if (k < equalRange[0]) {
                right = equalRange[0] - 1;
            } else if (k > equalRange[1]) {
                left = equalRange[1] + 1;
            } else {
                return a[k];
            }
            depth++;
        }
        throw new IllegalStateException("QuickSelect failed to locate k");
    }
}
