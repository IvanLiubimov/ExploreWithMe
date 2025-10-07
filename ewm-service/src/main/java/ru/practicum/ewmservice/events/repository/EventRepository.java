package ru.practicum.ewmservice.events.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;
import ru.practicum.ewmservice.events.model.Event;
import ru.practicum.ewmservice.events.model.State;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e " +
            "WHERE (:text IS NULL OR :text = '' OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:categories IS NULL OR e.category.id in (:categories)) " +
            "AND e.eventDate BETWEEN :rangeStart AND :rangeEnd " +
            "AND e.state = :state")
    List<Event> findAllWithSort(@Param("text") String text,
                                @Param("categories") List<Long> categories,
                                @Param("rangeStart") Instant rangeStart,
                                @Param("rangeEnd") Instant rangeEnd,
                                @Param("state") State state,
                                Pageable pageable);

    boolean existsByCategoryId(Long catId);

    Optional<Event> findByIdAndState(Long id, State state);

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE (:userIds IS NULL OR e.initiator.id IN :userIds) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND e.eventDate BETWEEN :rangeStart AND :rangeEnd")
    List<Event> searchEvents(@Param("userIds") List<Long> userIds,
                             @Param("states") List<State> states,
                             @Param("categories") List<Long> categories,
                             @Param("rangeStart") Instant rangeStart,
                             @Param("rangeEnd") Instant rangeEnd,
                             Pageable pageable);
}
