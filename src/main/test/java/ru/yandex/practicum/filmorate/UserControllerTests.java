package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldNotCreateUserWithEmptyRequest() {
        ResponseEntity<String> response = restTemplate.postForEntity("/users", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void shouldNotCreateUserWithEmptyEmail() {
        User user = User.builder()
                .email("")
                .login("Login")
                .name("Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity("/users", user, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotCreateUserWithInvalidEmail() {
        User user = User.builder()
                .email("invalid-email")
                .login("Login")
                .name("Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity("/users", user, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldNotCreateUserWithEmptyLogin() {
        User user = User.builder()
                .email("email@test.com")
                .login("")
                .name("Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity("/users", user, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldCreateUserWithoutName() {
        User user = User.builder()
                .email("email@test.com")
                .login("Login")
                .name(null)
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(user.getLogin());
    }

    @Test
    void shouldNotCreateUserWithFutureBirthday() {
        User user = User.builder()
                .email("email@test.com")
                .login("Login")
                .name("Name")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity("/users", user, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldCreateValidUser() {
        User user = User.builder()
                .email("email@test.com")
                .login("Login")
                .name("Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getBody().getLogin()).isEqualTo(user.getLogin());
        assertThat(response.getBody().getName()).isEqualTo(user.getName());
        assertThat(response.getBody().getBirthday()).isEqualTo(user.getBirthday());
    }

    @Test
    void shouldNotUpdateNonexistentUser() {
        User user = User.builder()
                .id(999L)
                .email("email@test.com")
                .login("Login")
                .name("Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<User> requestEntity = new HttpEntity<>(user, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                requestEntity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateExistingUser() {
        User user = User.builder()
                .email("email@test.com")
                .login("Login")
                .name("Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ResponseEntity<User> createResponse = restTemplate.postForEntity("/users", user, User.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        User updatedUser = createResponse.getBody().toBuilder()
                .name("Updated Name")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<User> requestEntity = new HttpEntity<>(updatedUser, headers);

        ResponseEntity<User> updateResponse = restTemplate.exchange(
                "/users",
                HttpMethod.PUT,
                requestEntity,
                User.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getName()).isEqualTo("Updated Name");
    }
}
