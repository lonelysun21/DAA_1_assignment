package daa;

import java.util.Random;

public final class InputGenerator {
    private InputGenerator() {
    }

    public static int[] random(int n, long seed) {
        Random random = new Random(seed);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = random.nextInt();
        }
        return a;
    }

    public static int[] sorted(int n, long seed) {
        int[] a = random(n, seed);
        java.util.Arrays.sort(a);
        return a;
    }

    public static int[] duplicates(int n, long seed) {
        Random random = new Random(seed);
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = random.nextInt(10);
        }
        return a;
    }
}
