package ru.practicum.ewmservice.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.comments.service.CommentService;

@RestController
@RequestMapping("/admin/comments/")
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        log.info("Получен запрос от администратора на удаление комментария id={} ", commentId);
        commentService.deleteComment(commentId);
        log.info("Комментарий удалён");
        return ResponseEntity.noContent().build();
    }
}
