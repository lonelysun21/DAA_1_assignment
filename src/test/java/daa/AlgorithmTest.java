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

    @Test
    void mergeSortEdgeCasesAreHandled() {
        int[] empty = {};
        MergeSort.sort(empty, new Metrics());
        assertArrayEquals(new int[]{}, empty);

        int[] one = {7};
        MergeSort.sort(one, new Metrics());
        assertArrayEquals(new int[]{7}, one);

        int[] equal = new int[1000];
        Arrays.fill(equal, 5);
        int[] expectedEqual = equal.clone();
        MergeSort.sort(equal, new Metrics());
        assertArrayEquals(expectedEqual, equal);

        int[] sorted = new int[1000];
        for (int i = 0; i < sorted.length; i++) sorted[i] = i;
        int[] expectedSorted = sorted.clone();
        MergeSort.sort(sorted, new Metrics());
        assertArrayEquals(expectedSorted, sorted);

        int[] reversed = new int[1000];
        for (int i = 0; i < reversed.length; i++) reversed[i] = reversed.length - i;
        int[] expectedReversed = reversed.clone();
        Arrays.sort(expectedReversed);
        MergeSort.sort(reversed, new Metrics());
        assertArrayEquals(expectedReversed, reversed);
    }

    @Test
    void mergeSortWorksAroundTheInsertionSortCutoff() {
        Random random = new Random(45);
        for (int n = 13; n <= 34; n++) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = random.nextInt(50);
            int[] expected = a.clone();
            Arrays.sort(expected);
            MergeSort.sort(a, new Metrics());
            assertArrayEquals(expected, a, "n=" + n);
        }
    }

    @Test
    void quickSortOnAllEqualLargeArrayIsLinear() {
        int n = 200_000;
        int[] a = new int[n];
        Arrays.fill(a, 3);
        Metrics metrics = new Metrics();
        QuickSort.sort(a, metrics);
        // 3-way partition: one pass over the array, at most 2 comparisons per element
        assertTrue(metrics.getComparisons() <= 3L * n,
                "comparisons=" + metrics.getComparisons());
    }

    @Test
    void quickSortDoesNotOverflowStackOnLargeSortedArray() {
        int n = 1_000_000;
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        QuickSort.sort(a, new Metrics());
        assertEquals(0, a[0]);
        assertEquals(n - 1, a[n - 1]);
    }

    @Test
    void quickSelectWithoutMetricsMatchesSortedK() {
        Random random = new Random(46);
        for (int t = 0; t < 100; t++) {
            int n = 1 + random.nextInt(300);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = random.nextInt(10);   // many duplicates
            int k = random.nextInt(n);
            int[] expected = a.clone();
            Arrays.sort(expected);
            assertEquals(expected[k], QuickSelect.select(a, k));
        }
    }

    @Test
    void quickSelectSingleElementAndSortedMinMax() {
        assertEquals(9, QuickSelect.select(new int[]{9}, 0));

        int n = 100_000;
        int[] sorted = new int[n];
        for (int i = 0; i < n; i++) sorted[i] = i;
        assertEquals(0, QuickSelect.select(sorted.clone(), 0));
        assertEquals(n - 1, QuickSelect.select(sorted.clone(), n - 1));
        assertEquals(n / 2, QuickSelect.select(sorted.clone(), n / 2));
    }
}
