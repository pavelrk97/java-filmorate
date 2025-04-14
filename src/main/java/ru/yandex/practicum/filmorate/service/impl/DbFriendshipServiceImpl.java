package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserCrudService;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.util.Collection;

@Service("dbFriendshipService")
@RequiredArgsConstructor
public class DbFriendshipServiceImpl implements FriendshipService {

    private final FriendshipStorage friendshipStorage;
    private final UserCrudService userCrudService;

    @Override
    public Collection<User> getFriends(Long userId) {
        userCrudService.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        return friendshipStorage.getFriends(userId);
    }


    @Override
    public Collection<User> getCommonFriends(Long firstUserId, Long secondUserId) {
        return friendshipStorage.getCommonFriends(firstUserId, secondUserId);
    }

    @Override
    public User addFriend(Long mainUserId, Long friendUserId) {
        return friendshipStorage.addFriend(mainUserId, friendUserId);
    }

    @Override
    public User removeFriend(Long mainUserId, Long friendUserId) {
        return friendshipStorage.removeFriend(mainUserId, friendUserId);
    }
}
