package ru.practicum.ewmservice.comments.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.comments.model.Comment;
import ru.practicum.ewmservice.events.model.Event;
import ru.practicum.ewmservice.user.model.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class CommentMapper {

    public CommentDtoToResponse toDto(Comment comment) {
        return CommentDtoToResponse.builder()
                .text(comment.getText())
                .event(comment.getEvent().getId())
                .creator(comment.getCreator().getId())
                .created(toLocalDateTime(comment.getCreated()))
                .build();
    }

    public Comment fromDto(CommentDto commentDto, User user, Event event) {
        return Comment.builder()
                .text(commentDto.getText())
                .created(Instant.now())
                .creator(user)
                .event(event)
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null
               ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                : null;
    }

    private Instant toInstant(LocalDateTime localDateTime){
        return localDateTime != null
                ? localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                : null;
    }
}
