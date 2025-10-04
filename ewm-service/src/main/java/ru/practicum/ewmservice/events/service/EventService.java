package ru.practicum.ewmservice.events.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.events.dto.*;
import ru.practicum.ewmservice.events.model.State;

import java.util.Collection;
import java.util.List;

@Service
public interface EventService {

    Collection<EventDtoFull> searchEvents(List<Long> usersId, List<State> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size);

    EventDtoFull editEvent(Long eventId, UpdateEventAdminRequest event);

    Collection<EventDtoFull> getAllEventsPublicFilter(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd, boolean onlyAvailable, String sort, Integer from, Integer size, HttpServletRequest request);

    EventDtoFull getEventById(Long eventId, HttpServletRequest request);

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    EventDtoFull createUserEvent(Long userId, NewEventDto eventShortDto);

    EventDtoFull getUserEventById(Long userId, Long eventId);

    EventDtoFull editUserEventById(Long userId, Long eventId, UpdateEventUserRequest updEventDto);
}
