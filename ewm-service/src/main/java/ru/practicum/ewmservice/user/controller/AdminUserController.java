package ru.practicum.ewmservice.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewmservice.user.dto.UserDto;
import ru.practicum.ewmservice.user.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@Slf4j
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Collection<UserDto>> getAllUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Запрос от администратора на получение пользователей: ids={}, from={}, size={}", ids, from, size);
        Collection<UserDto> users = userService.getAllUsers(ids, from, size);
        log.info("Получено пользователей: {}", users.size());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        log.info("Запрос от администратора на создание пользователя");
        UserDto user = userService.createUser(userDto);
        log.info("Пользователь создан: name {}, email {}", user.getName(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("Запрос на удаление пользователя id={}", userId);
        userService.deleteUser(userId);
        log.info("Пользователь id={} удалён", userId);
        return ResponseEntity.noContent().build(); // 204
    }
}

