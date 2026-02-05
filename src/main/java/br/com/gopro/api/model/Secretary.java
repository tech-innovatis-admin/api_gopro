package br.com.gopro.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "secretariats")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Secretary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "sigla")
    private String sigla;

    @ManyToOne(optional = false)
    @JoinColumn(name = "public_agency_id", nullable = false, foreignKey = @ForeignKey(name = "fk_secretary_public_agency"))
    private PublicAgency publicAgency;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cnpj", length = 14)
    private String cnpj;

    @Column(name = "is_client", nullable = false)
    private Boolean isClient;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "address", length = 400)
    private String address;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "is_active")
    private Boolean isActive;

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
