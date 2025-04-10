package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserCrudService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service("dbUserCrudService")
@RequiredArgsConstructor
public class DbUserCrudServiceImpl implements UserCrudService {

    @Qualifier("userDbStorage")
    private final UserStorage storage;

    @Override
    public User create(User user) {
        return storage.create(user);
    }

    @Override
    public User update(User user) {
        return storage.update(user);
    }

    @Override
    public Collection<User> findAll() {
        return storage.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return storage.findById(id);
    }
}
