package org.delcom.ifs25020pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25020pbo2026springbootcashflow.domain.entity.Transaction;
import java.math.BigDecimal;

/** Port spesifik Transaction dengan penomoran ID otomatis. */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /** Menyimpan transaksi pemasukan baru. */
    Transaction saveIncome(String description, BigDecimal amount);

    /** Menyimpan transaksi pengeluaran baru. */
    Transaction saveExpense(String description, BigDecimal amount);
}
