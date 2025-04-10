package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmCrudService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Service("dbFilmCrudService")
@RequiredArgsConstructor
public class DbFilmCrudServiceImpl implements FilmCrudService {
    private final FilmStorage filmStorage;

    @Override
    public Film create(Film film) {
        return filmStorage.create(film);
    }

    @Override
    public Film update(Film film) {
        return filmStorage.update(film);
    }

    @Override
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    @Override
    public Film findById(Long id) {
        return filmStorage.findById(id);
    }
}
