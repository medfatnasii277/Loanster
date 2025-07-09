package com.pm.officerservice.dto;

import com.pm.officerservice.model.Document;
import com.pm.officerservice.model.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    
    private Long documentId;
    private Long borrowerId;
    private String borrowerName;
    private Long loanApplicationId;
    private String documentType;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private DocumentStatus status;
    private LocalDateTime uploadedAtSource;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static DocumentResponse fromEntity(Document entity) {
        return DocumentResponse.builder()
                .documentId(entity.getDocumentId())
                .borrowerId(entity.getBorrower().getBorrowerId())
                .borrowerName(entity.getBorrower().getFirstName() + " " + entity.getBorrower().getLastName())
                .loanApplicationId(entity.getLoanApplication() != null ? entity.getLoanApplication().getApplicationId() : null)
                .documentType(entity.getDocumentType())
                .fileName(entity.getFileName())
                .filePath(entity.getFilePath())
                .fileSize(entity.getFileSize())
                .contentType(entity.getContentType())
                .status(entity.getStatus())
                .uploadedAtSource(entity.getUploadedAtSource())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
} 