package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmLikeService;
import ru.yandex.practicum.filmorate.storage.FilmLikeStorage;

import java.util.List;

@Service("dbFilmLikeService")
@RequiredArgsConstructor
public class DbFilmLikeServiceImpl implements FilmLikeService {
    private final FilmLikeStorage filmLikeStorage;

    @Override
    public void addLike(long filmId, long userId) {
        filmLikeStorage.addLike(filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        filmLikeStorage.removeLike(filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(Long count) {
        return filmLikeStorage.getPopularFilms(count);
    }
}

