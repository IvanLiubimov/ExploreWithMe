package ru.practicum.ewmservice.compilation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewmservice.compilation.model.Compilation;

import java.util.List;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("""
            SELECT DISTINCT c
            FROM Compilation c
            LEFT JOIN FETCH c.events e
            LEFT JOIN FETCH e.category
            LEFT JOIN FETCH e.initiator
            WHERE c.id IN :ids AND (:pinned IS NULL OR c.pinned = :pinned)
            """)
    List<Compilation> findAllByIdInWithEventsAndPinned(@Param("ids") List<Long> ids,
                                                       @Param("pinned") Boolean pinned);


    Page<Compilation> findByPinned(Boolean pinned, Pageable pageable);

    Page<Compilation> findAll(Pageable pageable);


}

