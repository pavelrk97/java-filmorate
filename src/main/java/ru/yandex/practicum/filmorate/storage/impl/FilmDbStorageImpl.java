package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmRowMappers;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorageImpl implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMappers filmRowMappers;

    @Override
    public Collection<Film> findAll() {
        log.info("Выгрузка всех фильмов");
        final String sqlQuery = "SELECT id, name, description, releaseDate, duration, mpa_id FROM films";
        return jdbcTemplate.query(sqlQuery, filmRowMappers::mapRowToFilm);
    }

    @Override
    public Film findById(Long id) {
        log.info("Поиск фильма по id = {}", id);

        final String sqlQuery = "SELECT id, name, description, releaseDate, duration, mpa_id " +
                "FROM films WHERE id = ?";

        // Извлекаем фильм из базы данных
        Film resultFilm = jdbcTemplate.queryForObject(sqlQuery, filmRowMappers::mapRowToFilm, id);

        // Если результат null, выбрасываем исключение
        return Optional.ofNullable(resultFilm)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    @Override
    public Film create(Film film) {
        log.info("Добавление нового фильма: {}", film.getName());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        final long filmId;

        final String sqlQueryFilm = "INSERT INTO films(name, description, releaseDate, duration, mpa_id) " +
                "values (?, ?, ?, ?, ?)";

        // проверяем существование рейтинга в таблице mpa
        getCountById(film);

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQueryFilm, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setString(3, film.getReleaseDate().toString());
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        if (Objects.nonNull(keyHolder.getKey())) {
            filmId = keyHolder.getKey().longValue();
        } else {
            throw new ValidationException("Ошибка добавления фильма в таблицу");
        }

        // кладём жанры фильма в таблицу film_genre
        addGenresInFilmGenres(film, filmId);

        List<Genre> resultGenres = getExistGenres(film).stream().toList();

        log.info("Фильм c id = {} успешно добавлен", film.getId());
        return Film.builder()
                .id(filmId)
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpa())
                .genres(resultGenres)
                .build();
    }

    @Override
    public Film update(Film newFilm) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        final long filmId;

        log.info("Обновление данных фильма с id = {}", newFilm.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String sqlQuery = "UPDATE films SET " +
                "name = ?, description = ?, releaseDate = ?, duration = ? " +
                "where id = ?";

        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"id"});
            stmt.setString(1, newFilm.getName());
            stmt.setString(2, newFilm.getDescription());
            stmt.setString(3, newFilm.getReleaseDate().toString());
            stmt.setInt(4, newFilm.getDuration());
            stmt.setLong(5, newFilm.getId());
            return stmt;
        }, keyHolder);

        if (Objects.nonNull(keyHolder.getKey())) {
            filmId = keyHolder.getKey().longValue();
        } else {
            throw new NotFoundException("Ошибка обновления фильма");
        }

        Film resultFilm = Film.builder()
                .id(filmId)
                .name(newFilm.getName())
                .description(newFilm.getDescription())
                .releaseDate(newFilm.getReleaseDate())
                .duration(newFilm.getDuration())
                .build();

        if (rows > 0) {
            log.info("Фильм с id = {} успешно обновлён", filmId);
            return resultFilm;

        } else {
            log.error("Ошибка обновления фильма id = {}", filmId);
            throw new NotFoundException("Ошибка обновления фильма id = " + filmId);
        }
    }

    // Дублирование MpaStorage
    public Integer getCountById(Film film) {
        log.info("Проверка существования mpa_id = {} в таблице mpa", film.getMpa().getId());
        Integer count;
        final String sqlQueryMpa = "SELECT COUNT(*) " +
                "FROM mpa WHERE id = ?";

        try {
            count = jdbcTemplate.queryForObject(sqlQueryMpa, Integer.class, film.getMpa().getId());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA id не существуют");
        }

        if (Objects.isNull(count) || count == 0) {
            throw new NotFoundException("MPA id не существует");
        }

        return count;
    }

    // Дублирование GenreDbStorageImpl
    public Collection<Long> findIds() {
        String sqlQuery = "SELECT id from genres";
        return jdbcTemplate.queryForList(sqlQuery, Long.class);
    }

    public Collection<Genre> getExistGenres(Film film) {
        List<Long> genres = findIds().stream().toList();
        List<Genre> filmGenres = film.getGenres();
        List<Genre> resultGenres = new ArrayList<>();

        if (Objects.nonNull(filmGenres)) {
            filmGenres.forEach(genre -> {
                        if (genres.contains(genre.getId())) {
                            resultGenres.add(genre);
                        } else {
                            throw new NotFoundException("Указанный жанр не существует");
                        }
                    }
            );
        }
        return resultGenres;
    }

    // Дублирование FilmGenreDbStorageImpl
    public void addGenresInFilmGenres(Film film, Long newId) {

        List<Genre> resultGenres = getExistGenres(film).stream().toList();

        final String sqlQueryFilmGenres = "INSERT INTO film_genre(film_id, genre_id) " +
                "values (?, ?)";

        jdbcTemplate.batchUpdate(sqlQueryFilmGenres, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setLong(1, newId);
                preparedStatement.setLong(2, resultGenres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return resultGenres.size();
            }
        });
    }
}
