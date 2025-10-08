package ru.practicum.ewmservice.comments.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private String text;
}
