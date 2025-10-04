package ru.practicum.ewmservice.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewmservice.category.dto.CategoryDto;
import ru.practicum.ewmservice.user.dto.UserShortDto;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
public class EventDtoFull {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private int confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private UserShortDto initiator;
    private LocationDto location;
    private boolean paid;
    private int participantLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private boolean requestModeration;
    private String state;
    private String title;
    private long views;
}