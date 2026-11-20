package ru.sortviz.model;

public record SortRequest(int[] data, Algorithm algorithm, Order order, Pivot pivot) {
    public enum Algorithm { BUBBLE, QUICK, MERGE }
    public enum Order { ASC, DESC }
    public enum Pivot { FIRST, MIDDLE, LAST, RANDOM }
}
