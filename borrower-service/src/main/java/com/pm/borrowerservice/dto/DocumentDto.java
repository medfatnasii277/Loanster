package com.pm.borrowerservice.dto;

import com.pm.borrowerservice.entity.Document;
import com.pm.borrowerservice.entity.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long id;
    private Long borrowerId;
    private Long loanApplicationId;
    private String documentName;
    private String documentType;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String description;
    private DocumentStatus status;
    private String statusUpdatedBy;
    private LocalDateTime statusUpdatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static DocumentDto fromEntity(Document document) {
        return DocumentDto.builder()
                .id(document.getId())
                .borrowerId(document.getBorrower() != null ? document.getBorrower().getId() : null)
                .loanApplicationId(document.getLoanApplication() != null ? document.getLoanApplication().getId() : null)
                .documentName(document.getDocumentName())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileSize(document.getFileSize())
                .contentType(document.getContentType())
                .description(document.getDescription())
                .status(document.getStatus())
                .statusUpdatedBy(document.getStatusUpdatedBy())
                .statusUpdatedAt(document.getStatusUpdatedAt())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
