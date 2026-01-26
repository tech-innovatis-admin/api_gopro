package br.com.gopro.api.model;

import br.com.gopro.api.enums.BudgetTransferStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_transfers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budget_transfer_project"))
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "budget_items_id", nullable = false, foreignKey = @ForeignKey(name = "fk_budget_transfer_budget_items"))
    private BudgetItems budgetItems;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_budget_categories", nullable = false, foreignKey = @ForeignKey(name = "fk_budget_transfer_from_budget_categories"))
    private BudgetCategories fromBudgetCategories;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tp_budget_categories", nullable = false, foreignKey = @ForeignKey(name = "fk_budget_transfer_to_budget_categories"))
    private BudgetCategories toBudgetCategories;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_transfer_status", nullable = false)
    private BudgetTransferStatusEnum budgetTransferStatus;

    @Lob
    @Column(name = "reason")
    private String reason;

    @ManyToOne(optional = false)
    @JoinColumn(name = "document_id")
    private Document document;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false, nullable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

}
