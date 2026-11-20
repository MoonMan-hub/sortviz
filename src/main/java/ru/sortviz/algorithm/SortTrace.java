package ru.sortviz.algorithm;

import java.util.ArrayList;
import java.util.List;
import ru.sortviz.model.SortRequest.Order;
import ru.sortviz.model.SortResult;
import ru.sortviz.model.SortResult.Metrics;
import ru.sortviz.model.SortResult.Operation;
import ru.sortviz.model.SortResult.Step;

public final class SortTrace {
    private final int[] data;
    private final Order order;
    private final List<Step> steps = new ArrayList<>();
    private int comparisons;
    private int swaps;
    private int writes;

    public SortTrace(int[] data, Order order) {
        this.data = data.clone();
        this.order = order;
    }

    public int size() {
        return data.length;
    }

    int valueAt(int index) {
        return data[index];
    }

    boolean inOrder(int first, int second) {
        comparisons++;
        steps.add(new Step(Operation.COMPARE, first, second));
        return order == Order.ASC ? data[first] <= data[second] : data[first] >= data[second];
    }

    void swap(int first, int second) {
        if (first == second) {
            return;
        }
        int value = data[first];
        data[first] = data[second];
        data[second] = value;
        swaps++;
        steps.add(new Step(Operation.SWAP, first, second));
    }

    void write(int index, int value) {
        data[index] = value;
        writes++;
        steps.add(new Step(Operation.WRITE, index, value));
    }

    public SortResult result(long elapsedNanos) {
        var metrics = new Metrics(comparisons, swaps, writes, steps.size(), elapsedNanos / 1_000_000.0);
        return new SortResult(data.clone(), List.copyOf(steps), metrics);
    }
}
