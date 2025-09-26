package stat.server.repository;

import stat.server.model.Hit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<Hit,Long> {

    //@Query("SELECT h FROM Hit h " +
    //        "WHERE h.dateTime BETWEEN :start AND :end " +
    //        "AND (:uris IS NULL OR h.uri IN :uris)")
    //Collection<Hit> getStats(Instant start, Instant end, List<String> uris);

    @Query("SELECT h FROM Hit h WHERE h.dateTime BETWEEN :start AND :end")
    Collection<Hit> getStats(Instant start, Instant end);

    @Query("SELECT h FROM Hit h WHERE h.dateTime BETWEEN :start AND :end AND h.uri IN :uris")
    Collection<Hit> getStatsByUris(Instant start, Instant end, List<String> uris);

}
