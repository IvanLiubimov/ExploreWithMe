package ru.practicum.ewmservice.category.dto;

import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.category.model.Category;

@Component
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public Category fromDto(CategoryDto dto) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
