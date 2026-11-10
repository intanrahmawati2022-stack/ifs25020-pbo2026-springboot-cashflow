package org.delcom.ifs25020pbo2026springbootcashflow.usecase;

import org.delcom.ifs25020pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import java.math.BigDecimal;
import java.util.List;

/**
 * Use case pencatatan keuangan. Tidak melakukan I/O; hanya memproses data
 * dan melempar checked exception domain bila operasi tidak valid.
 */
public class FinanceUseCase {
    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    /** Menambah pemasukan setelah validasi keterangan dan jumlah. */
    public Transaction addIncome(String description, String amountText) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal amount = parseAmount(amountText);
        return repository.saveIncome(cleanDescription, amount);
    }

    /** Menambah pengeluaran setelah validasi keterangan dan jumlah. */
    public Transaction addExpense(String description, String amountText) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal amount = parseAmount(amountText);
        return repository.saveExpense(cleanDescription, amount);
    }

    /** Menghapus transaksi; melempar exception jika ID tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException {
        if (!repository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi berdasarkan keterangan (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        String lower = keyword == null ? "" : keyword.trim().toLowerCase();
        return repository.findBy(t -> t.getDescription().toLowerCase().contains(lower));
    }

    /** Mengurutkan transaksi sesuai {@link SortOption}. */
    public List<Transaction> sortTransactions(SortOption option) {
        return repository.findAll().stream().sorted(option.comparator()).toList();
    }

    /** Total pemasukan. */
    public BigDecimal getTotalIncome() {
        return sumOf(true);
    }

    /** Total pengeluaran. */
    public BigDecimal getTotalExpense() {
        return sumOf(false);
    }

    /** Saldo = total pemasukan - total pengeluaran. */
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    private BigDecimal sumOf(boolean income) {
        return repository.findAll().stream()
                .filter(t -> t.isIncome() == income)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String validateDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private BigDecimal parseAmount(String amountText) throws ValidationException {
        if (amountText == null || amountText.trim().isEmpty()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        String text = amountText.trim();
        if (!text.matches("[+-]?\\d+(\\.\\d+)?")) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        BigDecimal amount = new BigDecimal(text);
        if (amount.signum() <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new ValidationException("Jumlah maksimal 2 angka di belakang koma");
        }
        return amount;
    }
}
