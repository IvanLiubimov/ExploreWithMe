package ru.practicum.ewmservice.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.comments.dto.CommentDto;
import ru.practicum.ewmservice.comments.dto.CommentDtoToResponse;
import ru.practicum.ewmservice.comments.service.CommentService;

import java.util.Collection;


@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
public class UserCommentsController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}/comments")
    public ResponseEntity<CommentDtoToResponse> createComment(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody CommentDto commentDto) {
        log.info("Получен запрос на создание нового комментария у событию  id={} пользователем id={}", eventId, userId);

        CommentDtoToResponse createdComment = commentService.createUserComment(userId, eventId, commentDto);
        log.info("Создан комментарий пользователем");
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentDtoToResponse> editComment(@PathVariable Long userId,
                                                            @PathVariable Long commentId,
                                                            @RequestBody CommentDto commentDto) {
        log.info("Получен запрос на изменение нового комментария id={} ", commentId);
        CommentDtoToResponse editedComment = commentService.editUserComment(commentId, commentDto, userId);
        log.info("Комментарий изменён");
        return ResponseEntity.ok(editedComment);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteUserComment(@PathVariable Long userId,
                                              @PathVariable Long commentId) {
        log.info("Получен запрос на удаление нового комментария id={} ", commentId);
        commentService.deleteUserComment(userId, commentId);
        log.info("Комментарий удалён");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments")
    public ResponseEntity<Collection<CommentDtoToResponse>> getUserComments(@PathVariable Long userId) {
        log.info("Получен запрос на получение комментариев пользователя с id={} ", userId);
        Collection<CommentDtoToResponse> userComments = commentService.getUserComment(userId);
        log.info("Комментарии пользователя в количестве {} получены", userComments.size());
        return ResponseEntity.ok(userComments);
    }
}
