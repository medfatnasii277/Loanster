package com.pm.officerservice.dto;

import com.pm.officerservice.model.DocumentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusUpdateRequest {
    
    @NotNull(message = "New status is required")
    private DocumentStatus newStatus;
    
    private String rejectionReason; // Optional, only for rejected documents
    
    @NotNull(message = "Updated by is required")
    private String updatedBy;
} 