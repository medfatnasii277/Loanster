package com.pm.borrowerservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.borrowerservice.entity.LoanApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanApplicationDto {

    private Long id;
    private Long borrowerId;
    private String borrowerName;
    
    @NotBlank(message = "Loan type is required")
    private String loanType;
    
    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.0", message = "Loan amount must be at least $1,000")
    private BigDecimal loanAmount;
    
    @NotNull(message = "Loan term in months is required")
    @Min(value = 1, message = "Loan term must be at least 1 month")
    private Integer loanTermMonths;
    
    private BigDecimal interestRate;
    private BigDecimal monthlyPayment;
    private BigDecimal totalPayment;
    private LoanApplicationStatus status;
    private String applicationNumber;
    
    @NotBlank(message = "Purpose is required")
    private String purpose;
    private String notes;
    private String approvedBy;
    private String rejectionReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
} 