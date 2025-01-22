package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// запуск теста на случайном порту
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldNotCreateFilmWithEmptyName() {
        Film film = Film.builder()
                .name("")
                .description("Description")
                .releaseDate(LocalDate.of(2001, 9, 11))
                .duration(90)
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotCreateFilmWithLongDescription() {
        Film film = Film.builder()
                .name("Name")
                .description(".".repeat(201)) // 201 символ
                .releaseDate(LocalDate.of(2001, 9, 11))
                .duration(90)
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotCreateFilmWithNullReleaseDate() {
        Film film = Film.builder()
                .name("Name")
                .description("Description")
                .releaseDate(null) // Дата выпуска не указана
                .duration(90)
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotCreateFilmWithNegativeDuration() {
        Film film = Film.builder()
                .name("Name")
                .description("Description")
                .releaseDate(LocalDate.of(2001, 9, 11))
                .duration(-90) // Отрицательная длительность
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotCreateFilmWithReleaseDateBefore1895() {
        Film film = Film.builder()
                .name("Name")
                .description("Description")
                .releaseDate(LocalDate.of(1895, 12, 27)) // Релиз до 1895 года
                .duration(90)
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity("/films", film, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
