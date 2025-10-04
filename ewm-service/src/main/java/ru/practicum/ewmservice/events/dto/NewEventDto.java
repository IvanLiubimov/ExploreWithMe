package ru.practicum.ewmservice.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewmservice.events.model.Location;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewEventDto {
    private Long id;
    private String annotation;
    private Long category;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String title;

    @JsonSetter("paid")
    public void setPaid(Object paid) {
        if (paid == null) {
            this.paid = false; // по дефолту
        } else if (paid instanceof Boolean b) {
            this.paid = b;
        } else {
            this.paid = Boolean.parseBoolean(paid.toString());
        }
    }

    @JsonSetter("requestModeration")
    public void setRequestModeration(Object requestModeration) {
        if (requestModeration == null) {
            this.requestModeration = false;
        } else if (requestModeration instanceof Boolean b) {
            this.requestModeration = b;
        } else {
            this.requestModeration = Boolean.parseBoolean(requestModeration.toString());
        }
    }
}

