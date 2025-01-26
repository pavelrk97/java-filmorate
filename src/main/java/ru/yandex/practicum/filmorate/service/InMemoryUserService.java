package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class InMemoryUserService {

    private final UserStorage storage; // сервисы зависят от интерфейсов классов-хранилищ

    public InMemoryUserService(UserStorage storage) {
        this.storage = storage;
    }

    private Optional<User> findUser(long userId) {
        return storage.findById(userId);
    }

    public Collection<User> getFriends(long userId) {
        Optional<User> currentUser = findUser(userId);

        if (currentUser.isPresent()) {
            Set<Long> friends = currentUser.get().getFriendsId();

            return storage.findAll()
                    .stream()
                    .filter(user -> friends.contains(user.getId()))
                    .toList();
        } else {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    public Collection<User> getCommonFriends(long firstUserId, long secondUserId) {
        User firstUser = findUser(firstUserId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id = {} не найден", firstUserId);
                    return new NotFoundException("Пользователь с id = " + firstUserId + " не найден");
                });

        User secondUser = findUser(secondUserId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id = {} не найден", secondUserId);
                    return new NotFoundException("Пользователь с id = " + secondUserId + " не найден");
                });

        // Пересечение списков друзей
        Set<Long> commonIds = firstUser.getFriendsId();
        commonIds.retainAll(secondUser.getFriendsId()); // retainAll оставляет только общие элементы (не забыть почитать)

        return storage.findAll()
                .stream()
                .filter(user -> commonIds.contains(user.getId()))
                .toList();
    }

    public User addFriend(long mainUserId, long friendUserId) {
        if (mainUserId == friendUserId) {
            log.error("Нельзя добавить в друзья самого себя");
            throw new ValidationException("Нельзя добавить в друзья самого себя");
        }

        User mainUser = findUser(mainUserId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id = {} не найден", mainUserId);
                    return new NotFoundException("Пользователь с id = " + mainUserId + " не найден");
                });

        User friendUser = findUser(friendUserId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id = {} не найден", friendUserId);
                    return new NotFoundException("Пользователь с id = " + friendUserId + " не найден");
                });

        mainUser.getFriendsId().add(friendUserId);
        friendUser.getFriendsId().add(mainUserId);

        log.info("Пользователь с id = {} добавил в друзья пользователя с id = {}", mainUserId, friendUserId);
        return mainUser;
    }

    public User removeFriend(long mainUserId, long friendUserId) {
        if (mainUserId == friendUserId) {
            log.error("Нельзя удалить себя из списка друзей");
            throw new ValidationException("Нельзя удалить себя из списка друзей");
        }

        User mainUser = findUser(mainUserId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id = {} не найден", mainUserId);
                    return new NotFoundException("Пользователь с id = " + mainUserId + " не найден");
                });

        User friendUser = findUser(friendUserId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id = {} не найден", friendUserId);
                    return new NotFoundException("Пользователь с id = " + friendUserId + " не найден");
                });

        mainUser.getFriendsId().remove(friendUserId);
        friendUser.getFriendsId().remove(mainUserId);

        log.info("Пользователь с id = {} удалил из друзей пользователя с id = {}", mainUserId, friendUserId);
        return mainUser;
    }

    public User create(User user) {
        log.info("Попытка создать пользователя: {}", user);
        User createdUser = storage.create(user);

        if (createdUser != null) {
            log.info("Пользователь успешно создан: {}", createdUser);
        } else {
            log.error("Ошибка при создании пользователя: {}", user);
        }

        return createdUser;
    }

    public User update(User user) {
        log.info("Попытка обновить данные пользователя с id = {}", user.getId());
        User updatedUser = storage.update(user);

        if (updatedUser != null) {
            log.info("Пользователь обновлён: {}", updatedUser);
        } else {
            log.error("Не удалось обновить пользователя с id = {}", user.getId());
        }

        return updatedUser;
    }

    public Collection<User> findAll() {
        log.info("Запрос списка всех пользователей");
        Collection<User> users = storage.findAll();

        if (users != null) {
            log.info("Получено {} пользователей", users.size());
        } else {
            log.warn("Не удалось получить пользователей, результат null");
        }

        return users;
    }

    public Optional<User> findById(long id) {
        log.info("Поиск пользователя с id = {}", id);
        Optional<User> user = storage.findById(id);

        if (user.isPresent()) {
            log.info("Пользователь с id = {} найден: {}", id, user.get());
        } else {
            log.warn("Пользователь с id = {} не найден", id);
        }

        return user;
    }
}
