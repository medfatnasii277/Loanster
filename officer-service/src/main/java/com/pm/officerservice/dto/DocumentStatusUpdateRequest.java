package com.pm.officerservice.dto;

import com.pm.officerservice.model.DocumentStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    
    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String rejectionReason; // Optional, only for rejected documents
    
    @NotBlank(message = "Updated by is required")
    @Size(max = 100, message = "Updated by cannot exceed 100 characters")
    private String updatedBy; // Officer/User ID who is making this change
} 