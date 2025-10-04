package ru.practicum.ewmservice.compilation.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.category.dto.CategoryMapper;
import ru.practicum.ewmservice.compilation.model.Compilation;
import ru.practicum.ewmservice.events.dto.EventMapper;
import ru.practicum.ewmservice.events.model.Event;
import ru.practicum.ewmservice.user.dto.UserMapper;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    public CompilationDto toDto(Compilation compilation,
                                Map<Long, Long> confirmedRequests,
                                Map<Long, Long> views) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(compilation.getEvents().stream()
                        .map(event -> eventMapper.toShortEventDto(
                                event,
                                categoryMapper.toDto(event.getCategory()),
                                userMapper.toShortDto(event.getInitiator()),
                                confirmedRequests.getOrDefault(event.getId(), 0L).intValue(),
                                views.getOrDefault(event.getId(), 0L)
                        ))
                        .toList())
                .build();
    }

    public Compilation fromNewDto(NewCompilationDto dto, Set<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(dto.getPinned());
        compilation.setEvents(events);
        return compilation;
    }
}