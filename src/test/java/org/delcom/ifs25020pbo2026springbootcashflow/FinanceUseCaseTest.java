package org.delcom.ifs25020pbo2026springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25020pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25020pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25020pbo2026springbootcashflow.usecase.FinanceUseCase;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FinanceUseCaseTest {
    private FinanceUseCase useCase;
    private final FinancePresenter presenter = new FinancePresenter();

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void balanceIsIncomeMinusExpense() throws Exception {
        useCase.addIncome("Gaji", "5000000");
        useCase.addExpense("Makan", "25000");
        assertEquals(new BigDecimal("4975000"), useCase.getBalance());
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(useCase.getBalance()));
    }

    @Test
    void balanceStaysConsistentAfterRemove() throws Exception {
        Transaction t = useCase.addExpense("Buku", "100000");
        useCase.addIncome("Bonus", "300000");
        useCase.removeTransaction(t.getId());
        assertEquals(new BigDecimal("300000"), useCase.getBalance());
    }

    @Test
    void validationRejectsBadInput() {
        assertThrows(ValidationException.class, () -> useCase.addIncome("  ", "1000"));
        assertThrows(ValidationException.class, () -> useCase.addIncome(null, "1000"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", "-5"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", "0"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", "abc"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", ""));
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void removeUnknownIdThrows() {
        EntityNotFoundException ex =
                assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(99));
        assertEquals(99, ex.getId());
    }

    @Test
    void searchIsCaseInsensitiveAndDoesNotMutate() throws Exception {
        useCase.addIncome("Gaji Bulanan", "100");
        useCase.addExpense("Makan siang", "10");
        assertEquals(1, useCase.searchTransactions("GAJI").size());
        assertEquals(2, useCase.getAllTransactions().size());
    }

    @Test
    void sortOptionsWork() throws Exception {
        useCase.addIncome("A", "50");
        useCase.addExpense("B", "10");
        useCase.addIncome("C", "30");
        assertEquals("B", useCase.sortTransactions(SortOption.AMOUNT_ASC).get(0).getDescription());
        assertEquals("A", useCase.sortTransactions(SortOption.AMOUNT_DESC).get(0).getDescription());
        assertEquals("B", useCase.sortTransactions(SortOption.EXPENSE_FIRST).get(0).getDescription());
        assertTrue(useCase.sortTransactions(SortOption.INCOME_FIRST).get(0).isIncome());
    }

    @Test
    void findAllReturnsDefensiveCopy() throws Exception {
        useCase.addIncome("A", "1");
        List<Transaction> copy = useCase.getAllTransactions();
        copy.clear();
        assertEquals(1, useCase.getAllTransactions().size());
    }

    @Test
    void presenterFormatsMoneyAndRow() throws Exception {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(new BigDecimal("1000000")));
        Transaction t = useCase.addIncome("Gaji", "1000000");
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(t));
    }
}
