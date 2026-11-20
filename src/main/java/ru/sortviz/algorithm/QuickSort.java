package ru.sortviz.algorithm;

import java.util.concurrent.ThreadLocalRandom;
import ru.sortviz.model.SortRequest.Pivot;

public final class QuickSort {
    private QuickSort() {}

    public static void sort(SortTrace trace, Pivot pivot) {
        sortRange(trace, pivot, 0, trace.size() - 1);
    }

    private static void sortRange(SortTrace trace, Pivot pivot, int low, int high) {
        if (low >= high) {
            return;
        }
        int pivotIndex = switch (pivot) {
            case FIRST -> low;
            case MIDDLE -> low + (high - low) / 2;
            case LAST -> high;
            case RANDOM -> ThreadLocalRandom.current().nextInt(low, high + 1);
        };
        trace.swap(pivotIndex, high);
        int boundary = low;
        for (int index = low; index < high; index++) {
            if (trace.inOrder(index, high)) {
                trace.swap(index, boundary);
                boundary++;
            }
        }
        trace.swap(boundary, high);
        sortRange(trace, pivot, low, boundary - 1);
        sortRange(trace, pivot, boundary + 1, high);
    }
}
