package ru.practicum.ewmservice.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.events.dto.*;
import ru.practicum.ewmservice.events.service.EventService;
import ru.practicum.ewmservice.request.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.request.dto.ParticipationRequestDtoUpd;
import ru.practicum.ewmservice.request.dto.RequestDtoForUpdResponse;
import ru.practicum.ewmservice.request.service.RequestService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class UserEventController {

    private final EventService eventService;
    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getUserEvents(@PathVariable Long userId,
                                                                   @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                                   @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Получен запрос на получение событий пользователя id={}", userId);

        List<EventShortDto> events = eventService.getUserEvents(userId, from, size);
        log.info("Cобытий получено: {} ", events.size());

        return ResponseEntity.ok(events);
    }

    @PostMapping
    public ResponseEntity<EventDtoFull> createUserEvents(@PathVariable Long userId,
                                                  @RequestBody NewEventDto newEventDto) {
        log.info("Получен запрос на создание нового события пользователем id={}", userId);

        if (newEventDto.getPaid() == null) newEventDto.setPaid(false);
        if (newEventDto.getParticipantLimit() == null) newEventDto.setParticipantLimit(0);
        if (newEventDto.getRequestModeration() == null) newEventDto.setRequestModeration(true);


        EventDtoFull event = eventService.createUserEvent(userId, newEventDto);
        log.info("Создано событие с id {}", event.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDtoFull> getUserEventById(@PathVariable Long userId,
                                                         @PathVariable Long eventId) {
        log.info("Получен запрос на получение события id={} пользователя id={}", eventId, userId);
        EventDtoFull event = eventService.getUserEventById(userId, eventId);
        log.info("Возвращено событие с и названием '{}'", event.getTitle());
        return ResponseEntity.ok(event);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventDtoFull> editUserEventById(@PathVariable Long userId,
                                                          @PathVariable Long eventId,
                                                          @RequestBody(required = false) UpdateEventUserRequest newEventDto) {
        log.info("Получен запрос на редактирование события с id {} от пользователя с id {}", eventId, userId);

        EventDtoFull event = eventService.editUserEventById(userId, eventId, newEventDto);
        log.info("Событие обновлено");
        return ResponseEntity.ok(event);
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<Collection<ParticipationRequestDto>> getAllUserRequestsOfEvent(@PathVariable Long userId,
                                                                                    @PathVariable Long eventId) {
        log.info("Получен запрос на получение заявок для события id={} пользователя id={}", eventId, userId);
        Collection<ParticipationRequestDto> requests = requestService.getUserRequestsOfEvent(userId, eventId);
        log.info("Заявок получено: {} ", requests.size());
        return ResponseEntity.ok(requests);
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<RequestDtoForUpdResponse> editUserRequestsStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody(required = false) ParticipationRequestDtoUpd request) {

        log.info("Получен запрос на изменение статуса заявок для события id={} пользователя id={}", eventId, userId);
        RequestDtoForUpdResponse requestsUpd = requestService.editUserRequestsStatusOfEvent(userId, eventId, request);
        log.info("Заявки обновлены");
        return ResponseEntity.ok(requestsUpd);
    }
}
