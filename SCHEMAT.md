# Schemat bazy danych

Plik generują reguły projektu (panel Maven → Lifecycle → verify). Nie edytujcie go ręcznie.

Tabele: 6 (w tym tabele łączące @ManyToMany: loan_book) · kolumny: 34 · relacje: 2 × @ManyToOne, 1 × @OneToMany, 1 × @OneToOne, 1 × @ManyToMany

| Tabela | Encja | Kolumny | Klucze obce |
|---|---|---|---|
| book | Book | 7 | user_account_id |
| library_card | LibraryCard | 6 | reader_id |
| loan | Loan | 7 | reader_id |
| reader | Reader | 9 | — |
| user_account | UserAccount | 5 | — |

```mermaid
erDiagram
    book {
        Long id PK
        String author
        Boolean available
        BookCategory category
        String isbn
        String title
        Long user_account_id FK
    }
    library_card {
        Long id PK
        Boolean active
        String card_number
        LocalDate expires_on
        LocalDate issued_on
        Long reader_id FK
    }
    loan {
        Long id PK
        LocalDate due_date
        LocalDate loan_date
        Long reader_id FK
        Integer renewal_count
        LocalDate return_date
        LoanStatus status
    }
    reader {
        Long id PK
        Boolean active
        String city
        String email
        String first_name
        String last_name
        String postal_code
        LocalDate registered_on
        String street
    }
    user_account {
        Long id PK
        LocalDateTime created_at
        String display_name
        String email
        AccountRole role
    }
    loan }o--o{ book : "loan_book"
    reader ||--o{ loan : "reader_id"
    reader ||--o| library_card : "reader_id"
    user_account ||--o{ book : "user_account_id"
```
