package ru.sortviz.algorithm;

public final class MergeSort {
    private MergeSort() {}

    public static void sort(SortTrace trace) {
        int[] buffer = new int[trace.size()];
        sortRange(trace, buffer, 0, trace.size());
    }

    private static void sortRange(SortTrace trace, int[] buffer, int start, int end) {
        if (end - start < 2) {
            return;
        }
        int middle = start + (end - start) / 2;
        sortRange(trace, buffer, start, middle);
        sortRange(trace, buffer, middle, end);

        int left = start;
        int right = middle;
        int output = start;
        while (left < middle && right < end) {
            if (trace.inOrder(left, right)) {
                buffer[output++] = trace.valueAt(left++);
            } else {
                buffer[output++] = trace.valueAt(right++);
            }
        }
        while (left < middle) {
            buffer[output++] = trace.valueAt(left++);
        }
        while (right < end) {
            buffer[output++] = trace.valueAt(right++);
        }
        for (int index = start; index < end; index++) {
            trace.write(index, buffer[index]);
        }
    }
}
