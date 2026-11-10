package org.delcom.ifs25020pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25020pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25020pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25020pbo2026springbootcashflow.usecase.FinanceUseCase;
import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin aplikasi catatan keuangan.
 * Pengurutan lewat klik header kolom; hapus lewat dialog konfirmasi.
 */
@Route("")
@PageTitle("Cashflow - Catatan Keuangan Pribadi")
public class FinanceView extends VerticalLayout {
    private static final String EMPTY_ALL = "- Data transaksi belum tersedia!";
    private static final String EMPTY_SEARCH = "- Transaksi tidak ditemukan!";

    private final FinanceUseCase useCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortBox = new ComboBox<>();
    private List<Transaction> currentItems = List.of();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span(EMPTY_ALL);
    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span("Rp 0.00");
    private final Span expenseTotal = new Span("Rp 0.00");
    private final Span balanceLabel = new Span("Rp 0.00");

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter) {
        this.useCase = useCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setAriaLabel("Cari keterangan transaksi");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(SortOption::label);
        sortBox.setPlaceholder("Urutkan...");
        sortBox.setAriaLabel("Urutkan transaksi");
        sortBox.setClearButtonVisible(true);
        sortBox.addValueChangeListener(e -> applySort());
        grid.getElement().setAttribute("aria-label", "Daftar transaksi keuangan");
        // Landmark utama halaman (aksesibilitas)
        getElement().setAttribute("role", "main");

        add(new H1("Catatan Keuangan"), buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Bahasa dokumen untuk Lighthouse/Axe (html-has-lang)
        attachEvent.getUI().getPage().executeJs("document.documentElement.lang = 'id';");
    }

    // ---------- ringkasan ----------
    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout row = new HorizontalLayout(
                card("Jumlah Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E3A8A"),
                card("Total Pemasukan", incomeTotal, "#DCFCE7", "#16A34A", "#14532D"),
                card("Total Pengeluaran", expenseTotal, "#FEE2E2", "#DC2626", "#7F1D1D"),
                card("Saldo", balanceLabel, "#FEF3C7", "#D97706", "#78350F"));
        row.setWidthFull();
        row.getStyle().set("align-items", "stretch");
        return row;
    }

    /** Kartu ringkasan; warna teks gelap agar kontras terhadap latar (WCAG AA). */
    private Div card(String caption, Span value, String background, String accent, String textColor) {
        Span cap = new Span(caption);
        cap.getStyle().set("display", "block").set("font-size", "0.875rem")
                .set("font-weight", "500").set("color", textColor);
        value.getStyle().set("display", "block").set("font-size", "1.5rem")
                .set("font-weight", "700").set("color", textColor);
        Div box = new Div(cap, value);
        box.getStyle().set("flex", "1").set("min-width", "0").set("background", background)
                .set("border-left", "6px solid " + accent).set("border-radius", "12px")
                .set("padding", "1rem").set("box-sizing", "border-box");
        return box;
    }

    /** Tombol dengan warna latar gelap dan teks putih (kontras tinggi). */
    private Button solidButton(Button button, String background) {
        button.getStyle().set("background-color", background).set("color", "#FFFFFF");
        return button;
    }

    /** Tombol sekunder dengan teks biru tua (kontras tinggi). */
    private Button plainButton(Button button) {
        button.getStyle().set("color", "#1E3A8A");
        return button;
    }

    private void updateSummary() {
        totalCount.setText(String.valueOf(useCase.getAllTransactions().size()));
        incomeTotal.setText(presenter.formatMoney(useCase.getTotalIncome()));
        expenseTotal.setText(presenter.formatMoney(useCase.getTotalExpense()));
        balanceLabel.setText(presenter.formatMoney(useCase.getBalance()));
    }

    // ---------- grid ----------
    private void configureGrid() {
        grid.addColumn(Transaction::getId).setHeader("ID").setKey("id")
                .setAutoWidth(true).setFlexGrow(0).setSortable(true)
                .setComparator(Transaction::getId);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setKey("description")
                .setFlexGrow(1).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah").setKey("amount")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getAmount));
        grid.addColumn(t -> t.getType().label()).setHeader("Tipe").setKey("type")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));
        grid.addColumn(presenter::formatCreatedAt).setHeader("Waktu").setKey("createdAt")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));
        grid.addComponentColumn(this::actionButtons).setHeader("Tindakan").setKey("tindakan")
                .setAutoWidth(true).setFlexGrow(0);
        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
    }

    private HorizontalLayout actionButtons(Transaction t) {
        Button delete = new Button("Hapus", VaadinIcon.TRASH.create(), e -> openDeleteDialog(t));
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        delete.getStyle().set("color", "#991B1B");
        delete.getElement().setAttribute("aria-label", "Hapus transaksi " + t.getId());
        return new HorizontalLayout(delete);
    }

    // ---------- toolbar ----------
    private HorizontalLayout buildToolbar() {
        Button income = solidButton(
                new Button("Tambah Pemasukan", VaadinIcon.ARROW_DOWN.create(), e -> openAddDialog(true)),
                "#166534");
        Button expense = solidButton(
                new Button("Tambah Pengeluaran", VaadinIcon.ARROW_UP.create(), e -> openAddDialog(false)),
                "#991B1B");
        Button search = plainButton(new Button("Cari", VaadinIcon.SEARCH.create(), e -> search()));
        Button refresh = plainButton(new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> refreshAll()));
        HorizontalLayout bar = new HorizontalLayout(searchField, sortBox, search, refresh, income, expense);
        bar.setWidthFull();
        bar.setFlexGrow(1, searchField);
        return bar;
    }

    // ---------- dialog ----------
    private void openAddDialog(boolean income) {
        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        TextField amount = new TextField("Jumlah (Rp)");
        amount.setWidthFull();
        VerticalLayout form = new VerticalLayout(description, amount);
        form.setPadding(false);

        Dialog dialog = new Dialog();
        Button save = new Button("Simpan", VaadinIcon.CHECK.create(), e -> {
            try {
                Transaction created = income
                        ? useCase.addIncome(description.getValue(), amount.getValue())
                        : useCase.addExpense(description.getValue(), amount.getValue());
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());
        buildDialog(dialog, income ? "Menambah Pemasukan" : "Menambah Pengeluaran", form, cancel, save);
        dialog.open();
    }

    private void openDeleteDialog(Transaction t) {
        Paragraph message = new Paragraph("Yakin ingin menghapus transaksi \"" + t.getDescription()
                + "\" (ID " + t.getId() + ", " + presenter.formatMoney(t.getAmount()) + ")?");
        Dialog dialog = new Dialog();
        Button confirm = new Button("Hapus", VaadinIcon.TRASH.create(), e -> {
            deleteTransaction(t);
            dialog.close();
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancel = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());
        buildDialog(dialog, "Menghapus Transaksi", message, cancel, confirm);
        dialog.open();
    }

    private void buildDialog(Dialog dialog, String title, Component body, Button... buttons) {
        dialog.setHeaderTitle(title);
        dialog.setWidth("480px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.removeAll();
        dialog.add(body);
        dialog.getFooter().add(buttons);
    }

    // ---------- aksi ----------
    private void deleteTransaction(Transaction t) {
        try {
            useCase.removeTransaction(t.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
            refreshAll();
        }
    }

    private void search() {
        String keyword = searchField.getValue();
        if (keyword == null || keyword.isBlank()) {
            refreshAll();
            return;
        }
        List<Transaction> results = useCase.searchTransactions(keyword);
        emptyState.setText(results.isEmpty() ? EMPTY_SEARCH : EMPTY_ALL);
        refreshGrid(results, "Hasil Pencarian: \"" + keyword + "\"");
    }

    private void refreshAll() {
        emptyState.setText(EMPTY_ALL);
        refreshGrid(useCase.getAllTransactions(), "Daftar Transaksi");
        searchField.clear();
    }

    private void applySort() {
        SortOption option = sortBox.getValue();
        if (option == null) {
            return;
        }
        grid.setItems(currentItems.stream().sorted(option.comparator()).toList());
    }

    private void refreshGrid(List<Transaction> items, String title) {
        currentItems = items;
        SortOption option = sortBox.getValue();
        grid.setItems(option == null ? items : items.stream().sorted(option.comparator()).toList());
        updateSummary();
        statusLabel.setText(items.isEmpty()
                ? title + " — " + emptyState.getText().replaceFirst("^- ", "")
                : title);
    }

    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification.show(message, 4000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
