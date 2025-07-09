package com.pm.officerservice.dto;

import com.pm.officerservice.model.LoanApplication;
import com.pm.officerservice.model.LoanApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {
    
    private Long applicationId;
    private Long borrowerId;
    private String borrowerName;
    private BigDecimal loanAmount;
    private Integer loanTermMonths;
    private String loanPurpose;
    private BigDecimal interestRate;
    private BigDecimal monthlyPayment;
    private LoanApplicationStatus status;
    private LocalDateTime appliedAtSource;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static LoanApplicationResponse fromEntity(LoanApplication entity) {
        return LoanApplicationResponse.builder()
                .applicationId(entity.getApplicationId())
                .borrowerId(entity.getBorrower().getBorrowerId())
                .borrowerName(entity.getBorrower().getFirstName() + " " + entity.getBorrower().getLastName())
                .loanAmount(entity.getLoanAmount())
                .loanTermMonths(entity.getLoanTermMonths())
                .loanPurpose(entity.getLoanPurpose())
                .interestRate(entity.getInterestRate())
                .monthlyPayment(entity.getMonthlyPayment())
                .status(LoanApplicationStatus.valueOf(entity.getStatus()))
                .appliedAtSource(entity.getAppliedAtSource())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
} 