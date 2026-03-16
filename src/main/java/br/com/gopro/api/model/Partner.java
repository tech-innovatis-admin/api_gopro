package br.com.gopro.api.model;

import br.com.gopro.api.enums.PartnersTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "partners")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "acronym")
    private String acronym;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "trade_name", nullable = false)
    private String tradeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "partners_type", nullable = false)
    private PartnersTypeEnum partnersType;

    @Column(name = "cnpj", nullable = false, length = 14)
    private String cnpj;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "address", length = 400)
    private String address;

    @Column(name = "site", length = 300)
    private String site;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", length = 50, nullable = false)
    private String state;

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
