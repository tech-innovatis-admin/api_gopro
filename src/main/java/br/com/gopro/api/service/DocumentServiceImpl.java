package br.com.gopro.api.service;

import br.com.gopro.api.config.DocumentsS3Properties;
import br.com.gopro.api.dtos.DocumentDownloadUrlDTO;
import br.com.gopro.api.dtos.DocumentResponseDTO;
import br.com.gopro.api.enums.DocumentOwnerTypeEnum;
import br.com.gopro.api.enums.DocumentStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.model.Document;
import br.com.gopro.api.repository.DocumentRepository;
import br.com.gopro.api.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private static final long MAX_SIZE_BYTES = 20L * 1024L * 1024L;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final DocumentsS3Properties documentsS3Properties;

    @Transactional
    @Override
    public DocumentResponseDTO upload(MultipartFile file, DocumentOwnerTypeEnum ownerType, Long ownerId, String category, Long createdBy) {
        validateFile(file);
        validateOwner(ownerType, ownerId);

        String bucket = documentsS3Properties.getBucket();
        if (bucket == null || bucket.isBlank()) {
            bucket = "not-configured";
        }

        String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());
        String s3Key = buildS3Key(ownerType, ownerId, sanitizedFilename);

        Document document = new Document();
        document.setOwnerType(ownerType);
        document.setOwnerId(ownerId);
        document.setCategory(category);
        document.setOriginalName(sanitizedFilename);
        document.setContentType(file.getContentType());
        document.setSizeBytes(file.getSize());
        document.setBucket(bucket);
        document.setS3Key(s3Key);
        document.setStatus(DocumentStatusEnum.UPLOADING);
        document.setIsActive(true);
        document.setCreatedBy(createdBy);

        try {
            document.setSha256(calculateSha256(file));
            documentRepository.saveAndFlush(document);
            storageService.uploadFile(document.getBucket(), document.getS3Key(), file.getInputStream(), file.getSize(), file.getContentType());
            document.setStatus(DocumentStatusEnum.AVAILABLE);
            documentRepository.save(document);
            log.info("document_upload_success ownerType={} ownerId={} documentId={} s3Key={}", ownerType, ownerId, document.getId(), document.getS3Key());
            return toResponseDTO(document);
        } catch (IOException | RuntimeException ex) {
            document.setStatus(DocumentStatusEnum.DELETED);
            document.setDeletedAt(LocalDateTime.now());
            documentRepository.save(document);
            log.error("document_upload_failed ownerType={} ownerId={} s3Key={} error={}", ownerType, ownerId, s3Key, ex.getMessage());
            throw new BusinessException("Falha ao realizar upload do documento");
        }
    }

    @Override
    public DocumentResponseDTO findById(UUID id) {
        Document document = documentRepository.findByIdAndStatusNot(id, DocumentStatusEnum.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Documento nao encontrado"));
        if (!Boolean.TRUE.equals(document.getIsActive())) {
            throw new ResourceNotFoundException("Documento nao encontrado");
        }
        return toResponseDTO(document);
    }

    @Override
    public DocumentDownloadUrlDTO generateDownloadUrl(UUID id, Duration expiresIn) {
        Document document = documentRepository.findByIdAndStatusNot(id, DocumentStatusEnum.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Documento nao encontrado"));
        if (!Boolean.TRUE.equals(document.getIsActive())) {
            throw new ResourceNotFoundException("Documento nao encontrado");
        }

        if (document.getStatus() != DocumentStatusEnum.AVAILABLE) {
            throw new BusinessException("Documento nao esta disponivel para download");
        }

        String url = storageService.generatePresignedDownloadUrl(document.getBucket(), document.getS3Key(), expiresIn).toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn.toSeconds());
        return new DocumentDownloadUrlDTO(url, expiresAt);
    }

    @Transactional
    @Override
    public void softDelete(UUID id) {
        Document document = documentRepository.findByIdAndStatusNot(id, DocumentStatusEnum.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Documento nao encontrado"));

        document.setStatus(DocumentStatusEnum.DELETED);
        document.setIsActive(false);
        document.setDeletedAt(LocalDateTime.now());
        documentRepository.save(document);
        log.info("document_soft_deleted documentId={} s3Key={}", document.getId(), document.getS3Key());
    }

    private void validateOwner(DocumentOwnerTypeEnum ownerType, Long ownerId) {
        if (ownerType == null || ownerId == null) {
            throw new BusinessException("ownerType e ownerId sao obrigatorios");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo obrigatorio");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException("Arquivo excede o tamanho maximo de 20MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("Tipo de arquivo nao permitido");
        }
    }

    private String buildS3Key(DocumentOwnerTypeEnum ownerType, Long ownerId, String sanitizedFilename) {
        return String.format("documents/%s/%d/%s-%s", ownerType.name(), ownerId, UUID.randomUUID(), sanitizedFilename);
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "arquivo";
        }
        String filename = originalFilename.replace("\\", "/");
        if (filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf('/') + 1);
        }
        filename = filename.replaceAll("[^A-Za-z0-9._-]", "_");
        if (filename.isBlank()) {
            return "arquivo";
        }
        return filename;
    }

    private String calculateSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = file.getBytes();
            byte[] hash = digest.digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | IOException ex) {
            return null;
        }
    }

    private DocumentResponseDTO toResponseDTO(Document document) {
        return new DocumentResponseDTO(
                document.getId(),
                document.getOwnerType(),
                document.getOwnerId(),
                document.getCategory(),
                document.getOriginalName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getSha256(),
                document.getBucket(),
                document.getS3Key(),
                document.getStatus(),
                document.getIsActive(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getCreatedBy(),
                document.getDeletedAt()
        );
    }
}
