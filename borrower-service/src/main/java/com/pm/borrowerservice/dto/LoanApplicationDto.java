package com.pm.borrowerservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.pm.borrowerservice.entity.LoanApplicationStatus;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanApplicationDto {

    private Long id;
    private Long borrowerId;
    private String borrowerName;
    private String loanType;
    private BigDecimal loanAmount;
    private Integer loanTermMonths;
    private BigDecimal interestRate;
    private BigDecimal monthlyPayment;
    private BigDecimal totalPayment;
    private LoanApplicationStatus status;
    private String applicationNumber;
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