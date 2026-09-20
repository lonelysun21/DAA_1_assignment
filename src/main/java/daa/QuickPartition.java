package daa;

import java.util.concurrent.ThreadLocalRandom;

public final class QuickPartition {
    private QuickPartition() {
    }

    public static int[] partition(int[] a, int left, int right, Metrics metrics) {
        int pivotIndex = ThreadLocalRandom.current().nextInt(left, right + 1);
        int pivot = a[pivotIndex];
        int lt = left;
        int i = left;
        int gt = right;

        while (i <= gt) {
            metrics.addComparison();
            if (a[i] < pivot) {
                swap(a, lt++, i++);
            } else {
                metrics.addComparison();
                if (a[i] > pivot) {
                    swap(a, i, gt--);
                } else {
                    i++;
                }
            }
        }
        return new int[]{lt, gt};
    }

    private static void swap(int[] a, int i, int j) {
        if (i != j) {
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
    }
}
