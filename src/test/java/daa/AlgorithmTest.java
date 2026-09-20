package daa;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmTest {
    @Test
    void mergeSortMatchesArraysSortOn100RandomArrays() {
        Random random = new Random(42);
        for (int t = 0; t < 100; t++) {
            int n = random.nextInt(300);
            int[] actual = new int[n];
            for (int i = 0; i < n; i++) actual[i] = random.nextInt(200) - 100;
            int[] expected = actual.clone();
            Arrays.sort(expected);

            MergeSort.sort(actual, new Metrics());
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    void quickSortMatchesArraysSortOn100RandomArrays() {
        Random random = new Random(43);
        for (int t = 0; t < 100; t++) {
            int n = random.nextInt(300);
            int[] actual = new int[n];
            for (int i = 0; i < n; i++) actual[i] = random.nextInt(200) - 100;
            int[] expected = actual.clone();
            Arrays.sort(expected);

            QuickSort.sort(actual, new Metrics());
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    void edgeCasesAreHandled() {
        int[] empty = {};
        MergeSort.sort(empty, new Metrics());
        QuickSort.sort(empty, new Metrics());

        int[] one = {7};
        MergeSort.sort(one, new Metrics());
        QuickSort.sort(one, new Metrics());
        assertArrayEquals(new int[]{7}, one);

        int[] equal = {5, 5, 5, 5, 5};
        QuickSort.sort(equal, new Metrics());
        assertArrayEquals(new int[]{5, 5, 5, 5, 5}, equal);

        int[] sorted = {1, 2, 3, 4, 5, 6};
        QuickSort.sort(sorted, new Metrics());
        assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6}, sorted);
    }

    @Test
    void quickSortDepthOnSorted100000IsBounded() {
        int n = 100_000;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        Metrics metrics = new Metrics();
        QuickSort.sort(a, metrics);

        double limit = 2.0 * (Math.log(n) / Math.log(2));
        assertTrue(metrics.getMaxDepth() <= limit,
                "depth=" + metrics.getMaxDepth() + ", limit=" + limit);
    }

    @Test
    void quickSelectMatchesSortedKOn100RandomArrays() {
        Random random = new Random(44);
        for (int t = 0; t < 100; t++) {
            int n = 1 + random.nextInt(300);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = random.nextInt(1000) - 500;
            int k = random.nextInt(n);
            int[] expected = a.clone();
            Arrays.sort(expected);

            int actual = QuickSelect.select(a, k, new Metrics());
            assertEquals(expected[k], actual);
        }
    }

    @Test
    void quickSelectRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[]{}, 0, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[]{1, 2}, -1, new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> QuickSelect.select(new int[]{1, 2}, 2, new Metrics()));
    }
}
