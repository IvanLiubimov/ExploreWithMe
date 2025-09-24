package stat.server.controller;

import stat.dto.HitDtoRequest;
import stat.dto.HitDtoStatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import stat.server.service.StatService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class StatController {

    final StatService statService;

    @PostMapping(path = "/hit")
    public ResponseEntity<Object> saveHit(@RequestBody HitDtoRequest hit) {
        log.info("получен запрос на сохранение данных запроса");
        return statService.saveHit(hit);
    }

    @GetMapping(path = "/stats")
    public ResponseEntity<Collection<HitDtoStatResponse>> getHits(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {

        log.info("получен запрос на получение данных запроса");

        String decodedStart = URLDecoder.decode(start, StandardCharsets.UTF_8);
        String decodedEnd = URLDecoder.decode(end, StandardCharsets.UTF_8);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(decodedStart, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(decodedEnd, formatter);

        if (uris == null) {
            uris = Collections.emptyList();
        }

        Collection<HitDtoStatResponse> result = statService.getHits(startDateTime, endDateTime, uris, unique);

        return ResponseEntity.ok(result);
    }
}
