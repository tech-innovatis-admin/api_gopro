package br.com.gopro.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budget_item_project_id"))
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "budget_categories_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budget_item_budget_categories_id"))
    private BudgetCategories budgetCategories;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_cost", precision = 15, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "palnned_amount", precision = 15, scale = 2)
    private BigDecimal plannedAmount;

    @Column(name = "executed_amount", precision = 15, scale = 2)
    private BigDecimal executedAmount;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
