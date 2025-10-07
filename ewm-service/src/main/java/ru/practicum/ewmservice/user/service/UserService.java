package ru.practicum.ewmservice.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewmservice.user.dto.UserDto;

import java.util.Collection;
import java.util.List;

@Service
public interface UserService {

    Collection<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size);

    UserDto createUser(UserDto userDto);

    void deleteUser(Long userId);
}