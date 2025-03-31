package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;


@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Optional<User> findById(long id) {
        if (users.containsKey(id)) {
            return Optional.of(users.get(id));
        }
        return Optional.empty();
    }

    // @Valid инициирует автоматическую валидацию аргумента согласно описанию его в классе
    // @RequestBody чтобы создать объект из тела запроса на добавление или обновление сущности
    @Override
    public User create(User user) {
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
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User update(User newUser) {
        Long id = newUser.getId();
        if (users.containsKey(id)) {
            User user = newUser.toBuilder()
                    .name(newUser.getName())
                    .id(id)
                    .email(newUser.getEmail())
                    .login(newUser.getLogin())
                    .birthday(newUser.getBirthday())
                    .build();
            users.put(id, user);
            return user;
        } else {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
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
