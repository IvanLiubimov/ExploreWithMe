package ru.practicum.ewmservice.request.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.request.dto.ParticipationRequestDto;
import ru.practicum.ewmservice.request.dto.ParticipationRequestDtoUpd;
import ru.practicum.ewmservice.request.dto.RequestDtoForUpdResponse;

import java.util.Collection;

@Service
public interface RequestService {


    Collection<ParticipationRequestDto> getAllUserRequests(Long userId);

    Collection<ParticipationRequestDto> getUserRequestsOfEvent(Long userId, Long eventId);

    RequestDtoForUpdResponse editUserRequestsStatusOfEvent(Long userId, Long eventId, ParticipationRequestDtoUpd request);

    ParticipationRequestDto createUserRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelUserRequest(Long userId, Long requestId);



}
