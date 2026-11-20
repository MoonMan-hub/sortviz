package ru.sortviz.model;

import java.util.List;

public record SortResult(int[] sorted, List<Step> steps, Metrics metrics) {
    public enum Operation { COMPARE, SWAP, WRITE }

    public record Step(Operation operation, int first, int second) {}

    public record Metrics(int comparisons, int swaps, int writes, int steps, double milliseconds) {}
}
