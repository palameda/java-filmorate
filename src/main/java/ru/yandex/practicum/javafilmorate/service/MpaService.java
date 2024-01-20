package ru.yandex.practicum.javafilmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.javafilmorate.model.Mpa;
import ru.yandex.practicum.javafilmorate.storage.dao.MpaStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public Mpa getById(int mpaId) {
        log.info("Отправлен запрос к хранилищу на получение рейтинга с id {}", mpaId);
        return mpaStorage.findById(mpaId);
    }

    public List<Mpa> getAll() {
        log.info("Отправлен запрос к хранилищу на получение списка рейтнгов");
        return mpaStorage.findAll();
    }
}
