package ru.practicum.ewmservice.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.category.model.Category;
import ru.practicum.ewmservice.comments.dto.CommentDto;
import ru.practicum.ewmservice.comments.dto.CommentDtoToResponse;
import ru.practicum.ewmservice.comments.dto.CommentMapper;
import ru.practicum.ewmservice.comments.model.Comment;
import ru.practicum.ewmservice.comments.repository.CommentRepository;
import ru.practicum.ewmservice.events.model.Event;
import ru.practicum.ewmservice.events.repository.EventRepository;
import ru.practicum.ewmservice.exception.ConditionsNotMetException;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.user.model.User;
import ru.practicum.ewmservice.user.repository.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public CommentDtoToResponse createUserComment(Long userId, Long eventId, CommentDto commentDto) {
        validate(commentDto);
        User user = getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        Comment comment =  commentRepository.save(commentMapper.fromDto(commentDto, user, event));
        return commentMapper.toDto(comment);
    }

    @Override
    public CommentDtoToResponse editUserComment(Long commentId, CommentDto commentDto, Long userId) {
        validate(commentDto);
        Comment comment = getCommentIfExists(commentId);
        if (!comment.getCreator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Изменять можно только собственный комментарий");
        }
        comment.setText(commentDto.getText());
        Comment updated = commentRepository.save(comment);
        return commentMapper.toDto(updated);
    }

    @Override
    public void deleteUserComment(Long userId, Long commentId) {
        User user = getUserIfExists(userId);
        Comment comment = getCommentIfExists(commentId);
        if (!userId.equals(comment.getCreator().getId())) {
            throw new ConditionsNotMetException("Удалять можно только собственный комментарий");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public Collection<CommentDtoToResponse> getUserComment(Long userId) {
        getUserIfExists(userId);
        Collection<Comment> userComments = commentRepository.findAllByCreatorId(userId);
        return userComments.stream().map(commentMapper::toDto).toList();
    }

    @Override
    public void deleteComment(Long commentId) {
        getCommentIfExists(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    public Collection<CommentDtoToResponse> getEventComments(Long eventId) {
        getEventIfExists(eventId);
        Collection<Comment> eventComments = commentRepository.findAllByEventId(eventId);
        return eventComments.stream().map(commentMapper::toDto).toList();
    }

    private void validate(CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ConditionsNotMetException("Комментарий должен содержать текст");
        }
    }


    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    private Comment getCommentIfExists(Long catId) {
        return commentRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = " + catId + " не найден"));
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("События с id = " + eventId + " не найдено"));
    }


}
