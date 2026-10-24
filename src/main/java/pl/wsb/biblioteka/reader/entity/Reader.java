package pl.wsb.biblioteka.reader.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reader")
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name",
            nullable = false, length = 60)
    private String firstName;

    @Column(name = "last_name",
            nullable = false, length = 80)
    private String lastName;

    @Column(name = "email",
            nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "registered_on", nullable = false)
    private LocalDate registeredOn;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Embedded
    private Address address;

    protected Reader() {
    }

    public Reader(String firstName, String lastName,
                  String email, LocalDate registeredOn,
                  Boolean active, Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.registeredOn = registeredOn;
        this.active = active;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getRegisteredOn() {
        return registeredOn;
    }

    public Boolean getActive() {
        return active;
    }

    public Address getAddress() {
        return address;
    }
}