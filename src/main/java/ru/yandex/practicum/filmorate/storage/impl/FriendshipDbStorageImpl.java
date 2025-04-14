package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendshipDbStorageImpl implements FriendshipStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User addFriend(Long user1Id, Long user2Id) {
        if (Objects.equals(user1Id, user2Id)) {
            log.error("Нельзя добавить в друзья самого себя");
            throw new ValidationException("Нельзя добавить в друзья самого себя");
        }

        User mainUser = findById(user1Id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + user1Id + " не найден"));
        User friendUser = findById(user2Id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + user2Id + " не найден"));

        String sqlQueryUser2 = "SELECT user2_id FROM friendship WHERE user1_id = ?";
        String sqlQueryAddFriend = "INSERT INTO friendship(user1_id, user2_id) values (?, ?)";

        List<Long> user2Ids = jdbcTemplate.queryForList(sqlQueryUser2, Long.class, user1Id);

        if (!user2Ids.contains(user2Id)) {
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sqlQueryAddFriend);
                stmt.setLong(1, user1Id);
                stmt.setLong(2, user2Id);
                return stmt;
            });
        }

        mainUser.getFriends().add(friendUser);
        log.info("Пользователь с id = {} добавил в друзья пользователя с id = {}", user1Id, user2Id);
        return mainUser;
    }

    @Override
    public User removeFriend(Long mainUserId, Long friendUserId) {
        log.info("Удаление из друзей");
        if (Objects.equals(mainUserId, friendUserId)) {
            log.error("Нельзя удалить из друзей самого себя");
            throw new ValidationException("Нельзя удалить из друзей самого себя");
        }

        User mainUser = findById(mainUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + mainUserId + " не найден"));
        User friendUser = findById(friendUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + friendUserId + " не найден"));

        String sqlDeleteFriend = "DELETE FROM friendship WHERE user1_id = ? AND user2_id = ?";
        int deletedRows = jdbcTemplate.update(sqlDeleteFriend, mainUserId, friendUserId);
        log.info("Удалено {} строк", deletedRows);
        log.info("Пользователь с id = {} удалил из друзей пользователя с id = {}", mainUserId, friendUserId);

        return mainUser;
    }

    @Override
    public Collection<User> getCommonFriends(Long firstUserId, Long secondUserId) {
        findById(firstUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + firstUserId + " не найден"));
        findById(secondUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + secondUserId + " не найден"));

        String sqlCommonFriends = """
            SELECT id, email, login, name, birthday
            FROM users
            JOIN friendship AS fri ON users.id = fri.user2_id
            JOIN friendship AS fri2 ON users.id = fri2.user2_id
            WHERE fri.user1_id = ? AND fri2.user1_id = ?
            """;

        return jdbcTemplate.query(sqlCommonFriends, userRowMapper::mapRowToUser, firstUserId, secondUserId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        String sqlQueryUser2 = "SELECT user2_id " +
                "FROM friendship WHERE user1_id = ?";

        List<Long> friendsId = jdbcTemplate.queryForList(sqlQueryUser2, Long.class, userId);
        List<User> allUsers = findAll().stream().toList();
        List<User> result = new ArrayList<>();

        for (User user : allUsers) {
            if (friendsId.contains(user.getId())) {
                result.add(user);
            }
        }

        return result;
    }

    public Optional<User> findById(Long id) {
        Optional<User> resultUser;

        String sqlQuery = "SELECT id, email, login, name, birthday " +
                "from users where id = ?";

        try {
            resultUser = Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery,
                    userRowMapper::mapRowToUser, id));
        } catch (EmptyResultDataAccessException e) {
            resultUser = Optional.empty();
        }

        if (resultUser.isPresent()) {
            return resultUser;

        } else {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

    public Collection<User> findAll() {
        String sqlQuery = "SELECT id, email, login, name, birthday from users";
        return jdbcTemplate.query(sqlQuery, userRowMapper::mapRowToUser);
    }
}
