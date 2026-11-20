package ru.sortviz.algorithm;

public final class BubbleSort {
    private BubbleSort() {}

    public static void sort(SortTrace trace) {
        for (int end = trace.size() - 1; end > 0; end--) {
            boolean swapped = false;
            for (int first = 0; first < end; first++) {
                int second = first + 1;
                if (!trace.inOrder(first, second)) {
                    trace.swap(first, second);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }
}
