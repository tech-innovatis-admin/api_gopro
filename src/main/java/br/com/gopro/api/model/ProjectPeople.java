package br.com.gopro.api.model;

import br.com.gopro.api.enums.ContractTypeEnum;
import br.com.gopro.api.enums.RoleProjectPeopleEnum;
import br.com.gopro.api.enums.StatusProjectPeopleEnum;
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
@Table(name = "project_people")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectPeople {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_project_people_project_id"))
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false, foreignKey = @ForeignKey(name = "fk_project_people_person_id"))
    private People person;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 255)
    private RoleProjectPeopleEnum role;

    @Column(name = "workload_hours", precision = 5, scale = 2)
    private BigDecimal workloadHours;

    @Column(name = "institutional_link", length = 255)
    private String institutionalLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 50)
    private ContractTypeEnum contractType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusProjectPeopleEnum status;

    @Column(name = "base_amount", precision = 15, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;
    @Column(name = "is_active")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}



