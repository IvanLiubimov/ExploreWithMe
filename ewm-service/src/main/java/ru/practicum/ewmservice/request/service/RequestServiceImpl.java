package ru.practicum.ewmservice.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.events.model.Event;
import ru.practicum.ewmservice.events.model.State;
import ru.practicum.ewmservice.events.repository.EventRepository;
import ru.practicum.ewmservice.exception.ConditionsNotMetException;
import ru.practicum.ewmservice.exception.ConflictException;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.request.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.request.dto.ParticipationRequestDtoUpd;
import ru.practicum.ewmservice.request.dto.RequestDtoForUpdResponse;
import ru.practicum.ewmservice.request.dto.RequestMapper;
import ru.practicum.ewmservice.request.model.Request;
import ru.practicum.ewmservice.request.model.RequestStatus;
import ru.practicum.ewmservice.request.repository.RequestRepository;
import ru.practicum.ewmservice.user.model.User;
import ru.practicum.ewmservice.user.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;


    @Override
    public ParticipationRequestDto createUserRequest(Long userId, Long eventId) {
        User user = getUserIfExists(userId);
        Event event = getEventIfExists(eventId);

        validate(user, event);
        RequestStatus status;
        //long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = RequestStatus.PENDING;
        }
        Request newRequest = new Request();
        newRequest.setRequester(user);
        newRequest.setEvent(event);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        newRequest.setCreated(now);
        newRequest.setStatus(status);

        return requestMapper.toDto(requestRepository.save(newRequest));
    }

    @Override
    public Collection<ParticipationRequestDto> getAllUserRequests(Long userId) {
        List<Request> requests = requestRepository.findAllByRequesterId(userId);

        return requests.stream()
                .map(requestMapper::toDto) // toDto теперь форматирует LocalDateTime
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ParticipationRequestDto> getUserRequestsOfEvent(Long userId, Long eventId) {
        Event event = getEventIfExists(eventId);
        getUserIfExists(userId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь не является инициатором события");
        }

        Collection<Request> requests = requestRepository.findAllByEventId(eventId);

        return requests.stream()

                .map(requestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RequestDtoForUpdResponse editUserRequestsStatusOfEvent(Long userId, Long eventId, ParticipationRequestDtoUpd updateRequest) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не является инициатором события с id=" + eventId);
        }
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Подтверждение заявок не требуется для этого события");
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (updateRequest != null && updateRequest.getStatus() == RequestStatus.CONFIRMED &&
                event.getParticipantLimit() > 0 &&
                confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Попытка принять заявку на участие в событии, когда лимит уже достигнут");
        }
        List<Request> requests;
        if (updateRequest == null || updateRequest.getIds() == null || updateRequest.getIds().isEmpty()) {
            requests = requestRepository.findAllByEventId(eventId).stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .toList();
        } else {
            requests = updateRequest.getIds().stream()
                    .map(this::getRequestIfExists)
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .toList();
        }
        if (requests.isEmpty()) {
            if (updateRequest != null && updateRequest.getStatus() == RequestStatus.CONFIRMED) {
                throw new ConflictException("Попытка принять заявку на участие в событии, когда лимит уже достигнут или заявки уже обработаны");
            }
            if (updateRequest != null && updateRequest.getStatus() == RequestStatus.REJECTED) {
                throw new ConflictException("Попытка отклонить уже обработанные заявки");
            }
        }

        for (Request r : requests) {
            if (r.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Статус можно изменить только у заявок в состоянии ожидания");
            }

            if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {
                if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
                    throw new ConflictException("Попытка принять заявку на участие в событии, когда лимит уже достигнут");
                }
                r.setStatus(RequestStatus.CONFIRMED);
                confirmedCount++;
            } else if (updateRequest.getStatus() == RequestStatus.REJECTED) {
                r.setStatus(RequestStatus.REJECTED);
            }
        }

        List<Request> savedRequests = requestRepository.saveAll(requests);
        return requestMapper.toDtoForUpdResponse(savedRequests);
    }


    @Override
    public ParticipationRequestDto cancelUserRequest(Long userId, Long requestId) {
        User user = getUserIfExists(userId);
        Request request = getRequestIfExists(requestId);

        if (!request.getRequester().getId().equals(user.getId())) {
            throw new ConditionsNotMetException("Пользователь не является владельцем запроса");
        }

        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }


    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Каиегория с id = " + eventId + " не найден"));
    }

    private Request getRequestIfExists(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("запроса с id = " + requestId + " не найдено"));
    }

    private void validate(User user, Event event) {
        if (user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("Инициатор события не может добавить запрос на своё событие");
        }

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        if (requestRepository.existsByRequesterIdAndEventId(user.getId(), event.getId())) {
            throw new ConflictException("Нельзя добавить повторный запрос");
        }

        long totalRequests = requestRepository.countByEventIdAndStatusIn(
                event.getId(),
                List.of(RequestStatus.CONFIRMED, RequestStatus.PENDING)
        );

        if (event.getParticipantLimit() > 0 && totalRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников для события");
        }
    }
}
