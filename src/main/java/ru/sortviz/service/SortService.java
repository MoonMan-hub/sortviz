package ru.sortviz.service;

import org.springframework.stereotype.Service;
import ru.sortviz.algorithm.BubbleSort;
import ru.sortviz.algorithm.MergeSort;
import ru.sortviz.algorithm.QuickSort;
import ru.sortviz.algorithm.SortTrace;
import ru.sortviz.model.SortRequest;
import ru.sortviz.model.SortRequest.Pivot;
import ru.sortviz.model.SortResult;

@Service
public class SortService {
    public SortResult sort(SortRequest request) {
        if (request.data() == null || request.data().length < 2 || request.data().length > 100) {
            throw new IllegalArgumentException("Введите от 2 до 100 целых чисел.");
        }
        if (request.algorithm() == null || request.order() == null) {
            throw new IllegalArgumentException("Выберите алгоритм и порядок сортировки.");
        }

        var trace = new SortTrace(request.data(), request.order());
        long start = System.nanoTime();
        switch (request.algorithm()) {
            case BUBBLE -> BubbleSort.sort(trace);
            case QUICK -> QuickSort.sort(trace, request.pivot() == null ? Pivot.MIDDLE : request.pivot());
            case MERGE -> MergeSort.sort(trace);
        }
        return trace.result(System.nanoTime() - start);
    }
}
