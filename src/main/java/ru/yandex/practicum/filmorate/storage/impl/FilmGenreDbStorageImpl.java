package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.GenreRowMappers;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmGenreStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component("filmGenreDbStorage")
@RequiredArgsConstructor
public class FilmGenreDbStorageImpl implements FilmGenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMappers genreRowMappers;

    @Override
    public Collection<Genre> findAll() {
        final String sqlQuery = "SELECT * FROM film_genre";
        return jdbcTemplate.query(sqlQuery, genreRowMappers::mapRowToGenre);
    }

    @Override
    public Collection<Genre> findByFilmId(Long id) {
        final String sqlQuery = "SELECT genre_id FROM film_genre WHERE film_id = ? ";
        return jdbcTemplate.query(sqlQuery, genreRowMappers::mapRowToGenre, id);
    }

    @Override
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

    public List<Genre> getListGenreFromDbGenres(Long filmId) {
        List<Genre> result = new ArrayList<>();

        String filmGenresQuery = "SELECT genre_id, " +
                "FROM film_genre " +
                "WHERE film_id = ? ";

        List<Long> genreIds = jdbcTemplate.queryForList(filmGenresQuery, Long.class, filmId);
        List<Genre> genres = findAllGenres().stream().toList();

        for (Genre genre : genres) {
            if (genreIds.contains(genre.getId())) {
                result.add(genre);
            }
        }

        return result;
    }

    // Дублируем GenreDbStorageImpl
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

    public Collection<Genre> findAllGenres() {
        String sqlQuery = "SELECT * from genres";
        return jdbcTemplate.query(sqlQuery, genreRowMappers::mapRowToGenre);
    }
}
