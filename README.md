# DAA Assignment 1 — Divide and Conquer & Asymptotic Notations

Java implementation of MergeSort, QuickSort and QuickSelect with a `Metrics` class, JUnit 5 tests,
a benchmark that exports `results.csv`, and PNG plots.

## Requirements

- Java 17+
- Maven 3.9+

## Project structure

```text
.
├── pom.xml
├── README.md
├── REPORT.md
├── results.csv
├── plots/
│   ├── time_vs_n.png
│   ├── depth_vs_n.png
│   └── ratio_vs_n.png
└── src/
    ├── main/java/daa/
    │   ├── Metrics.java          comparisons, max recursion depth, time (System.nanoTime)
    │   ├── MergeSort.java        one reusable buffer, insertion-sort cutoff 15, linear merge
    │   ├── QuickPartition.java   random pivot + 3-way partition (shared by QuickSort and QuickSelect)
    │   ├── QuickSort.java        smaller side first, larger side in a loop
    │   ├── QuickSelect.java      k-th smallest element, k starts from 0
    │   ├── InputGenerator.java   random / sorted / duplicates inputs
    │   ├── Benchmark.java        runs all cases, writes results.csv, calls PlotGenerator
    │   └── PlotGenerator.java    draws the PNG plots with Java2D (no extra libraries)
    └── test/java/daa/
        └── AlgorithmTest.java
```

## Build and run the tests

```bash
mvn clean test
```

## Run the benchmark

```bash
mvn -q -DskipTests package
java -cp target/classes daa.Benchmark
```

The benchmark runs MergeSort, QuickSort and QuickSelect on n = 1 000; 10 000; 100 000; 1 000 000 and on
`random`, `sorted` and `duplicates` (values 0..9) inputs. Every case is repeated 5 times and the median
is saved.

It **overwrites** the following files (run it only if you want to regenerate them):

- `results.csv` — columns `algorithm,input,n,time_ms,comparisons,max_depth`
- `plots/time_vs_n.png`
- `plots/depth_vs_n.png`
- `plots/ratio_vs_n.png`

## Notes

- One "comparison" is one `<`, `>` or `<=` test between two array elements. In the 3-way partition an
  element that is not smaller than the pivot costs two comparisons.
- QuickSort and QuickSelect choose the pivot with `ThreadLocalRandom`, so their comparison counts differ
  slightly from run to run. The numbers in `REPORT.md` correspond to the committed `results.csv`.
  MergeSort is deterministic. Times depend on the machine.
- `QuickSelect` rearranges the input array in place.
