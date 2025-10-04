package ru.practicum.ewmservice.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.category.dto.CategoryDto;
import ru.practicum.ewmservice.category.dto.CategoryMapper;
import ru.practicum.ewmservice.category.model.Category;
import ru.practicum.ewmservice.category.repository.CategoryRepository;
import ru.practicum.ewmservice.events.repository.EventRepository;
import ru.practicum.ewmservice.exception.ConditionsNotMetException;
import ru.practicum.ewmservice.exception.ConflictException;
import ru.practicum.ewmservice.exception.NotFoundException;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));
        return categoryMapper.toDto(category);
    }

    @Override
    public Collection<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {

        validate(categoryDto);
        Category category = categoryRepository.save(categoryMapper.fromDto(categoryDto));
        return categoryMapper.toDto(category);
    }

    @Override
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Категория с id =" + catId + " не найдена");
        }

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Нельзя удалять категории, в которых есть события");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto editCategory(Long catId, CategoryDto categoryDto) {
        validateForUpdate(categoryDto);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + catId + " не найдена"));

        if (categoryDto.getName() != null && !categoryDto.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(categoryDto.getName())) {
                throw new ConflictException("Категория с именем " + categoryDto.getName() + " уже существует");
            }
            category.setName(categoryDto.getName());
        }
        Category updated = categoryRepository.save(category);
        return categoryMapper.toDto(updated);
    }

    private void validate(CategoryDto categoryDto) {
        if (categoryDto.getName() == null || categoryDto.getName().isBlank()) {
            throw new ConditionsNotMetException("У категории должно быть имя");
        }
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Категория с именем " + categoryDto.getName() + " уже существует");
        }
        if (categoryDto.getName().length() > 50) {
            throw new ConditionsNotMetException("Длина имени категории должна быть не более 50 символов");
        }
    }

    private void validateForUpdate(CategoryDto categoryDto) {
        if (categoryDto.getName().length() > 50) {
            throw new ConditionsNotMetException("Длина имени категории должна быть не более 50 символов");
        }
    }
}
