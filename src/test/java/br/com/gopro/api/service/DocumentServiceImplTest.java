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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private DocumentsS3Properties documentsS3Properties;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void upload_shouldCreateDocumentUploadAndReturnAvailable() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contrato.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        when(documentsS3Properties.getBucket()).thenReturn("private-bucket");
        when(documentRepository.saveAndFlush(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponseDTO result = documentService.upload(file, DocumentOwnerTypeEnum.PROJECT, 10L, "contrato", 1L);

        assertThat(result.status()).isEqualTo(DocumentStatusEnum.AVAILABLE);
        assertThat(result.bucket()).isEqualTo("private-bucket");
        assertThat(result.s3Key()).startsWith("documents/PROJECT/10/");

        verify(storageService).uploadFile(eq("private-bucket"), anyString(), any(), eq(file.getSize()), eq("application/pdf"));
    }

    @Test
    void upload_shouldThrowBusinessException_whenFileTooLarge() {
        byte[] data = new byte[(int) (20L * 1024L * 1024L + 1L)];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contrato.pdf",
                "application/pdf",
                data
        );

        assertThatThrownBy(() -> documentService.upload(file, DocumentOwnerTypeEnum.PROJECT, 10L, null, 1L))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(documentRepository, storageService);
    }

    @Test
    void upload_shouldMarkDeleted_whenStorageFails() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contrato.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        when(documentsS3Properties.getBucket()).thenReturn("private-bucket");
        when(documentRepository.saveAndFlush(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("s3 error")).when(storageService)
                .uploadFile(eq("private-bucket"), anyString(), any(), eq(file.getSize()), eq("application/pdf"));

        assertThatThrownBy(() -> documentService.upload(file, DocumentOwnerTypeEnum.PROJECT, 10L, null, 1L))
                .isInstanceOf(BusinessException.class);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(DocumentStatusEnum.DELETED);
    }

    @Test
    void generateDownloadUrl_shouldReturnPresignedUrl_whenAvailable() {
        UUID id = UUID.randomUUID();
        Document document = new Document();
        document.setId(id);
        document.setStatus(DocumentStatusEnum.AVAILABLE);
        document.setBucket("private-bucket");
        document.setS3Key("documents/PROJECT/10/x.pdf");

        when(documentRepository.findByIdAndStatusNot(id, DocumentStatusEnum.DELETED)).thenReturn(Optional.of(document));
        when(storageService.generatePresignedDownloadUrl("private-bucket", "documents/PROJECT/10/x.pdf", Duration.ofMinutes(10)))
                .thenReturn(URI.create("https://signed-url"));

        DocumentDownloadUrlDTO result = documentService.generateDownloadUrl(id, Duration.ofMinutes(10));

        assertThat(result.url()).isEqualTo("https://signed-url");
    }

    @Test
    void generateDownloadUrl_shouldThrowBusinessException_whenNotAvailable() {
        UUID id = UUID.randomUUID();
        Document document = new Document();
        document.setId(id);
        document.setStatus(DocumentStatusEnum.UPLOADING);

        when(documentRepository.findByIdAndStatusNot(id, DocumentStatusEnum.DELETED)).thenReturn(Optional.of(document));

        assertThatThrownBy(() -> documentService.generateDownloadUrl(id, Duration.ofMinutes(10)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void softDelete_shouldMarkDeleted() {
        UUID id = UUID.randomUUID();
        Document document = new Document();
        document.setId(id);
        document.setStatus(DocumentStatusEnum.AVAILABLE);

        when(documentRepository.findByIdAndStatusNot(id, DocumentStatusEnum.DELETED)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        documentService.softDelete(id);

        assertThat(document.getStatus()).isEqualTo(DocumentStatusEnum.DELETED);
        assertThat(document.getDeletedAt()).isNotNull();
    }

    @Test
    void findById_shouldThrowNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(documentRepository.findByIdAndStatusNot(id, DocumentStatusEnum.DELETED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
