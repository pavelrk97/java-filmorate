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
public class InMemoryFilmService {

    private final FilmStorage filmStorage; // сервисы зависят от интерфейсов классов-хранилищ
    private final InMemoryUserService userService;
    private String notFound = " не найден";

    public InMemoryFilmService(FilmStorage filmStorage, InMemoryUserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    private Optional<Film> findFilm(long id) {
        return filmStorage.findById(id);
    }

    private Optional<User> findUser(long id) {
        return userService.findById(id);
    }


    public Film addLike(long filmId, long userId) {
        String notFound = " не найден";

        // Получаем фильм или выбрасываем исключение
        Film film = findFilm(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с id = {} не найден", filmId);
                    return new NotFoundException("Фильм с id = " + filmId + notFound);
                });

        // Получаем пользователя или выбрасываем исключение
        User user = findUser(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id = {} не найден", userId);
                    return new NotFoundException("Пользователь с id = " + userId + notFound);
                });

        // Добавляем лайк
        film.getLikes().add(userId);
        log.info("Пользователь с id = {} поставил лайк фильму с id = {}", userId, filmId);

        return film;
    }

    public Film removeLike(long filmId, long userId) {
        String notFound = " не найден";

        // Получаем фильм или выбрасываем исключение
        Film film = findFilm(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с id = {} не найден", filmId);
                    return new NotFoundException("Фильм с id = " + filmId + notFound);
                });

        // Получаем пользователя или выбрасываем исключение
        User user = findUser(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с id = {} не найден", userId);
                    return new NotFoundException("Пользователь с id = " + userId + notFound);
                });

        // Удаляем лайк
        if (film.getLikes().remove(userId)) {
            log.info("Пользователь с id = {} удалил лайк фильму с id = {}", userId, filmId);
        } else {
            log.warn("Попытка удалить лайк, который не был поставлен. Пользователь с id = {} для фильма с id = {}", userId, filmId);
        }

        return film;
    }


    public List<Film> getPopularFilms(Long count) {
        Comparator<Film> filmComparator = Comparator.comparing(film -> film.getLikes().size());

        if (count <= 0) {
            log.error("Число отображаемых фильмов count не может быть меньше, либо равно 0");
            throw new IllegalArgumentException("Число отображаемых фильмов count не может быть меньше, либо равно 0");
        }

        return findAll()
                .stream()
                .sorted(filmComparator.reversed())
                .limit(count)
                .toList();
    }

    public Film create(Film film) {
        log.info("Попытка добавить новый фильм: {}", film);
        Film createdFilm = filmStorage.create(film);

        if (createdFilm != null) {
            log.info("Фильм успешно добавлен: {}", createdFilm);
        } else {
            log.error("Ошибка при добавлении фильма: {}", film);
        }

        return createdFilm;
    }

    public Collection<Film> findAll() {
        log.info("Запрос списка всех фильмов");
        Collection<Film> films = filmStorage.findAll();

        if (films != null) {
            log.info("Получено {} фильмов", films.size());
        } else {
            log.warn("Не удалось получить фильмы, результат null");
        }

        return films;
    }

    public Film update(Film film) {
        log.info("Попытка обновить фильм с id = {}", film.getId());
        Film updatedFilm = filmStorage.update(film);

        if (updatedFilm != null) {
            log.info("Фильм успешно обновлен: {}", updatedFilm);
        } else {
            log.error("Не удалось обновить фильм с id = {}", film.getId());
        }

        return updatedFilm;
    }
}
