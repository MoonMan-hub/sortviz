"use strict";

const byId = id => document.getElementById(id);
const data = byId("data");
const algorithm = byId("algorithm");
const order = byId("order");
const pivot = byId("pivot");
const plot = byId("plot");
const status = byId("status");
const progress = byId("progress");
const error = byId("error");
const play = byId("play");
const step = byId("step");
const speed = byId("speed");
const file = byId("file");
const sortedResult = byId("sorted-result");
const comparisons = byId("comparisons");
const swaps = byId("swaps");
const writes = byId("writes");
const duration = byId("duration");

const notes = {
    BUBBLE: "Сравнивает соседние элементы и меняет местами пары, которые стоят в неверном порядке.",
    QUICK: "Выбирает опорный элемент и делит массив на части, которые сортируются отдельно.",
    MERGE: "Делит массив на половины и соединяет отсортированные части, записывая значения по порядку."
};

let values = [];
let bars = [];
let minimum = 0;
let scale = 1;
let result = null;
let cursor = 0;
let timer = null;
let pending = null;

function parseArray(text) {
    const tokens = text.trim().split(/[\s,;]+/);
    if (tokens.length < 2 || tokens.length > 100) {
        throw new Error("Введите от 2 до 100 целых чисел.");
    }
    return tokens.map(token => {
        const number = Number(token);
        if (!/^[+-]?\d+$/.test(token) || number < -2147483648 || number > 2147483647) {
            throw new Error("Каждое значение должно быть целым числом от −2 147 483 648 до 2 147 483 647.");
        }
        return number;
    });
}

function setError(message = "") {
    error.hidden = !message;
    error.textContent = message;
}

function drawFrame(event, complete = false) {
    const tone = event?.operation === "COMPARE" ? "compare" : "change";
    for (const [index, bar] of bars.entries()) {
        const value = values[index];
        const highlighted = event != null && (index === event.first ||
            (event.operation !== "WRITE" && index === event.second));
        bar.classList.toggle("compare", !complete && highlighted && tone === "compare");
        bar.classList.toggle("change", !complete && highlighted && tone === "change");
        bar.classList.toggle("sorted", complete);
        bar.classList.toggle("negative", value < 0);
        bar.style.setProperty("--bottom", `${100 * (Math.min(0, value) - minimum) / scale}%`);
        bar.style.setProperty("--height", `${100 * Math.abs(value) / scale}%`);
        bar.title = `Элемент ${index + 1}: ${value}`;
        if (bar.firstChild) {
            bar.firstChild.textContent = value;
        }
    }
}

function drawArray(input) {
    values = input;
    minimum = Math.min(0, ...values);
    scale = Math.max(0, ...values) - minimum || 1;
    plot.style.setProperty("--zero", `${-100 * minimum / scale}%`);
    plot.classList.toggle("dense", values.length > 20);
    const showLabels = values.length <= 18 && Math.max(...values.map(number => String(number).length)) <= 4;
    const slots = values.map(() => {
        const slot = document.createElement("div");
        slot.className = "bar-slot";
        const bar = document.createElement("div");
        bar.className = "bar";
        if (showLabels) {
            bar.append(document.createElement("span"));
        }
        slot.append(bar);
        return slot;
    });
    plot.replaceChildren(...slots);
    bars = slots.map(slot => slot.firstChild);
    drawFrame();
}

function updateButtons() {
    const complete = result && cursor === result.steps.length;
    play.disabled = pending !== null;
    play.textContent = pending ? "Загрузка…" : timer !== null ? "Пауза" :
        complete ? "Запустить снова" : cursor ? "Продолжить" : "Запустить";
    step.disabled = Boolean(pending || complete);
}

function stopPlayback() {
    clearInterval(timer);
    timer = null;
    updateButtons();
}

function reset() {
    pending?.abort();
    pending = null;
    result = null;
    cursor = 0;
    stopPlayback();
    progress.textContent = "";
    comparisons.textContent = swaps.textContent = writes.textContent = "0";
    duration.textContent = "—";
    sortedResult.hidden = true;
    plot.setAttribute("aria-label", "Столбчатая диаграмма исходного массива");
    setError();
    try {
        drawArray(parseArray(data.value));
        status.textContent = "Готово к запуску";
    } catch {
        drawArray([]);
        status.textContent = "Введите исходные данные";
    }
}

function updateAlgorithm() {
    byId("algorithm-note").textContent = notes[algorithm.value];
    byId("pivot-field").hidden = algorithm.value !== "QUICK";
    reset();
}

async function prepareRun() {
    if (result && cursor < result.steps.length) {
        return true;
    }
    reset();
    let input;
    try {
        input = parseArray(data.value);
    } catch (problem) {
        setError(problem.message);
        return false;
    }
    const request = new AbortController();
    pending = request;
    status.textContent = "Подготовка шагов";
    updateButtons();
    try {
        const response = await fetch("/api/sort", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ data: input, algorithm: algorithm.value, order: order.value, pivot: pivot.value }),
            signal: request.signal
        });
        const payload = await response.json();
        if (pending !== request) {
            return false;
        }
        if (!response.ok) {
            setError(payload.error || "Не удалось выполнить сортировку.");
            status.textContent = "Ошибка сортировки";
            return false;
        }
        result = payload;
        return true;
    } catch {
        if (pending === request) {
            setError("Не удалось получить шаги. Проверьте, что сервер SortViz запущен.");
            status.textContent = "Ошибка соединения";
        }
        return false;
    } finally {
        if (pending === request) {
            pending = null;
            updateButtons();
        }
    }
}

function advance() {
    const event = result.steps[cursor++];
    const { operation, first, second } = event;
    switch (operation) {
        case "COMPARE":
            comparisons.textContent = Number(comparisons.textContent) + 1;
            break;
        case "SWAP":
            [values[first], values[second]] = [values[second], values[first]];
            swaps.textContent = Number(swaps.textContent) + 1;
            break;
        case "WRITE":
            values[first] = second;
            writes.textContent = Number(writes.textContent) + 1;
            break;
    }
    const complete = cursor === result.steps.length;
    drawFrame(event, complete);
    progress.textContent = `Шаг ${cursor} из ${result.steps.length}`;
    if (complete) {
        stopPlayback();
        status.textContent = "Сортировка завершена";
        duration.textContent = result.metrics.milliseconds.toLocaleString("ru-RU", { maximumFractionDigits: 3 });
        plot.setAttribute("aria-label", "Столбчатая диаграмма отсортированного массива");
        sortedResult.textContent = `Результат: ${values.join(", ")}`;
        sortedResult.hidden = false;
    }
}

function startPlayback() {
    clearInterval(timer);
    timer = setInterval(advance, 1000 / Number(speed.value));
    status.textContent = "Воспроизведение";
    updateButtons();
}

play.addEventListener("click", async () => {
    if (timer !== null) {
        stopPlayback();
        status.textContent = "Пауза";
    } else if (await prepareRun()) {
        startPlayback();
    }
});

step.addEventListener("click", async () => {
    stopPlayback();
    if (await prepareRun()) {
        status.textContent = "Пауза";
        advance();
        updateButtons();
    }
});

speed.addEventListener("input", () => {
    byId("speed-display").textContent = `${speed.value} шаг/с`;
    if (timer !== null) {
        startPlayback();
    }
});

data.addEventListener("input", reset);
algorithm.addEventListener("change", updateAlgorithm);
order.addEventListener("change", reset);
pivot.addEventListener("change", reset);
byId("reset").addEventListener("click", reset);

byId("random").addEventListener("click", () => {
    data.value = Array.from({ length: 12 }, () => 10 + Math.floor(Math.random() * 90)).join(", ");
    reset();
});

byId("open-file").addEventListener("click", () => file.click());
file.addEventListener("change", async () => {
    const chosen = file.files[0];
    file.value = "";
    if (!chosen) {
        return;
    }
    try {
        if (chosen.size > 16384) {
            throw new Error("Файл слишком большой. Выберите .txt или .csv размером до 16 КБ.");
        }
        const contents = await chosen.text();
        parseArray(contents);
        data.value = contents.trim();
        reset();
    } catch (problem) {
        setError(problem.message);
    }
});

updateAlgorithm();
