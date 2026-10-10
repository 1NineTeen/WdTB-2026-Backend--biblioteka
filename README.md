# WdTB-2026-Backend

Szablon projektu laboratoryjnego z przedmiotu **Wprowadzenie do technologii backendowych** (Uniwersytet WSB Merito Wrocław).

Prowadzący: **Wojciech Koszela** · [LinkedIn](https://www.linkedin.com/in/wkoszela/) · [GitHub](https://github.com/wkoszela)

**Dokumentacja: otwórzcie [`docs/index.html`](docs/index.html) w przeglądarce.** Są tam instrukcje do zajęć,
wymagania, zasady zaliczenia i opis reguł projektu.

## W skrócie

- Zespół 3 osoby, jedno repozytorium: **fork** tego szablonu przemianowany na **`WdTB-2026-Backend-<nazwaprojektu>`**.
- Każdy zespół buduje backend własnego projektu: encje i relacje, repozytoria i zapytania, serwisy, REST API.
- Wymagania każdego laboratorium sprawdzają automatyczne testy (reguły projektu) w GitHub Actions. Tag `lab-N` uruchamia reguły laboratorium N.

| Zajęcia | Temat |
|---|---|
| LAB 01 | Start: projekt, aplikacja i opis pomysłu (bez oceny) |
| LAB 02 | Encje i relacje w JPA |
| LAB 03 | Repozytoria i zapytania |
| LAB 04 | Serwisy i komponenty |
| LAB 05 | Kontrolery REST |
| LAB 06 | Rozbudowa: zdarzenia, konfiguracja, zadania cykliczne |
| LAB 07 | Odrabianie |

## Po forku

1. *Settings → General → Repository name* → `WdTB-2026-Backend-<nazwaprojektu>` → **Rename**.
2. Zakładka **Actions** → *I understand my workflows, go ahead and enable them*.
3. *Settings → Collaborators → Add people*: osoby z zespołu i `wkoszela`.
4. Nazwa projektu i link do repozytorium do arkusza zespołów.
5. Pull Requesty kierujcie do **swojego** repozytorium (*base repository* = Wasz fork).

Szczegóły: `docs/lab01.html`, część B.

## Uruchomienie

Wymagania: JDK 21 lub nowsze, IntelliJ IDEA (albo Maven 3.9+), Git. Docker nie jest potrzebny.

```bash
mvn spring-boot:run      # aplikacja: http://localhost:8080, konsola bazy H2: http://localhost:8080/h2-console
mvn verify               # testy zespołu i reguły dla laboratorium z <lab> w pom.xml
mvn verify -Dlab=2       # podgląd reguł kolejnego laboratorium
```

Baza H2 (JDBC URL `jdbc:h2:file:./data/wdtb`, użytkownik `sa`, hasło puste). Tabele tworzy Hibernate z encji,
dane startowe są w `src/main/resources/data.sql`.

## Konwencja nazw

| Gdzie | Wzór |
|---|---|
| repozytorium (fork, po zmianie nazwy) | `WdTB-2026-Backend-<nazwaprojektu>` |
| pakiet Javy | `pl.wsb.<nazwaprojektu>` |
| `artifactId` w `pom.xml` | `wdtb-2026-backend-<nazwaprojektu>` |
| karta projektu | `PROJEKT.md` |
