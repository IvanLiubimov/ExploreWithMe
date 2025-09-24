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
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    final StatRepository statRepository;
    final HitMapper hitMapper;
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

        Collection<Hit> listOfHits = uris.isEmpty()
                ? statRepository.getStats(startInstant, endInstant)
                : statRepository.getStatsByUris(startInstant, endInstant, uris); //пришедшие хиты из репозиторя

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
            stats.sort((h1, h2) -> Long.compare(h2.getHits(), h1.getHits()));
            return stats;
        }
        Map<String, Map<String, Long>> normalHits = new HashMap<>();
        for (Hit hit : listOfHits) {
            normalHits
                    .computeIfAbsent(hit.getApp(), a -> new HashMap<>())
                    .merge(hit.getUri(), 1L, Long::sum);

        }
        List<HitDtoStatResponse> stats = new ArrayList<>();

        for (Map.Entry<String, Map<String, Long>> appEntry : normalHits.entrySet()) {
            String app = appEntry.getKey();
            Map<String, Long> uriMap = appEntry.getValue();
            for (Map.Entry<String, Long> uriEntry : uriMap.entrySet()) {
                String uri = uriEntry.getKey();
                Long statCount = uriEntry.getValue();

                stats.add(new HitDtoStatResponse(app, uri, statCount));
            }
        }
        stats.sort((h1, h2) -> Long.compare(h2.getHits(), h1.getHits()));
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
        }

    private void saveValidate(HitDtoRequest hitDtoRequest) {
        if (hitDtoRequest.getApp() == null || hitDtoRequest.getApp().isBlank()) {
            throw new IllegalArgumentException("App не может быть null или пустым");
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
