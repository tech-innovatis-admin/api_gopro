package br.com.gopro.api.model;

import br.com.gopro.api.enums.StatusProjectsEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
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

    @Column(name = "code", length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_projects", nullable = false)
    private StatusProjectsEnum statusProjects;

    @Column(name = "area_segmento")
    private String areaSegmento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "orgao_financiador_id", nullable = false, foreignKey = @ForeignKey(name = "fk_project_orgao_financiador"))
    private Organization orgaoFinancioador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "executing_org_id", nullable = false, foreignKey = @ForeignKey(name = "fk_project_executing_org"))
    private Organization executingOrg;

    @Column(name = "cordinator")
    private String cordinator;

    @Column(name = "scope", length = 500)
    private String scope;

    @Column(name = "contract_value", precision = 15, scale = 2)
    private BigDecimal contractValue;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "opening_date")
    private LocalDateTime openingDate;

    @Column(name = "execution_location")
    private String executionLocation;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long UpdatedBy;
}
