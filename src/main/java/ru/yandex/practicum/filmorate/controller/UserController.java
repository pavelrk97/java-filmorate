package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendshipService;
import ru.yandex.practicum.filmorate.service.UserCrudService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserCrudService userCrudService;
    private final FriendshipService friendshipService;


    public UserController(@Qualifier("dbUserCrudService") UserCrudService userCrudService,
                          @Qualifier("dbFriendshipService") FriendshipService friendshipService) {
        this.userCrudService = userCrudService;
        this.friendshipService = friendshipService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userCrudService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return userCrudService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userCrudService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return userCrudService.update(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        return friendshipService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        return friendshipService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Long id) {
        return friendshipService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return friendshipService.getCommonFriends(id, otherId);
    }
}
