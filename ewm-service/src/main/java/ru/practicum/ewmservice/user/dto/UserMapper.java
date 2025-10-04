package ru.practicum.ewmservice.user.dto;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewmservice.user.model.User;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId().toString());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public UserShortDto toShortDto(User user) {
        if (user == null) return null;
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        return dto;
    }

    public User fromDto(UserDto dto) {
        if (dto == null) return null;
        User user = new User();
        user.setId(dto.getId() != null ? Long.parseLong(dto.getId()) : null);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return user;
    }
}
