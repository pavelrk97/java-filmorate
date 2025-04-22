package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserCrudService {

    User create(User user);

    User update(User user);

    Collection<User> findAll();

    Optional<User> findById(Long id);
}
