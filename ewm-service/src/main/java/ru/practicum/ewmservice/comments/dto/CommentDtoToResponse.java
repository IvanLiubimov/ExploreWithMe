package ru.practicum.ewmservice.comments.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDtoToResponse {
    private String text;
    private LocalDateTime created;
    private Long creator;
    private Long event;
}
