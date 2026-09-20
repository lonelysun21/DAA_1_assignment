package daa;

public final class MergeSort {
    private static final int CUTOFF = 15;

    private MergeSort() {
    }

    public static void sort(int[] a, Metrics metrics) {
        if (a == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        if (a.length < 2) {
            return;
        }
        int[] buffer = new int[a.length];
        sort(a, buffer, 0, a.length - 1, 1, metrics);
    }

    private static void sort(int[] a, int[] buffer, int left, int right,
                             int depth, Metrics metrics) {
        metrics.updateDepth(depth);
        int size = right - left + 1;
        if (size <= CUTOFF) {
            insertionSort(a, left, right, metrics);
            return;
        }

        int mid = left + (right - left) / 2;
        sort(a, buffer, left, mid, depth + 1, metrics);
        sort(a, buffer, mid + 1, right, depth + 1, metrics);
        merge(a, buffer, left, mid, right, metrics);
    }

    private static void insertionSort(int[] a, int left, int right, Metrics metrics) {
        for (int i = left + 1; i <= right; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= left) {
                metrics.addComparison();
                if (a[j] <= key) {
                    break;
                }
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static void merge(int[] a, int[] buffer, int left, int mid, int right,
                              Metrics metrics) {
        int i = left;
        int j = mid + 1;
        int k = left;

        while (i <= mid && j <= right) {
            metrics.addComparison();
            if (a[i] <= a[j]) {
                buffer[k++] = a[i++];
            } else {
                buffer[k++] = a[j++];
            }
        }
        while (i <= mid) {
            buffer[k++] = a[i++];
        }
        while (j <= right) {
            buffer[k++] = a[j++];
        }
        for (int p = left; p <= right; p++) {
            a[p] = buffer[p];
        }
    }
}
