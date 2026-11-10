package org.delcom.ifs25020pbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi. Setiap opsi membawa comparator-nya sendiri
 * sehingga logika pengurutan terpusat di domain.
 */
public enum SortOption {
    /** Jumlah terkecil lebih dulu. */
    AMOUNT_ASC("Jumlah terkecil", Comparator.comparing(Transaction::getAmount)),

    /** Jumlah terbesar lebih dulu. */
    AMOUNT_DESC("Jumlah terbesar", Comparator.comparing(Transaction::getAmount).reversed()),

    /** Pemasukan ditampilkan lebih dulu. */
    INCOME_FIRST("Pemasukan dulu", Comparator.comparing((Transaction t) -> t.getType() != TransactionType.INCOME)),

    /** Pengeluaran ditampilkan lebih dulu. */
    EXPENSE_FIRST("Pengeluaran dulu", Comparator.comparing((Transaction t) -> t.getType() != TransactionType.EXPENSE));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    /** Nama opsi untuk ditampilkan. */
    public String label() {
        return label;
    }

    /** Comparator untuk opsi ini. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
