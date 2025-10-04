package ru.practicum.ewmservice.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewmservice.request.model.Request;
import ru.practicum.ewmservice.request.model.RequestStatus;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("SELECT r FROM Request r " +
            "WHERE r.event.id = :eventId")
    List<Request> findAllByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM Request r " +
            "WHERE r.event.id = :eventId " +
            "AND r.status = :status")
    Long countByEventIdAndStatus(@Param("eventId") Long eventId,
                                 @Param("status") RequestStatus requestStatus);


    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    long countByEventIdAndStatusIn(Long eventId, List<RequestStatus> statuses);

    List<Request> findAllByRequesterId(Long requesterId);

    List<Request> findByIdIn (List<Long> ids);


}
