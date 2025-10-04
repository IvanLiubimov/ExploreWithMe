package ru.practicum.ewmservice.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.compilation.dto.CompilationDto;
import ru.practicum.ewmservice.compilation.dto.NewCompilationDto;
import ru.practicum.ewmservice.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewmservice.compilation.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationDto> createCompilation(@RequestBody NewCompilationDto newCompilationDto) {
        log.info("Запрос от администратора на создание подборки");
        CompilationDto newCompilation = compilationService.createCompilation(newCompilationDto);
        log.info("Подборка создана успешно");
        return ResponseEntity.status(201).body(newCompilation);
    }

    @DeleteMapping("/{compId}")
    public ResponseEntity<Void> deleteCompilation(@PathVariable Long compId) {
        log.info("Запрос от администратора на удаление подборки");
        CompilationDto newCompilation = compilationService.deleteCompilation(compId);
        log.info("Подборка удалена успешно");
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationDto> updateCompilation(@PathVariable Long compId,
                                                            @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        log.info("Запрос от администратора на изменение подборки");
        CompilationDto updatedCompilation = compilationService.updateCompilation(compId, updateCompilationRequest);
        log.info("Подборка изменена успешно");
        return ResponseEntity.ok(updatedCompilation);
    }

}
