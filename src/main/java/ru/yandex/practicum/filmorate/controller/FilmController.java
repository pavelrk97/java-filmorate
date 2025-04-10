package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmCrudService;
import ru.yandex.practicum.filmorate.service.FilmLikeService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmCrudService filmCrudService;
    private final FilmLikeService filmLikeService;

    public FilmController(@Qualifier("dbFilmCrudService") FilmCrudService filmCrudService,
                          @Qualifier("dbFilmLikeService") FilmLikeService filmLikeService) {
        this.filmCrudService = filmCrudService;
        this.filmLikeService = filmLikeService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmCrudService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        return filmCrudService.findById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmCrudService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmCrudService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmLikeService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmLikeService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Long count) {
        return filmLikeService.getPopularFilms(count);
    }
}
