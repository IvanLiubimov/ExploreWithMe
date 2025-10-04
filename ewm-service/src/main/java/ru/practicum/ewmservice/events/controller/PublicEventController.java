package ru.practicum.ewmservice.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.events.dto.EventDtoFull;
import ru.practicum.ewmservice.events.service.EventService;
import ru.practicum.ewmservice.exception.ConditionsNotMetException;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Collection<EventDtoFull>> getAllEventsPublicFilter(@RequestParam(required = false) String text,
                                                                       @RequestParam(required = false) List<Long> categories,
                                                                       @RequestParam(required = false) Boolean paid,
                                                                       @RequestParam(required = false) String rangeStart,
                                                                       @RequestParam(required = false) String rangeEnd,
                                                                       @RequestParam(defaultValue = "false") boolean onlyAvailable,
                                                                       @RequestParam(required = false) String sort,
                                                                       @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                                       @RequestParam(name = "size", defaultValue = "10") Integer size,
                                                                       HttpServletRequest request) {
        // возвращает полную информацию обо всех событиях подходящих под переданные условия
        // В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список
        log.info("Публичный запрос на получение всех событий с сортировкой");
        log.info("client ip: {}", request.getRemoteAddr());
        log.info("endpoint path: {}", request.getRequestURI());

        if ("0".equals(text)) {
            throw new ConditionsNotMetException("Параметр text не может быть 0");
        }
        if (categories != null && categories.size() == 1 && categories.get(0) == 0) {
            throw new ConditionsNotMetException("Параметр categories не может быть 0");
        }

        Collection<EventDtoFull> events = eventService.getAllEventsPublicFilter(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDtoFull> getEventById(@PathVariable Long eventId,
                                                      HttpServletRequest request) {
        // возвращает краткую информацию о событии
        log.info("Публичный запрос на получение события по id");
        log.info("client ip: {}", request.getRemoteAddr());
        log.info("endpoint path: {}", request.getRequestURI());

        EventDtoFull event = eventService.getEventById(eventId, request);


        log.info("Public getEventById returned event title={}", event.getTitle());
        return ResponseEntity.ok(event);
    }
}
