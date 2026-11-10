package org.delcom.ifs25020pbo2026springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.delcom.ifs25020pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25020pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/** Smoke test: konteks Spring naik dan bean Clean Architecture ter-wiring. */
@SpringBootTest
class AppTest {
    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoadsAndBeansAreWired() {
        assertNotNull(context.getBean(ITransactionRepository.class));
        assertNotNull(context.getBean(FinanceUseCase.class));
        assertNotNull(context.getBean(FinancePresenter.class));
    }

    @Test
    void mainDelegatesToSpringApplication() {
        assertTrue(App.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }
}
