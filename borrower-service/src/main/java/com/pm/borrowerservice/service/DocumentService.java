package com.pm.borrowerservice.service;

import com.pm.borrowerservice.entity.Borrower;
import com.pm.borrowerservice.entity.Document;
import com.pm.borrowerservice.entity.LoanApplication;
import com.pm.borrowerservice.repository.BorrowerRepository;
import com.pm.borrowerservice.repository.DocumentRepository;
import com.pm.borrowerservice.repository.LoanApplicationRepository;
import com.pm.borrowerservice.service.KafkaEventProducerService;
import com.pm.borrowerservice.util.EventMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final BorrowerRepository borrowerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final KafkaEventProducerService kafkaEventProducerService;
    private final EventMapper eventMapper;

    @Value("${app.file.upload.path}")
    private String uploadPath;

    @Value("${app.file.allowed-extensions}")
    private String allowedExtensions;

    @Value("${app.file.max-size-mb}")
    private int maxSizeMb;

    public List<Document> getDocumentsForBorrower(Long borrowerId) {
        return documentRepository.findByBorrowerId(borrowerId);
    }

    public Document getDocument(Long borrowerId, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));
        if (!document.getBorrower().getId().equals(borrowerId)) {
            throw new EntityNotFoundException("Document does not belong to this borrower");
        }
        return document;
    }

    public List<Document> getDocumentsForLoanApplication(Long loanApplicationId) {
        return documentRepository.findByLoanApplicationId(loanApplicationId);
    }

    @Transactional
    public Document uploadDocument(Long borrowerId, MultipartFile file, String documentType, String description, Long loanApplicationId) throws IOException {
        try {
            log.info("Uploading document for borrower ID: {} with type: {}", borrowerId, documentType);
            Borrower borrower = borrowerRepository.findById(borrowerId)
                    .orElseThrow(() -> new EntityNotFoundException("Borrower not found"));
            LoanApplication loanApplication = null;
            if (loanApplicationId != null) {
                loanApplication = loanApplicationRepository.findById(loanApplicationId)
                        .orElseThrow(() -> new EntityNotFoundException("Loan application not found"));
            }
            validateFile(file);
            String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path dir = Paths.get(uploadPath, String.valueOf(borrowerId));
            Files.createDirectories(dir);
            Path filePath = dir.resolve(fileName);
            file.transferTo(filePath);
            Document document = Document.builder()
                    .borrower(borrower)
                    .loanApplication(loanApplication)
                    .documentName(file.getOriginalFilename())
                    .documentType(documentType)
                    .filePath(filePath.toString())
                    .fileName(fileName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .description(description)
                    .status(com.pm.borrowerservice.entity.DocumentStatus.PENDING)
                    .build();
            document = documentRepository.save(document);
            kafkaEventProducerService.publishDocumentUploadEvent(eventMapper.toDocumentUploadEvent(document));
            log.info("Successfully uploaded document with ID: {} and published event", document.getId());
            return document;
        } catch (Exception e) {
            log.error("Error uploading document for borrower ID: {}", borrowerId, e);
            throw e;
        }
    }

    @Transactional
    public Document updateDocumentStatus(Long borrowerId, Long documentId, com.pm.borrowerservice.entity.DocumentStatus status, String officerId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));
        if (!document.getBorrower().getId().equals(borrowerId)) {
            throw new EntityNotFoundException("Document does not belong to this borrower");
        }
        document.setStatus(status);
        document.setStatusUpdatedBy(officerId);
        document.setStatusUpdatedAt(java.time.LocalDateTime.now());
        return documentRepository.save(document);
    }

    @Transactional
    public void deleteDocument(Long borrowerId, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));
        if (!document.getBorrower().getId().equals(borrowerId)) {
            throw new EntityNotFoundException("Document does not belong to this borrower");
        }
        File file = new File(document.getFilePath());
        if (file.exists()) {
            file.delete();
        }
        documentRepository.deleteById(documentId);
    }

    private void validateFile(MultipartFile file) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();
        Set<String> allowed = Set.of(allowedExtensions.split(","));
        if (!allowed.contains(ext)) {
            throw new IllegalArgumentException("File extension not allowed: " + ext);
        }
        if (file.getSize() > maxSizeMb * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum allowed: " + maxSizeMb + "MB");
        }
    }
} 