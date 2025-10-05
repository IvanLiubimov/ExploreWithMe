package ru.practicum.ewmservice.request.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.events.model.Event;
import ru.practicum.ewmservice.request.model.Request;
import ru.practicum.ewmservice.request.model.RequestStatus;
import ru.practicum.ewmservice.user.model.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class RequestMapper {

    public ParticipationRequestDto toDto(Request request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getCreated(),
                request.getStatus().name()
        );
    }

   public Request updateStatus(Request request, RequestStatus newStatus) {
       request.setStatus(newStatus);
       return request;
   }

    public RequestDtoForUpdResponse toDtoForUpdResponse(List<Request> requests) {
        List<ParticipationRequestDto> confirmed = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.CONFIRMED)
                .map(this::toDto)
                .toList();

        List<ParticipationRequestDto> rejected = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.REJECTED)
                .map(this::toDto)
                .toList();

        return new RequestDtoForUpdResponse(confirmed, rejected);

    }

    private Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime != null
                ? localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                : null;
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null
                ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                : null;
    }

    private LocalDateTime truncateToMicroseconds(LocalDateTime ldt) {
        return ldt.withNano((ldt.getNano() / 1000) * 1000);
    }

}
