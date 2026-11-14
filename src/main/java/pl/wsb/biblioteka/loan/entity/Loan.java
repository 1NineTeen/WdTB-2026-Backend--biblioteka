package pl.wsb.biblioteka.loan.entity;

import jakarta.persistence.*;
import pl.wsb.biblioteka.reader.entity.Reader;
import pl.wsb.biblioteka.book.entity.Book;

import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "loan")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoanStatus status;

    @Column(name = "renewal_count", nullable = false)
    private Integer renewalCount;

    // LAB 03 - wypozyczenie nalezy do czytelnika
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reader_id", nullable = false)
    private Reader reader;

    // LAB 03 - jedno wypozyczenie moze zawierac wiele ksiazek
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "loan_book",
            joinColumns = @JoinColumn(name = "loan_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private Set<Book> books = new HashSet<>();

    // Konstruktor wymagany przez JPA
    protected Loan() {
    }

    // Konstruktor do tworzenia wypozyczen
    public Loan(
            LocalDate loanDate,
            LocalDate dueDate,
            LocalDate returnDate,
            LoanStatus status,
            Integer renewalCount,
            Reader reader) {

        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.renewalCount = renewalCount;
        this.reader = reader;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public Integer getRenewalCount() {
        return renewalCount;
    }

    public Reader getReader() {
        return reader;
    }

    public Set<Book> getBooks() {
        return books;
    }
}
