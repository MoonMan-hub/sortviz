package ru.sortviz.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.sortviz.model.SortRequest;
import ru.sortviz.model.SortResult;
import ru.sortviz.service.SortService;

@RestController
public class SortController {
    private final SortService service;

    public SortController(SortService service) {
        this.service = service;
    }

    @PostMapping("/api/sort")
    public SortResult sort(@RequestBody SortRequest request) {
        return service.sort(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidArguments(IllegalArgumentException error) {
        return Map.of("error", error.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidJson() {
        return Map.of("error", "Проверьте, что массив содержит целые числа, а настройки заданы верно.");
    }
}
