package br.com.gopro.api.repository;

import br.com.gopro.api.enums.DocumentOwnerTypeEnum;
import br.com.gopro.api.enums.DocumentStatusEnum;
import br.com.gopro.api.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByIdAndStatusNot(UUID id, DocumentStatusEnum status);
    boolean existsByS3Key(String s3Key);
    List<Document> findByOwnerTypeAndOwnerIdAndStatus(DocumentOwnerTypeEnum ownerType, Long ownerId, DocumentStatusEnum status);
}
