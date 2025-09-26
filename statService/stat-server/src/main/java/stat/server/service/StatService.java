package stat.server.service;

import stat.dto.HitDtoRequest;
import stat.dto.HitDtoStatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
public interface StatService {

    ResponseEntity<Object> saveHit(HitDtoRequest hit);

    Collection<HitDtoStatResponse> getHits(LocalDateTime start,
                                           LocalDateTime end,
                                           List<String> uris,
                                           boolean unique);
}
