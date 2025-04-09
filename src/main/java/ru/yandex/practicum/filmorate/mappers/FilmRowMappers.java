package ru.yandex.practicum.filmorate.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.FilmLikeStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

// немного разнес логику
@Slf4j
@Component
@RequiredArgsConstructor
public class FilmRowMappers {

    private final MpaStorage mpaStorage;
    private final FilmGenreStorage filmGenreStorage;
    private final FilmLikeStorage filmLikeStorage;

    public Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        log.info("Старт метода mapRowToFilm");

        Long filmId = rs.getLong("id");
        Integer mpaId = rs.getInt("mpa_id");

        Film baseFilm = Film.builder()
                .id(filmId)
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(LocalDate.parse(rs.getString("releaseDate")))
                .duration(rs.getInt("duration"))
                .build();

        return baseFilm.toBuilder()
                .mpa(mpaStorage.findById(mpaId))
                .genres(filmGenreStorage.getListGenreFromDbGenres(filmId))
                .likes(filmLikeStorage.getLikesById(filmId))
                .build();
    }
}


//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class FilmRowMappers {
//
//    private final MpaStorage mpaStorage;
//    private final FilmGenreStorage filmGenreStorage;
//    private final FilmLikeStorage filmLikeStorage;
//
//    public Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
//        log.info("Старт метода Film mapRowToFilm(ResultSet resultSet, int rowNum)");
//        Integer mpaId = resultSet.getInt("mpa_id");
//        Mpa mpa = mpaStorage.findById(mpaId);
//        List<Genre> result = filmGenreStorage.getListGenreFromDbGenres(resultSet.getLong("id"));
//        log.info("Получаем количество лайков фильма по id = {}", resultSet.getLong("id"));
//        Long likes = filmLikeStorage.getLikesById(resultSet.getLong("id"));
//
//        return Film.builder()
//                .id(resultSet.getLong("id"))
//                .likes(likes)
//                .name(resultSet.getString("name"))
//                .description(resultSet.getString("description"))
//                .releaseDate(LocalDate.parse(resultSet.getString("releaseDate")))
//                .duration(resultSet.getInt("duration"))
//                .mpa(mpa)
//                .genres(result)
//                .build();
//    }
//}
