package ru.practicum.ewmservice.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.request.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.request.service.RequestService;
import java.util.Collection;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Slf4j
public class PrivateRequestController {

    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<Collection<ParticipationRequestDto>> getAllUserRequests(@PathVariable Long userId) {
        log.info("Запрос от пользователя на получение всех запроса участия");
        Collection<ParticipationRequestDto> responses = requestService.getAllUserRequests(userId);
        log.info("Запрос участия получены");//В случае, если по заданным фильтрам не найдено ни одной заявки, возвращает пустой список
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> createUserRequests(@PathVariable Long userId,
                                                                                  @RequestParam Long eventId) {
        //нельзя добавить повторный запрос (Ожидается код ошибки 409)
        //инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
        //нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
        //если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
        //если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного
        log.info("Запрос от пользователя на создание запроса участия");
        ParticipationRequestDto response = requestService.createUserRequest(userId, eventId);
        log.info("Запрос участия создан создана");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelUserRequests(@PathVariable Long userId,
                                                                                  @PathVariable Long requestId) {
        log.info("Запрос от пользователя на отмену запроса участия");
        ParticipationRequestDto response = requestService.cancelUserRequest(userId, requestId);
        log.info("Запрос участия отменён");

        return ResponseEntity.ok(response);
    }
}
