package stat.server.service;

import stat.dto.HitDtoRequest;
import stat.dto.HitDtoStatResponse;
import lombok.RequiredArgsConstructor;
import stat.server.model.Hit;
import stat.server.model.HitMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import stat.server.repository.StatRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    final private StatRepository statRepository;
    final private HitMapper hitMapper;
    private static final LocalDateTime MIN_DATE = LocalDateTime.of(2020, 1, 1, 0, 0);

    @Override
    public ResponseEntity<Object> saveHit(HitDtoRequest hitDtoRequest) {
        saveValidate(hitDtoRequest);

        Hit hit = hitMapper.dtoRequestToModel(hitDtoRequest, Instant.now());
        return ResponseEntity.ok(statRepository.save(hit));
    }

    @Override
    public Collection<HitDtoStatResponse> getHits(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        requestValidate(start, end, uris);



        Instant startInstant = toInstant(start);
        Instant endInstant = toInstant(end);

        Collection<Hit> listOfHits = statRepository.getStats(startInstant, endInstant, uris); //пришедшие хиты из репозиторя

        Map<String, Map<String, Set<String>>> uniqueHits = new HashMap<>(); //мапа для хранения полей хитов

        if (unique) {
            for (Hit hit : listOfHits) {
                uniqueHits.computeIfAbsent(hit.getApp(), a -> new HashMap<>())
                        .computeIfAbsent(hit.getUri(), u -> new HashSet<>())
                        .add(hit.getIp());
            }

            List<HitDtoStatResponse> stats = new ArrayList<>();
            for (Map.Entry<String, Map<String, Set<String>>> appEntry : uniqueHits.entrySet()) {
                String app = appEntry.getKey();
                Map<String, Set<String>> uriMap = appEntry.getValue();

                for (Map.Entry<String, Set<String>> uriEntry : uriMap.entrySet()) {
                    String uri = uriEntry.getKey();
                    int hits = uriEntry.getValue().size();

                    HitDtoStatResponse hitDtoStatResponse = new HitDtoStatResponse(app, uri, hits);
                    stats.add(hitDtoStatResponse);
                }
            }

            return stats;
        }
        Map<String, Map<String, Long>> normalHits = new HashMap<>();
        for (Hit hit : listOfHits) {
            normalHits
                    .computeIfAbsent(hit.getApp(), a -> new HashMap<>())
                    .merge(hit.getUri(), 1L, Long::sum);

        }
        List<HitDtoStatResponse> stats = new ArrayList<>();

        for(Map.Entry<String, Map<String, Long>> appEntry : normalHits.entrySet()) {
            String app = appEntry.getKey();
            Map<String, Long> uriMap = appEntry.getValue();
            for(Map.Entry<String, Long> uriEntry : uriMap.entrySet()) {
                String uri = uriEntry.getKey();
                Long statCount = uriEntry.getValue();

                stats.add(new HitDtoStatResponse(app, uri, statCount));
            }
        }
        return stats;
    }

    private void requestValidate(LocalDateTime start, LocalDateTime end, List<String> uris) {
            if (start == null || end == null) {
                throw new IllegalArgumentException("Начало и конец периода не могут быть null");
            }

            if (start.isBefore(MIN_DATE)) {
                throw new IllegalArgumentException("Дата начала не может быть раньше " + MIN_DATE);
            }

            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Дата начала должна быть раньше даты окончания");
            }

            if (uris == null || uris.isEmpty()) {
                throw new IllegalArgumentException("Список URI не может быть пустым или null");
            }
        }

    private void saveValidate(HitDtoRequest hitDtoRequest) {
        if (hitDtoRequest.getApp() == null || hitDtoRequest.getApp().isBlank()) {
            throw new IllegalArgumentException("App не может быть null или пустым");
        }
        if (hitDtoRequest.getUri() == null || hitDtoRequest.getUri().isBlank()) {
            throw new IllegalArgumentException("uri не может быть null или пустым");
        }
        if (hitDtoRequest.getIp() == null || hitDtoRequest.getIp().isBlank()) {
            throw new IllegalArgumentException("ip не может быть null или пустым");
        }
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime != null
                ? localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                : null;
    }
}
