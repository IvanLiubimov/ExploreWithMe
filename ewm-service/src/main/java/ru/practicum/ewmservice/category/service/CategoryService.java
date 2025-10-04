package ru.practicum.ewmservice.category.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.category.dto.CategoryDto;

import java.util.Collection;

@Service
public interface CategoryService {
    CategoryDto getCategoryById(Long catId);

    Collection<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto createCategory(CategoryDto categoryDto);

    void deleteCategory(Long catId);

    CategoryDto editCategory(Long catId, CategoryDto categoryDto);
}
