package ru.yandex.practicum.javafilmorate.integrationTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.javafilmorate.JavaFilmorateApplication;
import ru.yandex.practicum.javafilmorate.model.*;
import ru.yandex.practicum.javafilmorate.storage.dao.DirectorStorage;
import ru.yandex.practicum.javafilmorate.storage.dao.implementation.*;

import java.time.LocalDate;
import java.util.*;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@SpringBootTest(classes = JavaFilmorateApplication.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final LikesDbStorage likesDbStorage;
    private final UserDbStorage userDbStorage;
    private final DirectorStorage directorStorage;
    private final Film film1 = new Film(null, "Film1", "Description1", LocalDate.parse("1970-01-01"),
            140, new Mpa(1, "G"), 0);
    private final Film film2 = new Film(null, "Film2", "Description2", LocalDate.parse("1980-01-01"),
            90, new Mpa(2, "PG"), 0);
    private final Film film3 = new Film(null, "Film3", "Description3", LocalDate.parse("1990-01-01"),
            190, new Mpa(2, "PG"), 0);
    private final User firstUser = new User(1, "email@yandex.ru", "Login1", "Name1", LocalDate.parse("1970-01-01"), null);
    private final User secondUser = new User(1, "email@gmail.com", "Login2", "Name2", LocalDate.parse("1980-01-01"), null);
    private final User thirdUser = new User(3, "email@gmail.com", "Login3", "Name3", LocalDate.parse("1990-01-01"), null);
    private final Director director = new Director(1, "DirectorName");
    private int film1Id, film2Id, film3Id;
    private int user1Id, user2Id, user3Id;

    @BeforeEach
    void createFilmData() {
        filmStorage.addFilm(film1);
        film1Id = film1.getId();
        filmStorage.addFilm(film2);
        film2Id = film2.getId();
        filmStorage.addFilm(film3);
        film3Id = film3.getId();

        userDbStorage.addUser(firstUser);
        user1Id = firstUser.getId();
        userDbStorage.addUser(secondUser);
        user2Id = secondUser.getId();
        userDbStorage.addUser(thirdUser);
        user3Id = thirdUser.getId();
    }

    @Test
    @DisplayName("Проверка метода update для Film")
    void testUpdateFilm() {
        Film updateFilm = new Film(1, "Film1", "updateDescription", LocalDate.parse("1990-01-01"), 140, new Mpa(1, "G"), 0);
        filmStorage.updateFilm(updateFilm);
        Film afterUpdate = filmStorage.findById(1);
        Assertions.assertEquals(afterUpdate.getDescription(), "updateDescription");
    }

    @Test
    @DisplayName("Проверка метода findById для Film")
    void testFindFilmById() {
        Film film = filmStorage.findById(1);
        Assertions.assertEquals(film.getId(), 1);
    }

    @Test
    @DisplayName("Проверка метода findAll() для Film")
    void testFindAll() {
        List<Film> current = filmStorage.findAll();
        Assertions.assertEquals(3, current.size(), "Количество фильмов не совпадает");
    }

    @Test
    @DisplayName("Проверка метода commonFilms для Film")
    void testCommonFilms() {
        likesDbStorage.addLike(film1Id, user1Id);
        likesDbStorage.addLike(film2Id, user1Id);
        likesDbStorage.addLike(film3Id, user1Id);

        likesDbStorage.addLike(film1Id, user2Id);
        likesDbStorage.addLike(film2Id, user2Id);

        likesDbStorage.addLike(film2Id, user3Id);

        /*Проверяем размер полученного списка*/
        List<Film> current = filmStorage.commonFilms(user1Id, user2Id);
        Assertions.assertEquals(2, current.size(), "Количество фильмов не совпадает.");

        /*Проверяем порядок элементов в списке.*/
        /*Первым должен быть фильм с id=2 т.к. у него три лайка*/
        Assertions.assertEquals(2, current.get(0).getId(), "Первым д.б. фильм с id=2 т.к. у него три лайка.");
        Assertions.assertEquals(1, current.get(1).getId(), "Вторым д.б. фильм с id=1 т.к. у него два лайка.");
    }

    @Test
    @DisplayName("Проверка метода deleteFilm")
    void testDeleteFilm() {
        filmStorage.deleteFilm(2);

        List<Film> current = filmStorage.findAll();
        Assertions.assertEquals(2, current.size(), "Количество film не совпадает.");

        Film[] expect = new Film[]{film1, film3};
        Assertions.assertArrayEquals(expect, current.toArray(), "Удален не тот film.");
    }

    @Test
    @DisplayName("Получение фильмов режиссёра, отсортированных по году")
    void testShouldFindDirectorFilmsByYear() {
        Director director2 = new Director(2, "DirectorName2");
        directorStorage.addDirector(director);
        directorStorage.addDirector(director2);
        //для каждого фильма указан режиссёр
        film1.getDirectors().add(director);
        filmStorage.updateFilm(film1);
        film2.getDirectors().add(director);
        filmStorage.updateFilm(film2);
        film3.getDirectors().add(director2);
        filmStorage.updateFilm(film3);
        //получение списка фильмов, отсортированного по году
        List<Film> filmsByYear = filmStorage.findDirectorFilmsByYearOrLikes(director.getId(), "year");
        Assertions.assertEquals(filmsByYear.size(), 2, "Количество фильмов не совпадает");
        Assertions.assertEquals(filmsByYear.get(0).getId(), film1.getId(), "Фильмы не отсортированы");
        Assertions.assertEquals(filmsByYear.get(1).getId(), film2.getId(), "Фильмы не отсортированы");
        Assertions.assertEquals(filmsByYear.get(0).getReleaseDate().toString(), "1970-01-01", "Даты не совпадают");
    }

    @Test
    @DisplayName("Получение фильмов режиссёра, отсортированных по лайкам")
    void testShouldFindDirectorFilmsByLikes() {
        Director director2 = new Director(2, "DirectorName2");
        directorStorage.addDirector(director);
        directorStorage.addDirector(director2);
        //для каждого фильма указан режиссёр
        film1.getDirectors().add(director);
        filmStorage.updateFilm(film1);
        film2.getDirectors().add(director);
        filmStorage.updateFilm(film2);
        film3.getDirectors().add(director2);
        filmStorage.updateFilm(film3);
        //пользователи проставляют лайки
        likesDbStorage.addLike(firstUser.getId(), film1.getId());
        likesDbStorage.addLike(firstUser.getId(), film2.getId());
        likesDbStorage.addLike(firstUser.getId(), film3.getId());
        likesDbStorage.addLike(secondUser.getId(), film2.getId());
        likesDbStorage.addLike(secondUser.getId(), film3.getId());
        likesDbStorage.addLike(thirdUser.getId(), film3.getId());
        //получение списка фильмов, отсортированного по лайкам
        List<Film> filmsByLikes = filmStorage.findDirectorFilmsByYearOrLikes(director.getId(), "likes");
        Assertions.assertEquals(filmsByLikes.size(), 2, "Количество фильмов не совпадает");
        Assertions.assertEquals(filmsByLikes.get(0).getId(), film1.getId(), "Фильмы не отсортированы");
        Assertions.assertEquals(filmsByLikes.get(1).getId(), film2.getId(), "Фильмы не отсортированы");
    }

    @Test
    @DisplayName("Проверка метода getPopularByGenre")
    void testGetPopularByGenre() {
        // Добавляем жанры всем фильмам
        Film film3 = filmStorage.findById(film3Id);
        film3.setGenres(Set.of(new Genre(1, "Комедия")));
        filmStorage.updateFilm(film3);

        Film film2 = filmStorage.findById(film2Id);
        film2.setGenres(Set.of(new Genre(1, "Комедия")));
        filmStorage.updateFilm(film2);

        Film film1 = filmStorage.findById(film1Id);
        film1.setGenres(Set.of(new Genre(2, "Драма")));
        filmStorage.updateFilm(film1);

        likesDbStorage.addLike(film1Id, user1Id);
        likesDbStorage.addLike(film1Id, user2Id);
        likesDbStorage.addLike(film2Id, user1Id);
        likesDbStorage.addLike(film2Id, user2Id);
        likesDbStorage.addLike(film3Id, user1Id);
        likesDbStorage.addLike(film3Id, user2Id);
        likesDbStorage.addLike(film3Id, user3Id);

        Assertions.assertEquals(List.of(filmStorage.findById(film3Id)), filmStorage.getPopularByGenre(1, 1),
                "Должен выдать только filmId3");
        Assertions.assertEquals(List.of(filmStorage.findById(film3Id), filmStorage.findById(film2Id)),
                filmStorage.getPopularByGenre(10, 1), "Должен выдать фильмы filmId3, filmId2, " +
                        "в этом порядке");
        Assertions.assertEquals(List.of(filmStorage.findById(1)),
                filmStorage.getPopularByGenre(10, 2), "Должен выдать filmId1");
    }

    @Test
    @DisplayName(("Проверка метода getPopularByYear"))
    void testGetPopularByYear() {
        Film film4 = new Film(null, "Film4", "Description4", LocalDate.parse("1990-10-01"),
                140, new Mpa(1, "G"), 0);
        film4.setGenres(Set.of(new Genre(2, "Драма")));
        filmStorage.addFilm(film4);
        int film4Id = film4.getId();

        likesDbStorage.addLike(film1Id, user1Id);
        likesDbStorage.addLike(film1Id, user2Id);
        likesDbStorage.addLike(film2Id, user1Id);
        likesDbStorage.addLike(film2Id, user2Id);
        likesDbStorage.addLike(film4Id, user1Id);
        likesDbStorage.addLike(film4Id, user2Id);
        likesDbStorage.addLike(film4Id, user3Id);

        Assertions.assertEquals(List.of(filmStorage.findById(film4Id)), filmStorage.getPopularByYear(1, 1990),
                "Должен выдать только фильм film4Id");
        Assertions.assertEquals(List.of(filmStorage.findById(film4Id), filmStorage.findById(film3Id)),
                filmStorage.getPopularByYear(10, 1990), "Должен выдать фильмы film4Id и film3Id");
        Assertions.assertEquals(List.of(filmStorage.findById(1)),
                filmStorage.getPopularByYear(10, 1970), "Должен выдать filmId1");
    }

    @Test
    @DisplayName("Проверка метода getPopularByGenreAndYear")
    void testGetPopularByGenreAndYear() {
        Film film4 = new Film(null, "Film4", "Description4", LocalDate.parse("1990-10-01"),
                140, new Mpa(1, "G"), 0);
        film4.setGenres(Set.of(new Genre(2, "Драма")));
        filmStorage.addFilm(film4);
        int film4Id = film4.getId();

        Film film5 = new Film(null, "Film5", "Description5", LocalDate.parse("1990-10-10"),
                140, new Mpa(1, "G"), 0);
        film5.setGenres(Set.of(new Genre(2, "Драма")));
        filmStorage.addFilm(film5);
        int film5Id = film5.getId();

        Film film3 = filmStorage.findById(film3Id);
        film3.setGenres(Set.of(new Genre(1, "Комедия")));
        filmStorage.updateFilm(film3);

        likesDbStorage.addLike(film3Id, user1Id);
        likesDbStorage.addLike(film4Id, user1Id);
        likesDbStorage.addLike(film4Id, user2Id);
        likesDbStorage.addLike(film5Id, user1Id);
        likesDbStorage.addLike(film5Id, user2Id);
        likesDbStorage.addLike(film5Id, user3Id);

        Assertions.assertEquals(List.of(filmStorage.findById(film5Id)),
                filmStorage.getPopularByGenreAndYear(1, 2, 1990),
                "в списке должен быть только film5");
        Assertions.assertEquals(List.of(filmStorage.findById(film5Id), filmStorage.findById(film4Id)),
                filmStorage.getPopularByGenreAndYear(10, 2, 1990),
                "в списке должны быть фильмы film5 и film4");
    }

    @Test
    @DisplayName("Проверка метода searchBySubstring")
    void testSearchBySubstring() {
        Director director2 = new Director(2, "FiLmDiReCtOr");
        directorStorage.addDirector(director);
        directorStorage.addDirector(director2);

        Film film34 = new Film(null, "gOlm34", "Description4", LocalDate.parse("1990-10-01"),
                140, new Mpa(1, "G"), 0);
        film34.getDirectors().add(director2);
        filmStorage.addFilm(film34);
        int film34Id = film34.getId();
        //для каждого фильма указан режиссёр
        film1.getDirectors().add(director);
        filmStorage.updateFilm(film1);
        film2.getDirectors().add(director);
        filmStorage.updateFilm(film2);
        film3.getDirectors().add(director2);
        filmStorage.updateFilm(film3);
        // Добавляем лайки, так как фильмы сортируются по популярности
        likesDbStorage.addLike(film2Id, user1Id);
        likesDbStorage.addLike(film2Id, user2Id);
        likesDbStorage.addLike(film3Id, user1Id);
        likesDbStorage.addLike(film3Id, user2Id);
        likesDbStorage.addLike(film3Id, user3Id);

        Assertions.assertEquals(List.of(filmStorage.findById(film1Id)),
                filmStorage.searchBySubstring("lm1", "title"), "Должен выдавать только film1");
        Assertions.assertEquals(List.of(filmStorage.findById(film3Id), filmStorage.findById(film34Id)),
                filmStorage.searchBySubstring("lm3", "title"), "Должен выдавать film3, film34");
        Assertions.assertEquals(List.of(filmStorage.findById(film2Id), filmStorage.findById(film1Id)),
                filmStorage.searchBySubstring("torn", "director"), "Должен выдавать film2, film1");
        Assertions.assertEquals(List.of(filmStorage.findById(film3Id), filmStorage.findById(film2Id),
                filmStorage.findById(1), filmStorage.findById(film34Id)),
                filmStorage.searchBySubstring("ilm", "title,director"), "Должен выдать фильма 3, 2, 1, 4");
        Assertions.assertEquals(List.of(filmStorage.findById(film3Id), filmStorage.findById(film2Id),
                        filmStorage.findById(1), filmStorage.findById(film34Id)),
                filmStorage.searchBySubstring("ilm", "director,title"), "Порядок параметров в by не" +
                        "имеет значения");
    }
}