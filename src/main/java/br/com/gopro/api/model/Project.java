package br.com.gopro.api.model;

import br.com.gopro.api.enums.ProjectGovIfEnum;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
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
@Table(name = "projects")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status", nullable = false)
    private ProjectStatusEnum projectStatus;

    @Column(name = "area_segmento")
    private String areaSegmento;

    @Column(name = "object", nullable = false, columnDefinition = "text")
    private String object;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "primary_partner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_project_primary_partner"))
    private Partner primaryPartner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secundary_partner_id", foreignKey = @ForeignKey(name = "fk_project_secundary_partner"))
    private Partner secundaryPartner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "primary_client_id", nullable = false, foreignKey = @ForeignKey(name = "fk_project_primary_client"))
    private PublicAgency primaryClient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secundary_client_id", foreignKey = @ForeignKey(name = "fk_project_secundary_client"))
    private Secretary secundaryClient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cordinator_id", foreignKey = @ForeignKey(name = "fk_project_cordinator_id"))
    private People cordinator;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_gov_if")
    private ProjectGovIfEnum projectGovIf;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type")
    private ProjectTypeEnum projectType;

    @Column(name = "contract_value", precision = 15, scale = 2)
    private BigDecimal contractValue;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "opening_date")
    private LocalDate openingDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "execution_location", length = 400)
    private String executionLocation;

    @Column(name = "total_received", nullable = false, precision = 35, scale = 2)
    private BigDecimal totalReceived;

    @Column(name = "total_expenses", nullable = false, precision = 35, scale = 2)
    private BigDecimal totalExpenses;

    @Column(name = "saldo", nullable = false, precision = 35, scale = 2)
    private BigDecimal saldo;
    @Column(name = "is_active")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}



