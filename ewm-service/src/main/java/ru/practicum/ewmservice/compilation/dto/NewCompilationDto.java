package ru.practicum.ewmservice.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    private Set<Long> events;

    private Boolean pinned = false;

    private String title;
}
