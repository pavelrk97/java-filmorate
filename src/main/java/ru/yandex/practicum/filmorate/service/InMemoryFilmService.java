package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class InMemoryFilmService implements FilmService {

    private final FilmStorage filmStorage; // сервисы зависят от интерфейсов классов-хранилищ
    private final InMemoryUserService userService;

    String notFound = " не найден";

    private Optional<Film> findFilm(long id) {
        return filmStorage.findById(id);
    }

    private Optional<User> findUser(long id) {
        return userService.findById(id);
    }

    public InMemoryFilmService(FilmStorage filmStorage, InMemoryUserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    @Override
    public Film addLike(long filmId, long userId) {
        Optional<Film> film = findFilm(filmId);
        Optional<User> user = findUser(userId);
        String notFound = " не найден";

        if (film.isPresent() && user.isPresent()) {
            film.get().getLikes().add(userId);

            log.info("Пользователь с id = {} поставил лайк фильму с id = {}", userId, filmId);
            return film.get();

        } else if (user.isEmpty()) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + notFound);

        } else {
            log.error("Фильм с id = {} не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + notFound);
        }
    }

    @Override
    public Film removeLike(long filmId, long userId) {
        Optional<Film> film = findFilm(filmId);
        Optional<User> user = findUser(userId);

        if (film.isPresent() && user.isPresent()) {
            film.get().getLikes().remove(userId);

            log.info("Пользователь с id = {} удалил лайк фильму с id = {}", userId, filmId);
            return film.get();

        } else if (user.isEmpty()) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + notFound);

        } else {
            log.error("Фильм с id = {} не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + notFound);
        }
    }

    @Override
    public List<Film> getPopularFilms(Long count) {
        Comparator<Film> filmComparator = Comparator.comparing(film -> film.getLikes().size());

        if (count <= 0) {
            log.error("Число отображаемых фильмов count не может быть меньше, либо равно 0");
            throw new RuntimeException("Число отображаемых фильмов count не может быть меньше, либо равно 0");
            // или IllegalArgumentException?
        }

        return findAll()
                .stream()
                .sorted(filmComparator.reversed())
                .limit(count)
                .toList();
    }

    @Override
    public Film create(Film film) {
        return filmStorage.create(film);
    }

    @Override
    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    @Override
    public Film update(Film film) {
        return filmStorage.update(film);
    }
}
