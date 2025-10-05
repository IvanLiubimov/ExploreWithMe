package ru.practicum.ewmservice.events.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.category.dto.CategoryDto;
import ru.practicum.ewmservice.category.dto.CategoryMapper;
import ru.practicum.ewmservice.category.model.Category;
import ru.practicum.ewmservice.category.repository.CategoryRepository;
import ru.practicum.ewmservice.events.dto.*;
import ru.practicum.ewmservice.events.model.Event;
import ru.practicum.ewmservice.events.model.Location;
import ru.practicum.ewmservice.events.model.State;
import ru.practicum.ewmservice.events.model.StateAction;
import ru.practicum.ewmservice.events.repository.EventRepository;
import ru.practicum.ewmservice.events.repository.LocationRepository;
import ru.practicum.ewmservice.exception.ConditionsNotMetException;
import ru.practicum.ewmservice.exception.ConflictException;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.request.model.RequestStatus;
import ru.practicum.ewmservice.request.repository.RequestRepository;
import ru.practicum.ewmservice.stat.client.StatClientEwm;
import ru.practicum.ewmservice.user.dto.UserShortDto;
import ru.practicum.ewmservice.user.dto.UserMapper;
import ru.practicum.ewmservice.user.model.User;
import ru.practicum.ewmservice.user.repository.UserRepository;
import ru.practicum.ewmservice.stat.dto.HitDtoRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final StatClientEwm statClientEwm;
    private final RequestRepository requestRepository;
    private final LocationRepository locationRepository;


    @Override
    public Collection<EventDtoFull> searchEvents(List<Long> userIds,
                                                 List<State> states,
                                                 List<Long> categories,
                                                 String rangeStart,
                                                 String rangeEnd,
                                                 Integer from,
                                                 Integer size) {

        int page = from / size;
        Pageable pageable = PageRequest.of(page, size);

        List<Long> filterUserIds = (userIds == null || userIds.isEmpty() || userIds.contains(0L)) ? null : userIds;
        List<Long> filterCategories = (categories == null || categories.isEmpty() || categories.contains(0L)) ? null : categories;
        List<State> filterStates = (states == null) ? null : (states.isEmpty() ? List.of() : states);

        Instant start;
        Instant end;
        try {
            start = rangeStart != null ?
                    LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                    : Instant.parse("1970-01-01T00:00:00Z");
        } catch (Exception e) {
            start = Instant.parse("1970-01-01T00:00:00Z");
        }

        try {
            end = rangeEnd != null ?
                    LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                    : Instant.parse("3000-12-31T23:59:59Z");
        } catch (Exception e) {
            end = Instant.parse("3000-12-31T23:59:59Z");
        }

        Collection<Event> events = eventRepository.searchEvents(
                filterUserIds, filterStates, filterCategories, start, end, pageable
        );

        Map<Long, Integer> confirmedRequestsMap = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> requestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED).intValue()
                ));

        Map<Long, Long> viewsMap = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> statClientEwm.getViews(e.getId())
                ));

        return events.stream()
                .map(event -> {
                    CategoryDto categoryDto = categoryMapper.toDto(event.getCategory());
                    UserShortDto initiatorDto = userMapper.toShortDto(event.getInitiator());
                    State state = event.getState();

                    EventDtoFull dto = eventMapper.toFullEventDto(event, categoryDto, initiatorDto, state)
                            .toBuilder()
                            .confirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0))
                            .views(viewsMap.getOrDefault(event.getId(), 0L))
                            .build();

                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public EventDtoFull editEvent(Long eventId, UpdateEventAdminRequest event) {
        Event oldEvent = getEventIfExists(eventId);

        Category category = null;
        if (event.getCategory() != null) {
            category = categoryRepository.findById(event.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        }

        validateForAdminEdit(event, oldEvent, event.getStateAction());
        Event updatedEvent = eventMapper.adminUpdateEventFromDto(oldEvent, event, category);
        Event saved = eventRepository.save(updatedEvent);

        return eventMapper.toFullEventDto(
                saved,
                category != null ? categoryMapper.toDto(category) : categoryMapper.toDto(saved.getCategory()),
                userMapper.toShortDto(saved.getInitiator()),
                saved.getState()
        );
    }

    @Override
    public Collection<EventDtoFull> getAllEventsPublicFilter(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd, boolean onlyAvailable, String sort, Integer from, Integer size, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(from / size, size);

        Instant start;
        Instant end;
        try {
            start = rangeStart != null ? Instant.parse(rangeStart)
                    : Instant.parse("1970-01-01T00:00:00Z"); // начало эпохи Unix
        } catch (Exception e) {
            start = Instant.parse("1970-01-01T00:00:00Z");
        }

        try {
            end = rangeEnd != null ? Instant.parse(rangeEnd)
                    : Instant.parse("3000-12-31T23:59:59Z"); // разумный максимум
        } catch (Exception e) {
            end = Instant.parse("3000-12-31T23:59:59Z");
        }

        Collection<Event> events = eventRepository.findAllWithSort(text, categories, start, end, State.PUBLISHED, pageable);

        HitDtoRequest hitDto = new HitDtoRequest(
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                java.time.LocalDateTime.now()
        );

        statClientEwm.saveHit(hitDto);

        return events.stream()
                .map(event -> {
                    CategoryDto categoryDto = categoryMapper.toDto(event.getCategory());
                    UserShortDto initiatorDto = userMapper.toShortDto(event.getInitiator());
                    State state = event.getState();

                    int confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED).intValue();

                    long views = statClientEwm.getViews(event.getId());

                    return eventMapper.toFullEventDto(event, categoryDto, initiatorDto, state)
                            .toBuilder()
                            .confirmedRequests(confirmedRequests)
                            .views(views)
                            .build();
                }).toList();
    }

    @Override
    public EventDtoFull getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("События с id = " + eventId + " не найден"));

        HitDtoRequest hitDto = new HitDtoRequest(
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        );
        statClientEwm.saveHit(hitDto);

        long views = statClientEwm.getViews(event.getId());

        CategoryDto categoryDto = categoryMapper.toDto(event.getCategory());
        UserShortDto initiatorDto = userMapper.toShortDto(event.getInitiator());

        return eventMapper.toFullEventDto(event, categoryDto, initiatorDto, event.getState())
                .toBuilder()
                .views(views)
                .build();
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageRequest);


        Map<Long, Long> confirmedRequests = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> requestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED)
                ));

        // Собираем views
        Map<Long, Long> views = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> statClientEwm.getViews(e.getId())
                ));

        return events.stream()
                .map(event -> eventMapper.toShortEventDto(
                        event,
                        categoryMapper.toDto(event.getCategory()),
                        userMapper.toShortDto(event.getInitiator()),
                        confirmedRequests.getOrDefault(event.getId(), 0L).intValue(),
                        views.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    @Override
    public EventDtoFull createUserEvent(Long userId, NewEventDto newEventDto) {
        validate(newEventDto);
        Category category = getCategoryIfExists(newEventDto.getCategory());
        User user = getUserIfExists(userId);
        Event event = eventMapper.toNewModel(newEventDto, category, user);
        Location savedLocation = locationRepository.save(event.getLocation());

        CategoryDto categoryDto = categoryMapper.toDto(category);
        UserShortDto userShortDto = userMapper.toShortDto(user);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toFullEventDto(savedEvent, categoryDto, userShortDto, State.PENDING);
    }

    @Override
    public EventDtoFull getUserEventById(Long userId, Long eventId) {
        Event event = getEventIfExists(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Пользователь не является инициатором события с id=" + eventId);
        };

        CategoryDto categoryDto = categoryMapper.toDto(event.getCategory());
        UserShortDto userShortDto = userMapper.toShortDto(event.getInitiator());

        return eventMapper.toFullEventDto(event, categoryDto, userShortDto, event.getState());
    }

    @Override
    public EventDtoFull editUserEventById(Long userId, Long eventId, UpdateEventUserRequest newEventDto) {
        getUserIfExists(userId);
        Event oldEvent = getEventIfExists(eventId);
        if (oldEvent.getEventDate() != null &&
                oldEvent.getEventDate().isBefore(Instant.now().plus(2, ChronoUnit.HOURS))) {
            throw new ConditionsNotMetException("Дата события должна быть не раньше, чем через 2 часа от текущего момента");
        }

        if (!oldEvent.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Пользователь не является инициатором события с id=" + eventId);
        }

        Category category = null;

        if (newEventDto == null) {
            oldEvent.setState(State.CANCELED);
        } else {

            validateForUserUpdate(newEventDto, oldEvent);

            if (newEventDto.getCategory() != null) {
                category = getCategoryIfExists(newEventDto.getCategory());
            }

            eventMapper.updateEventFromDto(newEventDto, oldEvent, category);
        }

        Event updatedEvent = eventRepository.save(oldEvent);

        CategoryDto categoryDto = categoryMapper.toDto(updatedEvent.getCategory());
        UserShortDto userShortDto = userMapper.toShortDto(updatedEvent.getInitiator());

        return eventMapper.toFullEventDto(updatedEvent, categoryDto, userShortDto, updatedEvent.getState());
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private Category getCategoryIfExists(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Каиегория с id = " + catId + " не найден"));
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = " + eventId + " не найдено"));
    }

    private void validate(NewEventDto newEventDto) {
        // Обратите внимание: дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
        if (newEventDto.getAnnotation() == null || newEventDto.getAnnotation().isBlank()) {
            throw new ConditionsNotMetException("Аннотация события не может быть пустой");
        }

        if (newEventDto.getDescription() == null || newEventDto.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание события не может быть пустым");
        }

        if (newEventDto.getDescription().length() < 20 || newEventDto.getDescription().length() > 7000) {
            throw new ConditionsNotMetException("Описание события не может быть таким коротким");
        }

        if (newEventDto.getAnnotation().length() < 20 || newEventDto.getAnnotation().length() > 2000) {
            throw new ConditionsNotMetException("Аннотация события должна быть не менее 20 и не более 2000 символов");
        }

        if (newEventDto.getTitle().length() < 3 || newEventDto.getTitle().length() > 120) {
            throw new ConditionsNotMetException("Название события должно быть не менее 3 и не более 120 символов");
        }

        if (newEventDto.getTitle() == null || newEventDto.getTitle().isBlank()) {
            throw new ConditionsNotMetException("Заголовок события не может быть пустым");
        }

        if (newEventDto.getEventDate() == null) {
            throw new ConditionsNotMetException("Дата события обязательна");
        }

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConditionsNotMetException(
                    "Дата события должна быть не раньше, чем через 2 часа от текущего момента"
            );
        }

        if (newEventDto.getParticipantLimit() != null && newEventDto.getParticipantLimit() < 0) {
            throw new ConditionsNotMetException("Лимит участников не может быть отрицательным");
        }

        if (newEventDto.getLocation() == null) {
            throw new ConditionsNotMetException("Локация события обязательна");
        }
    }

    private void validateForUserUpdate(UpdateEventUserRequest newEventDto, Event oldEvent) {
        if (newEventDto.getAnnotation() != null) {
            String annotation = newEventDto.getAnnotation();
            if (annotation.isBlank()) {
                throw new ConditionsNotMetException("Аннотация события не может быть пустой");
            }
            if (annotation.length() < 20 || annotation.length() > 2000) {
                throw new ConditionsNotMetException("Аннотация события должна быть не менее 20 и не более 2000 символов");
            }
        }
        if (newEventDto.getDescription() != null) {
            String description = newEventDto.getDescription();
            if (description.isBlank()) {
                throw new ConditionsNotMetException("Описание события не может быть пустым");
            }
            if (description.length() < 20 || description.length() > 7000) {
                throw new ConditionsNotMetException("Описание события должно быть не менее 20 и не более 7000 символов");
            }
        }
        if (newEventDto.getTitle() != null) {
            String title = newEventDto.getTitle();
            if (title.isBlank()) {
                throw new ConditionsNotMetException("Заголовок события не может быть пустым");
            }
            if (title.length() < 3 || title.length() > 120) {
                throw new ConditionsNotMetException("Название события должно быть не менее 3 и не более 120 символов");
            }
        }
        if (newEventDto.getEventDate() != null) {
            LocalDateTime newDate = newEventDto.getEventDate();
            if (newDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConditionsNotMetException("Дата события должна быть не раньше, чем через 2 часа от текущего момента");
            }
        }
        if (!(oldEvent.getState() == State.CANCELED || oldEvent.getState() == State.WAITING)) {
            throw new ConflictException("Изменить можно только отмененные события или события в состоянии ожидания модерации");
        }

        if (newEventDto.getParticipantLimit() != null && newEventDto.getParticipantLimit() < 0) {
            throw new ConditionsNotMetException("Лимит участников не может быть отрицательным");
        }
    }

    private void validateForAdminEdit(UpdateEventAdminRequest event, Event oldEvent, StateAction stateAction) {
        if (event == null) return;

        if (event.getEventDate() != null) {
            Instant newDate = event.getEventDate().atZone(ZoneId.systemDefault()).toInstant();
            if (newDate.isBefore(Instant.now().plus(2, ChronoUnit.HOURS))) {
                throw new ConditionsNotMetException("Дата события должна быть не раньше, чем через 2 часа от текущего момента");
            }
        }

        if (event.getDescription() != null) {
            String description = event.getDescription();
            if (description.length() < 20 || description.length() > 7000) {
                throw new ConditionsNotMetException("Описание события должно быть не менее 20 и не более 7000 символов");
            }
        }

        if (event.getAnnotation() != null) {
            String annotation = event.getAnnotation();
            if (annotation.length() < 20 || annotation.length() > 2000) {
                throw new ConditionsNotMetException("Аннотация события должна быть не менее 20 и не более 2000 символов");
            }
        }

        if (event.getTitle() != null) {
            String title = event.getTitle();
            if (title.length() < 3 || title.length() > 120) {
                throw new ConditionsNotMetException("Название события должно быть не менее 3 и не более 120 символов");
            }
        }

        if (stateAction == StateAction.PUBLISH_EVENT && oldEvent.getState() != State.WAITING) {
            throw new ConflictException("Событие можно публиковать только если оно в состоянии ожидания публикации");
        }

        if (stateAction == StateAction.REJECT_EVENT && oldEvent.getState() == State.PUBLISHED) {
            throw new ConflictException("Опубликованное событие нельзя отклонить");
        }
    }
}
