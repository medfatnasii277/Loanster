package com.pm.officerservice.dto;

import com.pm.officerservice.model.LoanApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanStatusUpdateRequest {
    
    @NotNull(message = "New status is required")
    private LoanApplicationStatus newStatus;
    
    private String rejectionReason; // Optional, only for rejected loans
    
    @NotNull(message = "Updated by is required")
    private String updatedBy;
} 