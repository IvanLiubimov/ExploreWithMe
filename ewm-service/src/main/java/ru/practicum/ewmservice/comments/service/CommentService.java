package ru.practicum.ewmservice.comments.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.comments.dto.CommentDto;
import ru.practicum.ewmservice.comments.dto.CommentDtoToResponse;

import java.util.Collection;

@Service
public interface CommentService {
    CommentDtoToResponse createUserComment(Long userId, Long eventId, CommentDto commentDto);

    CommentDtoToResponse editUserComment(Long commentId, CommentDto commentDto, Long userId);

    void deleteUserComment(Long userId, Long commentId);

    Collection<CommentDtoToResponse> getUserComment(Long userId);

    void deleteComment(Long commentId);

    Collection<CommentDtoToResponse> getEventComments(Long eventId);
}
