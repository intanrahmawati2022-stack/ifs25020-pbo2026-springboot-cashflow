package org.delcom.ifs25020pbo2026springbootcashflow.domain.exception;

/** Dilempar ketika entitas dengan ID tertentu tidak ditemukan. */
public class EntityNotFoundException extends DomainException {
    private final int id;

    public EntityNotFoundException(int id) {
        super("Transaksi dengan ID " + id + " tidak ditemukan.");
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
