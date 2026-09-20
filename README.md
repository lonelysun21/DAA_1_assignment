# DAA Assignment 1 — Divide and Conquer & Asymptotic Notations

Java implementation for MergeSort, QuickSort and QuickSelect with metrics, JUnit 5 tests, benchmark, CSV export and PNG plots.

## Requirements

- Java 17+
- Maven 3.9+

## Project structure

```text
DAA_Assignment1/
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
    │   ├── Metrics.java
    │   ├── MergeSort.java
    │   ├── QuickPartition.java
    │   ├── QuickSort.java
    │   ├── QuickSelect.java
    │   ├── InputGenerator.java
    │   ├── Benchmark.java
    │   └── PlotGenerator.java
    └── test/java/daa/
        └── AlgorithmTest.java
```

## Build and tests

```bash
mvn clean test
```

## Benchmark

Run:

```bash
mvn -q -DskipTests package
java -cp target/classes daa.Benchmark
```

The benchmark runs all three algorithms on n = 1,000; 10,000; 100,000; 1,000,000 and on random, sorted and duplicate-heavy inputs. Each case is repeated five times and the median is written to `results.csv`.

The benchmark also creates:

- `plots/time_vs_n.png`
- `plots/depth_vs_n.png`
- `plots/ratio_vs_n.png`

## Git workflow

Recommended branches:

- `main`
- `feature/mergesort`
- `feature/quicksort`
- `feature/select`
- `feature/metrics`

Example commits:

```bash
git checkout -b feature/mergesort
git add .
git commit -m "feat(mergesort): add reusable buffer and insertion cutoff"

git checkout main
# merge the feature branch after checking it
```

Release tag:

```bash
git tag v1.0
git push origin main --tags
```

## Submission

Create a ZIP named:

`DAA_Assignment1_name_surname_group.zip`

Upload it to Moodle and provide the GitHub repository link. Keep the repository on branch `main` and create tag `v1.0`.
