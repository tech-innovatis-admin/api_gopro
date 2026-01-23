package br.com.gopro.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project", nullable = false, foreignKey = @ForeignKey(name = "fk_expenses_project_id"))
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "budget_items", nullable = false, foreignKey = @ForeignKey(name = "fk_expenses_budget_items_id"))
    private BudgetItems budgetItems;

    @ManyToOne(optional = false)
    @JoinColumn(name = "budget_categories", nullable = false, foreignKey = @ForeignKey(name = "fk_expenses_budget_categories_id"))
    private BudgetCategories budgetCategories;

    @ManyToOne(optional = false)
    @JoinColumn(name = "income", nullable = false, foreignKey = @ForeignKey(name = "fk_expenses_income_id"))
    private Income income;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_people", foreignKey = @ForeignKey(name = "fk_expenses_project_people_id"))
    private ProjectPeople projectPeople;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_organization", foreignKey = @ForeignKey(name = "fk_expenses_project_organization"))
    private ProjectOrganization projectOrganization;

    @Column(name = "description")
    private String description;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @OneToMany
    @JoinColumn(name = "expense_id")
    private List<Document> documents;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
