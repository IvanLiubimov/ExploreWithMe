package ru.practicum.ewmservice.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.exception.ConditionsNotMetException;
import ru.practicum.ewmservice.exception.ConflictException;
import ru.practicum.ewmservice.user.dto.UserDto;
import ru.practicum.ewmservice.user.dto.UserMapper;
import ru.practicum.ewmservice.user.model.User;
import ru.practicum.ewmservice.user.repository.UserRepository;
import ru.practicum.ewmservice.exception.NotFoundException;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Collection<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size) {
        if (ids == null || ids.isEmpty()) {
            PageRequest pageRequest = PageRequest.of(from / size, size);
            return userRepository.findAll(pageRequest).stream()
                    .map(userMapper::toDto)
                    .toList();
        } else {
            return userRepository.findAllById(ids).stream()
                    .skip(from)
                    .limit(size)
                    .map(userMapper::toDto)
                    .toList();
        }
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        validate(userDto);

        User user = userMapper.fromDto(userDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        userRepository.delete(user);
    }

    private void validate(UserDto userDto) {

        String email = userDto.getEmail();
        if (email == null || email.isBlank()) {
            throw new ConditionsNotMetException("Email пользователя не может быть пустым");
        }
        if (email.length() < 6 || email.length() > 254) {
            throw new ConditionsNotMetException("Длина email должна быть от 6 до 254 символов");
        }
        if (!email.contains("@")) {
            throw new ConditionsNotMetException("Email пользователя должен содержать '@'");
        }
        String[] parts = email.split("@");
        if (parts.length != 2) {
            throw new ConditionsNotMetException("Email должен содержать ровно один символ '@'");
        }
        String localPart = parts[0];
        String domainPart = parts[1];

        if (localPart.length() > 64) {
            throw new ConditionsNotMetException("Локальная часть email не может быть длиннее 64 символов");
        }

        String[] domainParts = domainPart.split("\\.");
        for (String dp : domainParts) {
            if (dp.length() > 63) {
                throw new ConditionsNotMetException("Часть домена '" + dp + "' длиннее 63 символов");
            }
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Пользователь с email " + email + " уже существует");
        }
        String name = userDto.getName();
        if (name == null || name.isBlank()) {
            throw new ConditionsNotMetException("Имя пользователя не может быть пустым");
        }
        if (name.length() < 2 || name.length() > 250) {
            throw new ConditionsNotMetException("Длина имени пользователя должна быть от 2 до 200 символов");
        }
    }
}
