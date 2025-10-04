package ru.practicum.ewmservice.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.category.dto.CategoryMapper;
import ru.practicum.ewmservice.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.compilation.dto.NewCompilationDto;
import ru.practicum.ewmservice.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewmservice.compilation.dto.CompilationMapper;
import ru.practicum.ewmservice.compilation.model.Compilation;
import ru.practicum.ewmservice.compilation.repository.CompilationRepository;
import ru.practicum.ewmservice.events.dto.EventMapper;
import ru.practicum.ewmservice.events.dto.EventShortDto;
import ru.practicum.ewmservice.events.model.Event;
import ru.practicum.ewmservice.events.repository.EventRepository;
import ru.practicum.ewmservice.exception.ConditionsNotMetException;
import ru.practicum.ewmservice.exception.NotFoundException;
import ru.practicum.ewmservice.request.model.RequestStatus;
import ru.practicum.ewmservice.request.repository.RequestRepository;
import ru.practicum.ewmservice.stat.client.StatClientEwm;
import ru.practicum.ewmservice.user.dto.UserMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final StatClientEwm statsClientEwm; // клиент для статистики
    private final RequestRepository requestRepository; // для confirmedRequests

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        validateNewCompilationRequest(newCompilationDto.getTitle(), newCompilationDto.getEvents());

        Set<Event> events = new HashSet<>();
        if (newCompilationDto.getEvents() != null) {
            events = new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));
        }
        Compilation compilation = compilationMapper.fromNewDto(newCompilationDto, events);
        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }

        return mapWithStats(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = getCompilationIfExists(compId);
        validateUpdateCompilationRequest(updateRequest.getTitle(), updateRequest.getEvents());

        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
        if (updateRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateRequest.getEvents()));
            compilation.setEvents(events);
        }

        Compilation saved = compilationRepository.save(compilation);
        return mapWithStats(saved);
    }

    @Override
    @Transactional
    public CompilationDto deleteCompilation(Long compId) {
        Compilation compilation = getCompilationIfExists(compId);
        CompilationDto dto = mapWithStats(compilation);
        compilationRepository.delete(compilation);
        return dto;
    }

    @Override
    public Collection<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        if (from < 0 || size <= 0) throw new IllegalArgumentException("Неверные параметры пагинации");

        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        return compilations.stream()
                .map(this::mapWithStats)
                .toList();
    }

    @Override
    @Transactional
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = getCompilationIfExists(compId);
        return mapWithStats(compilation);
    }

    private Compilation getCompilationIfExists(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена"));
        compilation.getEvents().size();
        return compilation;
    }

    private CompilationDto mapWithStats(Compilation compilation) {
        if (compilation.getEvents() == null || compilation.getEvents().isEmpty()) {
            return compilationMapper.toDto(compilation, Map.of(), Map.of());
        }

        List<Event> events = compilation.getEvents().stream().toList();

        Map<Long, Long> confirmedRequests = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> requestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED)
                ));

        Map<Long, Long> views = events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        e -> statsClientEwm.getViews(e.getId())
                ));

        return compilationMapper.toDto(compilation, confirmedRequests, views);
    }

    private void validateNewCompilationRequest(String title, Set<Long> eventIds) {
        if (title == null || title.isBlank()) {
            throw new ConditionsNotMetException("Название подборки не может быть пустым");
        }
        if (title.length() > 50) {
            throw new ConditionsNotMetException("Длина имени категории должна быть не более 50 символов");
        }
        validateEvents(eventIds);
    }

    private void validateUpdateCompilationRequest(String title, Set<Long> eventIds) {
        if (title != null) {
            if (title.isBlank()) {
                throw new ConditionsNotMetException("Название подборки не может быть пустым");
            }
            if (title.length() > 50) {
                throw new ConditionsNotMetException("Длина имени категории должна быть не более 50 символов");
            }
        }
        validateEvents(eventIds);
    }

    private void validateEvents(Set<Long> eventIds) {
        if (eventIds != null && !eventIds.isEmpty()) {
            List<Event> foundEvents = eventRepository.findAllById(eventIds);
            if (foundEvents.size() != eventIds.size()) {
                throw new NotFoundException("Cобытия из списка не найдены");
            }
        }
    }


}
