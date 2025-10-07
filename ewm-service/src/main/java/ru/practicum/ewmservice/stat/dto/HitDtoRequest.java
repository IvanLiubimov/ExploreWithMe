package ru.practicum.ewmservice.stat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder

public record HitDtoRequest(
        String app,
        String uri,
        String ip,
        LocalDateTime timestamp
) {}