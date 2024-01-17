DELETE FROM FILM_GENRES;
DELETE FROM LIKES;
DELETE FROM GENRES;
DELETE FROM FRIENDS;
DELETE FROM USERS;
DELETE FROM FILMS;

ALTER TABLE USERS ALTER COLUMN USER_ID RESTART WITH 1;
ALTER TABLE FILMS ALTER COLUMN FILM_ID RESTART WITH 1;

MERGE INTO MPA (MPA_ID, MPA_NAME)
VALUES (1, 'G');
MERGE INTO MPA (MPA_ID, MPA_NAME)
VALUES (2, 'PG');
MERGE INTO MPA (MPA_ID, MPA_NAME)
VALUES (3, 'PG-13');
MERGE INTO MPA (MPA_ID, MPA_NAME)
VALUES (4, 'R');
MERGE INTO MPA (MPA_ID, MPA_NAME)
VALUES (5, 'NC-17');

MERGE INTO GENRES (GENRE_ID, GENRE_NAME)
VALUES (1, 'Комедия');
MERGE INTO GENRES (GENRE_ID, GENRE_NAME)
VALUES (2, 'Драма');
MERGE INTO GENRES (GENRE_ID, GENRE_NAME)
VALUES (3, 'Мультфильм');
MERGE INTO GENRES (GENRE_ID, GENRE_NAME)
VALUES (4, 'Триллер');
MERGE INTO GENRES (GENRE_ID, GENRE_NAME)
VALUES (5, 'Документальный');
MERGE INTO GENRES (GENRE_ID, GENRE_NAME)
VALUES (6, 'Боевик');