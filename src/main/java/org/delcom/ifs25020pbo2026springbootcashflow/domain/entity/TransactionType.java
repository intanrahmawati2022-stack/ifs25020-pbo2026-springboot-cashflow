package org.delcom.ifs25020pbo2026springbootcashflow.domain.entity;

/** Jenis transaksi keuangan. */
public enum TransactionType {
    /** Uang masuk (menambah saldo). */
    INCOME("Pemasukan"),
    /** Uang keluar (mengurangi saldo). */
    EXPENSE("Pengeluaran");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    /** Nama jenis transaksi dalam bahasa Indonesia. */
    public String label() {
        return label;
    }
}
