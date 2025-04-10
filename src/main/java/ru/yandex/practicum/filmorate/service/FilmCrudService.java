package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmCrudService {
    Film create(Film film);
    Film update(Film film);
    Collection<Film> findAll();
    Film findById(Long id);
}
