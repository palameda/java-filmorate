package ru.yandex.practicum.javafilmorate.storage.dao.implementation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.javafilmorate.model.User;
import ru.yandex.practicum.javafilmorate.storage.dao.UserStorage;
import ru.yandex.practicum.javafilmorate.utils.UnregisteredDataException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User addUser(User user) {
        if (user == null) {
            throw new UnregisteredDataException("При добавлении пользователя был передан null");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Cоздание пользователя с id {}", user.getId());
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("USER_ID");
        user.setId(simpleJdbcInsert.executeAndReturnKey(user.userRowMap()).intValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new UnregisteredDataException("При обновлении данных передан пользователь без id");
        }
        log.info("Обновление пользователя с id {}", user.getId());
        if (findById(user.getId()) != null) {
            String sqlQuery = "UPDATE USERS SET USER_EMAIL = ?, USER_LOGIN = ?, USER_NAME = ?, USER_BIRTHDAY = ? " +
                    "WHERE USER_ID = ?";
            jdbcTemplate.update(sqlQuery,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    user.getId());
            return user;
        } else {
            throw new UnregisteredDataException("Пользователь с id " + user.getId() + " не зарегистрирован в системе");
        }
    }

    @Override
    public User deleteUser(User user) {
        if (user == null) {
            throw new UnregisteredDataException("При удалении пользователя был передан null");
        }
        log.info("Удаление пользователя с id {}", user.getId());
        if (findById(user.getId()) != null) {
            String sqlQuery = "DELETE FROM USERS WHERE USER_ID = ?";
            jdbcTemplate.update(sqlQuery, user.getId());
        }
        return user;
    }

    @Override
    public User findById(Integer userId) {
        if (userId == null) {
            throw new UnregisteredDataException("При поиске пользователя был передан id равный null");
        }
        User user;
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM USERS WHERE USER_ID = ?", userId);
        if (rs.first()) {
            user = new User(
                    rs.getInt("USER_ID"),
                    rs.getString("email"),
                    rs.getString("login"),
                    rs.getString("name"),
                    rs.getDate("birthday").toLocalDate(),
                    null
            );
        } else {
            throw new UnregisteredDataException("Пользователь с id " + userId + " не зарегистрирован в системе");
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        log.info("Получение списка всех пользователей");
        String sqlQuery = "SELECT * FROM USERS";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> new User(
                rs.getInt("USER_ID"),
                rs.getString("USER_EMAIL"),
                rs.getString("USER_LOGIN"),
                rs.getString("USER_NAME"),
                rs.getDate("USER_BIRTHDAY").toLocalDate(),
                null)
        );
    }
}