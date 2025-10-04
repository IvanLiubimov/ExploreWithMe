package ru.practicum.ewmservice.events.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.category.dto.CategoryDto;
import ru.practicum.ewmservice.category.model.Category;
import ru.practicum.ewmservice.events.model.Event;
import ru.practicum.ewmservice.events.model.Location;
import ru.practicum.ewmservice.events.model.State;
import ru.practicum.ewmservice.events.model.StateAction;
import ru.practicum.ewmservice.user.dto.UserShortDto;
import ru.practicum.ewmservice.user.model.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class EventMapper {

    public Event toNewModel(NewEventDto newEventDto, Category category, User user) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .createdOn(Instant.now())
                .description(newEventDto.getDescription())
                .category(category)
                .confirmedRequests(0)
                .location(newEventDto.getLocation())
                .eventDate(toInstant(newEventDto.getEventDate()))
                .initiator(user)
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0)
                .publishedOn(null)
                .requestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true)
                .state(State.WAITING)
                .title(newEventDto.getTitle())
                .views(0L)
                .build();
    }

    public EventShortDto toShortEventDto(Event event,
                                         CategoryDto categoryDto,
                                         UserShortDto initiator,
                                         int confirmedRequests,
                                         long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(categoryDto)
                .eventDate(toLocalDateTime(event.getEventDate()))
                .initiator(initiator)
                .paid(event.getPaid())
                .views(views)
                .confirmedRequests(confirmedRequests)
                .build();
    }

    public LocationDto toLocationDto(Location location){
        LocationDto locDto = new LocationDto();
        locDto.setLat(location.getLat());
        locDto.setLon(location.getLon());
        return locDto;
    }

    public EventDtoFull toFullEventDto(Event event, CategoryDto categoryDto, UserShortDto initiator, State state) {
        return EventDtoFull.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(categoryDto)
                .location(toLocationDto(event.getLocation()))
                .eventDate(toLocalDateTime(event.getEventDate()))
                .initiator(initiator)
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .createdOn(toLocalDateTime(event.getCreatedOn()))
                .publishedOn(toLocalDateTime(event.getPublishedOn()))
                .state(state.toString())
                .views(event.getViews())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }


    public void updateEventFromDto(UpdateEventUserRequest newEventDto, Event oldEvent, Category category) {
        if (newEventDto.getAnnotation() != null) {
            oldEvent.setAnnotation(newEventDto.getAnnotation());
        }

        if (newEventDto.getDescription() != null) {
            oldEvent.setDescription(newEventDto.getDescription());
        }

        if (newEventDto.getEventDate() != null) {
            oldEvent.setEventDate(toInstant(newEventDto.getEventDate()));
        }

        if (newEventDto.getLocation() != null) {
            oldEvent.setLocation(newEventDto.getLocation());
        }

        if (newEventDto.getTitle() != null) {
            oldEvent.setTitle(newEventDto.getTitle());
        }

        if (newEventDto.getPaid() != null) {
            oldEvent.setPaid(newEventDto.getPaid());
        }

        if (newEventDto.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(newEventDto.getParticipantLimit());
        }

        if (newEventDto.getRequestModeration() != null) {
            oldEvent.setRequestModeration(newEventDto.getRequestModeration());
        }

        if (category != null) {
            oldEvent.setCategory(category);
        }

        if (newEventDto.getStateAction() != null) {
            switch (newEventDto.getStateAction()) {
                case SEND_TO_REVIEW -> {
                    oldEvent.setState(State.PENDING);
                    oldEvent.setPublishedOn(Instant.now());
                }
                case CANCEL_REVIEW -> {
                    oldEvent.setState(State.CANCELED);
                }
            }
        }

    }

    public Event adminUpdateEventFromDto(Event oldEvent, UpdateEventAdminRequest eventUpd, Category category) {
        if (eventUpd.getAnnotation() != null) {
            oldEvent.setAnnotation(eventUpd.getAnnotation());
        }

        if (category != null) {
            oldEvent.setCategory(category);
        }

        if (eventUpd.getDescription() != null) {
            oldEvent.setDescription(eventUpd.getDescription());
        }

        if (eventUpd.getEventDate() != null) {
            oldEvent.setEventDate(toInstant(eventUpd.getEventDate()));
        }

        if (eventUpd.getLocation() != null) {
            oldEvent.setLocation(eventUpd.getLocation());
        }

        if (eventUpd.getPaid() != null) {
            oldEvent.setPaid(eventUpd.getPaid());
        }

        if (eventUpd.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(eventUpd.getParticipantLimit());
        }
        if (eventUpd.getRequestModeration() != null) {
            oldEvent.setRequestModeration(eventUpd.getRequestModeration());
        }
        if (eventUpd.getTitle() != null) {
            oldEvent.setTitle(eventUpd.getTitle());
        }
        if (eventUpd.getStateAction() != null) {
            switch (eventUpd.getStateAction()) {
                case PUBLISH_EVENT -> {
                    oldEvent.setState(State.PUBLISHED);
                    oldEvent.setPublishedOn(Instant.now());
                }
                case REJECT_EVENT -> oldEvent.setState(State.CANCELED);
            }
        }

        return oldEvent;
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime != null
                ? localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                : null;
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null
                ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                : null;
    }
}
