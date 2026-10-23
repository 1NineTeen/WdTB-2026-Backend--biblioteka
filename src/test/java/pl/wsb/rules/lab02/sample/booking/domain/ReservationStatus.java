package pl.wsb.rules.lab02.sample.booking.domain;

/** Cykl życia rezerwacji. W bazie zapisywany jako tekst (@Enumerated(EnumType.STRING)). */
public enum ReservationStatus {
    NEW,
    CONFIRMED,
    CANCELLED,
    DONE
}
