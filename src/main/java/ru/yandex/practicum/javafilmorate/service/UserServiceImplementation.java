package ru.yandex.practicum.javafilmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.javafilmorate.model.User;
import ru.yandex.practicum.javafilmorate.storage.UserStorage;
import ru.yandex.practicum.javafilmorate.utils.InvalidDataException;
import ru.yandex.practicum.javafilmorate.utils.UnregisteredDataException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImplementation implements UserService {
    private final UserStorage userStorage;

    @Override
    public User addUser(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("При добавлении пользователя передан login, содержащий пробел(ы)");
            throw new InvalidDataException("login пользователя не должен содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        userStorage.addUser(user);
        log.info("Пользователь {} ({}) успешно добавлен", user.getLogin(), user.getName());
        return user;
    }

    @Override
    public User updateUser(User user) {
        checkUser(user.getId());
        if (user.getLogin().contains(" ")) {
            log.error("При обновлении пользователя передан login, содержащий пробел(ы)");
            throw new InvalidDataException("login пользователя не должен содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        userStorage.updateUser(user);
        log.info("Данные пользователя {} ({}) успешно обновлены", user.getLogin(), user.getName());
        return user;
    }

    @Override
    public void deleteUser(User user) {
        checkUser(user.getId());
        log.info("Пользователь {} ({}) успешно удалён", user.getLogin(), user.getName());
        userStorage.deleteUser(user);
    }

    @Override
    public User getUserById(int id) {
        User user = checkUser(id);
        log.info("Пользователь c id {} зарегестрирован в системе как {}", id, user.getLogin());
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = List.copyOf(userStorage.getAllUsers().values());
        log.info("В системе зарегистрировано {} пользователей", users.size());
        return users;
    }

    @Override
    public void addFriend(int selfId, int friendId) {
        User self = checkUser(selfId);
        User friend = checkUser(friendId);
        self.getFriends().add(friendId);
        friend.getFriends().add(selfId);
        log.info("Пользователи {} и {} теперь друзья", userStorage.getAllUsers().get(selfId),
                userStorage.getAllUsers().get(friendId));
    }

    @Override
    public void deleteFriend(int selfId, int friendId) {
        User self = checkUser(selfId);
        User friend = checkUser(friendId);
        self.getFriends().remove(friendId);
        friend.getFriends().remove(selfId);
        log.info("Пользователи {} и {} больше не друзья", userStorage.getAllUsers().get(selfId),
                userStorage.getAllUsers().get(friendId));
    }

    @Override
    public List<User> getUserFriends(int selfId) {
        User user = checkUser(selfId);
        List<User> friends = user.getFriends().stream()
                .map(friendId -> userStorage.getAllUsers().get(friendId))
                .collect(Collectors.toList());
        log.info("У пользователя {} {} друзей", user.getName(), friends.size());
        return friends;
    }

    @Override
    public List<User> getCommonFriends(int firstId, int secondId) {
        User firstUser = checkUser(firstId);
        User secondUser = checkUser(secondId);
        List<User> commonFriends = firstUser.getFriends().stream()
                .filter(friendId -> secondUser.getFriends().contains(friendId))
                .map(filteredId -> userStorage.getAllUsers().get(filteredId))
                .collect(Collectors.toList());
        log.info("У пользователя {} и пользователя {} {} общих друзей", firstUser.getName(), secondUser.getName(),
                commonFriends.size());
        return commonFriends;
    }

    private User checkUser(int id) {
        if (!userStorage.getAllUsers().containsKey(id)) {
            log.error("Пользователь не зарегистрирован в системе");
            throw new UnregisteredDataException("Пользователь не зарегистрирован в системе");
        }
        return userStorage.getAllUsers().get(id);
    }
}
