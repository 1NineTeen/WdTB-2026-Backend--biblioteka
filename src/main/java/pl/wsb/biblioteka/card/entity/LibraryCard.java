
package pl.wsb.biblioteka.card.entity;

import jakarta.persistence.*;
import pl.wsb.biblioteka.reader.entity.Reader;

import java.time.LocalDate;

@Entity
@Table(name = "library_card")
public class LibraryCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true)
    private String cardNumber;

    @Column(name = "issued_on", nullable = false)
    private LocalDate issuedOn;

    @Column(name = "expires_on", nullable = false)
    private LocalDate expiresOn;

    @Column(name = "active", nullable = false)
    private Boolean active;

    // LAB 03 - jedna karta nalezy do jednego czytelnika
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reader_id",
            nullable = false, unique = true)
    private Reader reader;

    // Konstruktor wymagany przez JPA
    protected LibraryCard() {
    }

    // Konstruktor karty bibliotecznej
    public LibraryCard(
            String cardNumber,
            LocalDate issuedOn,
            LocalDate expiresOn,
            Boolean active,
            Reader reader) {

        this.cardNumber = cardNumber;
        this.issuedOn = issuedOn;
        this.expiresOn = expiresOn;
        this.active = active;
        this.reader = reader;
    }

    public Long getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public LocalDate getIssuedOn() {
        return issuedOn;
    }

    public LocalDate getExpiresOn() {
        return expiresOn;
    }

    public Boolean getActive() {
        return active;
    }

    public Reader getReader() {
        return reader;
    }
}
