package org.delcom.ifs25020pbo2026springbootcashflow.framework;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;

/** Konfigurasi halaman global (meta tag untuk SEO). */
@Meta(name = "description",
        content = "Aplikasi web pencatat pemasukan dan pengeluaran pribadi beserta saldo, "
                + "dibangun dengan Spring Boot dan Vaadin.")
public class AppShell implements AppShellConfigurator {
}
