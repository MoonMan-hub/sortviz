package ru.sortviz.service;

import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.sortviz.model.SortRequest;
import ru.sortviz.model.SortRequest.Algorithm;
import ru.sortviz.model.SortRequest.Order;
import ru.sortviz.model.SortRequest.Pivot;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortServiceTest {
    private final SortService service = new SortService();

    @ParameterizedTest
    @EnumSource(Algorithm.class)
    void sortsInBothOrdersAndRecordsReplayableSteps(Algorithm algorithm) {
        int[][] inputs = {
            {Integer.MAX_VALUE, -4, 0, 9, -4, Integer.MIN_VALUE, 9},
            {2, 1},
            {3, 3, 3},
            new Random(42).ints(100, -50, 51).toArray()
        };
        for (int[] data : inputs) {
            for (Order order : Order.values()) {
                checkRun(data, algorithm, order, Pivot.MIDDLE);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Pivot.class)
    void quickSortSupportsAllPivots(Pivot pivot) {
        checkRun(new int[] {10, 2, 10, -7, 4}, Algorithm.QUICK, Order.ASC, pivot);
    }

    @Test
    void rejectsArrayLengthsOutsideTheAllowedRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service.sort(new SortRequest(new int[1], Algorithm.BUBBLE, Order.ASC, null)));
        assertThrows(IllegalArgumentException.class,
                () -> service.sort(new SortRequest(new int[101], Algorithm.BUBBLE, Order.ASC, null)));
    }

    private void checkRun(int[] input, Algorithm algorithm, Order order, Pivot pivot) {
        int[] data = input.clone();
        int[] expected = Arrays.stream(input).boxed()
                .sorted(order == Order.ASC ? Integer::compare : (a, b) -> Integer.compare(b, a))
                .mapToInt(Integer::intValue)
                .toArray();
        var result = service.sort(new SortRequest(data, algorithm, order, pivot));

        int comparisons = 0;
        int swaps = 0;
        int writes = 0;
        int[] replay = input.clone();
        for (var step : result.steps()) {
            int first = step.first();
            int second = step.second();
            switch (step.operation()) {
                case COMPARE -> comparisons++;
                case SWAP -> {
                    int value = replay[first];
                    replay[first] = replay[second];
                    replay[second] = value;
                    swaps++;
                }
                case WRITE -> {
                    replay[first] = second;
                    writes++;
                }
            }
        }
        assertArrayEquals(input, data);
        assertArrayEquals(expected, result.sorted());
        assertArrayEquals(expected, replay);
        assertEquals(comparisons, result.metrics().comparisons());
        assertEquals(swaps, result.metrics().swaps());
        assertEquals(writes, result.metrics().writes());
        assertEquals(result.steps().size(), result.metrics().steps());
    }
}
