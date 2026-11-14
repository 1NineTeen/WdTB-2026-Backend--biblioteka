-- Dane startowe aplikacji. Tabele tworzy Hibernate z encji, a ten plik Spring Boot uruchamia zaraz potem.
-- LAB 01: dopiszcie tu konta wszystkich osób z zespołu (reguła R1.03).
-- LAB 02: dopiszcie dane do swoich tabel (kolejność: najpierw tabele, na które wskazują klucze obce).
insert into user_account (email, display_name, role, created_at) values
    ('wrx85921@student.wroclaw.merito.pl',   'Damian',   'USER',    current_timestamp),
    ('wrx82077@student.wroclaw.merito.pl',  'Mateusz',  'USER',    current_timestamp),
    ('s608714@student.wroclaw.merito.pl',       'Eliasz', 'USER', current_timestamp),
    ('admin@example.com',       'Admin', 'ADMIN',   current_timestamp);

-- LAB 01: Wasze konta (zmieńcie na swoje dane, rola USER)
-- insert into user_account (email, display_name, role, created_at) values
--     ('imie.nazwisko@student.merito.pl', 'Imię', 'USER', current_timestamp);

-- ===== LAB 02/03 - BOOK =====
-- Kazda ksiazka ma przypisanego uzytkownika

INSERT INTO book
(title, author, isbn, category, available, user_account_id)
VALUES
    ('Wiedzmin: Ostatnie zyczenie',
     'Andrzej Sapkowski', '9788375780635', 'FANTASY', true, 1),

    ('Lalka',
     'Boleslaw Prus', '9788373271890', 'FICTION', false, 2),

    ('Krotka historia czasu',
     'Stephen Hawking', '9788328701861', 'SCIENCE', false, 3);


-- ===== LAB 02/03 - READER =====
-- Czytelnicy biblioteki

INSERT INTO reader
(first_name, last_name, email,
 registered_on, active, street, city, postal_code)
VALUES
    ('Jan', 'Kowalski', 'jan@example.com',
     '2026-10-01', true, 'Grabiszynska 10', 'Wroclaw', '53-500'),

    ('Anna', 'Nowak', 'anna@example.com',
     '2026-10-05', true, 'Legnicka 25', 'Wroclaw', '54-204'),

    ('Piotr', 'Wisniewski', 'piotr@example.com',
     '2026-10-10', false, 'Dluga 15', 'Wroclaw', '53-633');


-- ===== LAB 03 - LIBRARY_CARD =====
-- Kazda karta jest przypisana do czytelnika

INSERT INTO library_card
(card_number, issued_on, expires_on, active, reader_id)
VALUES
    ('K-0001', '2026-10-01', '2027-10-01', true, 1),
    ('K-0002', '2026-10-05', '2027-10-05', true, 2),
    ('K-0003', '2026-10-10', '2027-10-10', false, 3);


-- ===== LAB 02/03 - LOAN =====
-- Kazde wypozyczenie ma przypisanego czytelnika

INSERT INTO loan
(loan_date, due_date, return_date,
 status, renewal_count, reader_id)
VALUES
    ('2026-10-01', '2026-10-15', '2026-10-12',
     'RETURNED', 0, 1),

    ('2026-11-01', '2026-11-20', NULL,
     'ACTIVE', 0, 2),

    ('2026-09-01', '2026-09-30', NULL,
     'OVERDUE', 1, 3);


-- ===== LAB 03 - LOAN_BOOK =====
-- Powiazania wypozyczen z ksiazkami

INSERT INTO loan_book (loan_id, book_id)
VALUES
    (1, 1),
    (1, 2),
    (2, 3),
    (3, 2);
