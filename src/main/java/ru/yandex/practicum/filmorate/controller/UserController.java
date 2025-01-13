package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    // @Valid инициирует автоматическую валидацию аргумента согласно описанию его в классе
    // @RequestBody чтобы создать объект из тела запроса на добавление или обновление сущности
    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Создание нового пользователя: {}", user.getLogin());
        if (Objects.isNull(user.getName())) {
            user = user.toBuilder()
                    .name(user.getLogin())
                    .build();
        } else {
            user = user.toBuilder()
                    .id(getNextId())
                    .build();
        }
        users.put(user.getId(), user);
        log.info("Пользователь c id = {} успешно добавлен", user.getId());
        return user;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Вывод {} пользователей", users.size());
        return users.values();
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        Long id = newUser.getId();
        if (users.containsKey(id)) {
            log.info("Обновление данных пользователя с id = {}", id);
            User user = newUser.toBuilder()
                    .name(newUser.getName())
                    .id(id)
                    .email(newUser.getEmail())
                    .login(newUser.getLogin())
                    .birthday(newUser.getBirthday())
                    .build();
            users.put(id, user);
            log.info("Пользователь с id = {} успешно обновлён", user.getId());
            return user;
        } else {
            log.error("Пользователь с id = {} не найден", id);
            throw new ValidationException("Пользователь с id = " + id + " не найден");
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
