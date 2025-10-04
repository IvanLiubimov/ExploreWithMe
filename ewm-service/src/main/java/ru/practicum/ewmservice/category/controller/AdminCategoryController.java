package ru.practicum.ewmservice.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.category.dto.CategoryDto;
import ru.practicum.ewmservice.category.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
        //Обратите внимание: имя категории должно быть уникальным
        log.info("Запрос от администратора на создание категории '{}'", categoryDto.getName());
        CategoryDto createdCat = categoryService.createCategory(categoryDto);
        log.info("Категория создана: id={}, name={}", createdCat.getId(), createdCat.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCat);
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long catId) {
        //Обратите внимание: с категорией не должно быть связано ни одного события.
        log.info("Запрос от администратора на удаление категории по id={}", catId);
        categoryService.deleteCategory(catId);
        log.info("Категория id={} deleted", catId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> editCategory(@PathVariable Long catId,
                                                    @RequestBody CategoryDto categoryDto) {
        //Обратите внимание: имя категории должно быть уникальным
        log.info("Запрос от администратора на изменение категории id={}, new name='{}'", catId, categoryDto.getName());
        CategoryDto updatedCat = categoryService.editCategory(catId, categoryDto);
        log.info("Категория изменена: id={}, name={}", updatedCat.getId(), updatedCat.getName());
        return ResponseEntity.ok(updatedCat);
    }
}
