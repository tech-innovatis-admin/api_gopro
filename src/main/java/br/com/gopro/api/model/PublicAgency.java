package br.com.gopro.api.model;

import br.com.gopro.api.enums.PublicAgencyTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "public_agencies")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicAgency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, length = 20)
    private String code;

    @Column(name = "sigla", length = 20)
    private String sigla;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "cnpj", length = 14, unique = true)
    private String cnpj;

    @Column(name = "is_client", nullable = false)
    private Boolean isClient;

    @Enumerated(EnumType.STRING)
    @Column(name = "public_agency_type", nullable = false)
    private PublicAgencyTypeEnum publicAgencyType;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "address", length = 400)
    private String address;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "is_active", nullable = false)
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
