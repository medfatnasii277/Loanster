package com.pm.borrowerservice.service;

import com.pm.borrowerservice.entity.Borrower;
import com.pm.borrowerservice.entity.Document;
import com.pm.borrowerservice.entity.DocumentStatus;
import com.pm.borrowerservice.entity.LoanApplication;
import com.pm.borrowerservice.repository.BorrowerRepository;
import com.pm.borrowerservice.repository.DocumentRepository;
import com.pm.borrowerservice.repository.LoanApplicationRepository;
import com.pm.borrowerservice.util.EventMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private KafkaEventProducerService kafkaEventProducerService;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private DocumentService documentService;

    @TempDir
    Path tempDir;

    private Borrower borrower;
    private LoanApplication loanApplication;
    private Document document;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // Set up test upload path
        ReflectionTestUtils.setField(documentService, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(documentService, "allowedExtensions", "pdf,jpg,jpeg,png,txt");
        ReflectionTestUtils.setField(documentService, "maxSizeMb", 5);

        borrower = new Borrower();
        borrower.setId(1L);
        borrower.setFirstName("John");
        borrower.setLastName("Doe");
        borrower.setEmail("john.doe@example.com");

        loanApplication = new LoanApplication();
        loanApplication.setId(1L);
        loanApplication.setBorrower(borrower);

        document = new Document();
        document.setId(1L);
        document.setBorrower(borrower);
        document.setLoanApplication(loanApplication);
        document.setDocumentName("test-document.pdf");
        document.setDocumentType("ID");
        document.setFileName("test_document_123.pdf");
        document.setFilePath(tempDir.resolve("1/test_document_123.pdf").toString());
        document.setFileSize(1024L);
        document.setContentType("application/pdf");
        document.setDescription("Test document");
        document.setStatus(DocumentStatus.PENDING);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        mockFile = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "Test PDF content".getBytes()
        );
    }

    @Test
    void getDocumentsForBorrower_ShouldReturnDocumentsList() {
        // Arrange
        List<Document> documents = Arrays.asList(document);
        when(documentRepository.findByBorrowerId(1L)).thenReturn(documents);

        // Act
        List<Document> result = documentService.getDocumentsForBorrower(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(document.getId(), result.get(0).getId());

        verify(documentRepository).findByBorrowerId(1L);
    }

    @Test
    void getDocumentsForLoanApplication_ShouldReturnDocumentsList() {
        // Arrange
        List<Document> documents = Arrays.asList(document);
        when(documentRepository.findByLoanApplicationId(1L)).thenReturn(documents);

        // Act
        List<Document> result = documentService.getDocumentsForLoanApplication(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(document.getId(), result.get(0).getId());

        verify(documentRepository).findByLoanApplicationId(1L);
    }

    @Test
    void uploadDocument_ShouldUploadAndReturnDocument() throws IOException {
        // Arrange
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanApplication));
        when(documentRepository.save(any(Document.class))).thenReturn(document);
        when(eventMapper.toDocumentUploadEvent(any(Document.class))).thenReturn(null);

        // Act
        Document result = documentService.uploadDocument(1L, mockFile, "ID", "Test document", 1L);

        // Assert
        assertNotNull(result);
        assertEquals(document.getId(), result.getId());

        verify(borrowerRepository).findById(1L);
        verify(loanApplicationRepository).findById(1L);
        verify(documentRepository).save(any(Document.class));
        verify(kafkaEventProducerService).publishDocumentUploadEvent(any());
    }

    @Test
    void uploadDocument_ShouldThrowException_WhenBorrowerNotFound() {
        // Arrange
        when(borrowerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            documentService.uploadDocument(1L, mockFile, "ID", "Test document", null);
        });

        verify(borrowerRepository).findById(1L);
        verifyNoInteractions(documentRepository);
        verifyNoInteractions(kafkaEventProducerService);
    }

    @Test
    void uploadDocument_ShouldThrowException_WhenLoanApplicationNotFound() {
        // Arrange
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            documentService.uploadDocument(1L, mockFile, "ID", "Test document", 1L);
        });

        verify(borrowerRepository).findById(1L);
        verify(loanApplicationRepository).findById(1L);
        verifyNoInteractions(documentRepository);
        verifyNoInteractions(kafkaEventProducerService);
    }

    @Test
    void uploadDocument_ShouldWorkWithoutLoanApplication() throws IOException {
        // Arrange
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(documentRepository.save(any(Document.class))).thenReturn(document);
        when(eventMapper.toDocumentUploadEvent(any(Document.class))).thenReturn(null);

        // Act
        Document result = documentService.uploadDocument(1L, mockFile, "ID", "Test document", null);

        // Assert
        assertNotNull(result);
        verify(borrowerRepository).findById(1L);
        verifyNoInteractions(loanApplicationRepository);
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void deleteDocument_ShouldDeleteDocument() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        // Act
        documentService.deleteDocument(1L, 1L);

        // Assert
        verify(documentRepository).findById(1L);
        verify(documentRepository).deleteById(1L);
    }

    @Test
    void deleteDocument_ShouldThrowException_WhenDocumentNotFound() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            documentService.deleteDocument(1L, 1L);
        });

        verify(documentRepository).findById(1L);
        verify(documentRepository, never()).deleteById(any());
    }

    @Test
    void deleteDocument_ShouldThrowException_WhenBorrowerMismatch() {
        // Arrange
        Borrower anotherBorrower = new Borrower();
        anotherBorrower.setId(2L);
        
        Document documentWithDifferentBorrower = new Document();
        documentWithDifferentBorrower.setId(1L);
        documentWithDifferentBorrower.setBorrower(anotherBorrower);

        when(documentRepository.findById(1L)).thenReturn(Optional.of(documentWithDifferentBorrower));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            documentService.deleteDocument(1L, 1L);
        });

        assertTrue(exception.getMessage().contains("Document does not belong to this borrower"));
        verify(documentRepository).findById(1L);
        verify(documentRepository, never()).deleteById(any());
    }

    @Test
    void uploadDocument_ShouldThrowException_WhenFileEmpty() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            documentService.uploadDocument(1L, emptyFile, "ID", "Test document", null);
        });
    }

    @Test
    void uploadDocument_ShouldThrowException_WhenFileTooBig() {
        // Arrange
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                largeContent
        );

        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            documentService.uploadDocument(1L, largeFile, "ID", "Test document", null);
        });
    }

    @Test
    void uploadDocument_ShouldThrowException_WhenInvalidFileExtension() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "Malicious content".getBytes()
        );

        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            documentService.uploadDocument(1L, invalidFile, "ID", "Test document", null);
        });
    }
}
