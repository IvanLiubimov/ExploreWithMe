package ru.practicum.ewmservice.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.ewmservice.request.model.RequestStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ParticipationRequestDtoUpd {
    private List<Long> ids;
    private RequestStatus status;
}
