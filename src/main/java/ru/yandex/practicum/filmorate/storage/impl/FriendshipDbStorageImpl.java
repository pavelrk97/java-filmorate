package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendshipDbStorageImpl implements FriendshipStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final UserRowMapper userRowMapper;

    @Override
    public User addFriend(Long user1Id, Long user2Id) {

        if (Objects.equals(user1Id, user2Id)) {
            log.error("Нельзя добавить в друзья самого себя");
            throw new ValidationException("Нельзя добавить в друзья самого себя");
        }

        Optional<User> mainUser = userStorage.findById(user1Id);
        Optional<User> friendUser = userStorage.findById(user2Id);

        if (mainUser.isPresent() && friendUser.isPresent()) {

            String sqlQueryUser2 = "SELECT user2_id " +
                    "FROM friendship WHERE user1_id = ?";

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

            mainUser.get().getFriends().add(friendUser.get());

            log.info("Пользователь с id = {} добавил в друзья пользователя с id = {}", user1Id, user2Id);
            return mainUser.get();

        } else if (mainUser.isEmpty()) {
            log.error("Пользователь с id = {} не найден", user1Id);
            throw new NotFoundException("Пользователь с id = " + user1Id + " не найден");

        } else {
            log.error("Пользователь с id = {} не найден", user2Id);
            throw new NotFoundException("Пользователь с id = " + user2Id + " не найден");
        }
    }

    @Override
    public User removeFriend(Long mainUserId, Long friendUserId) {
        log.info("Удаление из друзей");
        if (Objects.equals(mainUserId, friendUserId)) {
            log.error("Нельзя удалить из друзей самого себя");
            throw new ValidationException("Нельзя удалить из друзей самого себя");
        }

        Optional<User> mainUser = userStorage.findById(mainUserId);
        Optional<User> friendUser = userStorage.findById(friendUserId);

        if (mainUser.isPresent() && friendUser.isPresent()) {

            String sqlDeleteFriend = "DELETE FROM friendship WHERE user1_id = ? AND user2_id = ?";

            int deletedRows = jdbcTemplate.update(sqlDeleteFriend, mainUserId, friendUserId);
            log.info("Удалено {} строк", deletedRows);

            log.info("Пользователь с id = {} удалил из друзей пользователя с id = {}", mainUserId, friendUserId);
            return mainUser.get();

        } else if (mainUser.isEmpty()) {
            log.error("Пользователь с id = {} не найден", mainUserId);
            throw new NotFoundException("Пользователь с id = " + mainUserId + " не найден");

        } else {
            log.error("Пользователь с id = {} не найден", friendUserId);
            throw new NotFoundException("Пользователь с id = " + friendUserId + " не найден");
        }
    }

    @Override
    public Collection<User> getCommonFriends(Long firstUserId, Long secondUserId) {
        String sqlCommonFriends = "SELECT id, email, login, name, birthday FROM users " +
                "JOIN friendship AS fri ON users.id = fri.user2_id " +
                "JOIN friendship AS fri2 ON users.id = fri2.user2_id " +
                "WHERE fri.user1_id = ? AND fri2.user1_id = ? ";

        if (userStorage.findById(firstUserId).isPresent() && userStorage.findById(secondUserId).isPresent()) {

            return jdbcTemplate.query(sqlCommonFriends, userRowMapper::mapRowToUser, firstUserId, secondUserId);

        } else if (userStorage.findById(firstUserId).isEmpty()) {
            log.error("Пользователь с id = {} не найден", firstUserId);
            throw new NotFoundException("Пользователь с id = " + firstUserId + " не найден");

        } else {
            log.error("Пользователь с id = {} не найден", secondUserId);
            throw new NotFoundException("Пользователь с id = " + secondUserId + " не найден");
        }
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        String sqlQueryUser2 = "SELECT user2_id " +
                "FROM friendship WHERE user1_id = ?";

        List<Long> friendsId = jdbcTemplate.queryForList(sqlQueryUser2, Long.class, userId);
        List<User> allUsers = userStorage.findAll().stream().toList();
        List<User> result = new ArrayList<>();

        for (User user : allUsers) {
            if (friendsId.contains(user.getId())) {
                result.add(user);
            }
        }

        return result;
    }
}
