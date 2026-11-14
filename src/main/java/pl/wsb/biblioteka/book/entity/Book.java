package pl.wsb.biblioteka.book.entity;

import jakarta.persistence.*;
import pl.wsb.biblioteka.account.domain.UserAccount;

@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "isbn", nullable = false,
            unique = true, length = 13)
    private String isbn;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private BookCategory category;

    @Column(name = "available", nullable = false)
    private Boolean available;

    // LAB 03 - uzytkownik, ktory dodal ksiazke
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount addedBy;

    // Konstruktor wymagany przez JPA
    protected Book() {
    }

    // Konstruktor ksiazki
    public Book(
            String title,
            String author,
            String isbn,
            BookCategory category,
            Boolean available,
            UserAccount addedBy) {

        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.available = available;
        this.addedBy = addedBy;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public BookCategory getCategory() {
        return category;
    }

    public Boolean getAvailable() {
        return available;
    }

    public UserAccount getAddedBy() {
        return addedBy;
    }
}
