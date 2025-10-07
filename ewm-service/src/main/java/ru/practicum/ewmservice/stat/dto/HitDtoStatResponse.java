package ru.practicum.ewmservice.stat.dto;

public record HitDtoStatResponse(
        String uri,
        long hits
) {}