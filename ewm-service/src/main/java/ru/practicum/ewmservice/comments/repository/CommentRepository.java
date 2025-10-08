package ru.practicum.ewmservice.comments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewmservice.comments.model.Comment;

import java.util.Collection;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    Collection<Comment> findAllByCreatorId(Long userId);

    Collection<Comment> findAllByEventId(Long eventId);
}
