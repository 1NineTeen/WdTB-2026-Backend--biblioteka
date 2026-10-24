package pl.wsb.rules.examples.cafe.entity;

/** Cykl życia rezerwacji. W bazie zapisywany jako tekst (@Enumerated(EnumType.STRING)). */
public enum ReservationStatus {
    NEW,
    CONFIRMED,
    CANCELLED,
    DONE
}
