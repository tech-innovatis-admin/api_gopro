package br.com.gopro.api.model;

import br.com.gopro.api.enums.DocumentOwnerTypeEnum;
import br.com.gopro.api.enums.DocumentStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "documents",
        indexes = {
                @Index(name = "idx_documents_owner", columnList = "owner_type,owner_id"),
                @Index(name = "idx_documents_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_documents_s3_key", columnNames = "s3_key")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", length = 50, nullable = false)
    private DocumentOwnerTypeEnum ownerType;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "category", length = 100)
    private String category;

    @NotBlank
    @Column(name = "original_name", length = 255, nullable = false)
    private String originalName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @NotNull
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "sha256", length = 64)
    private String sha256;

    @NotBlank
    @Column(name = "bucket", length = 255, nullable = false)
    private String bucket;

    @NotBlank
    @Column(name = "s3_key", length = 500, nullable = false)
    private String s3Key;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private DocumentStatusEnum status;
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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}


