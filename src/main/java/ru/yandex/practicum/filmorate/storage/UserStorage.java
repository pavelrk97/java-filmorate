package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    User create(User user);

    Collection<User> findAll();

    User update(User newUser);

    Optional<User> findById(long userId);
}
