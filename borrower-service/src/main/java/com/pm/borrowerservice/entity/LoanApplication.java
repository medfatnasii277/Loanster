package com.pm.borrowerservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Borrower borrower;

    @NotBlank(message = "Loan type is required")
    @Column(name = "loan_type", nullable = false)
    private String loanType;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.0", message = "Loan amount must be at least $1,000")
    @DecimalMax(value = "1000000.0", message = "Loan amount cannot exceed $1,000,000")
    @Column(name = "loan_amount", nullable = false)
    private BigDecimal loanAmount;

    @NotNull(message = "Loan term is required")
    @Min(value = 12, message = "Loan term must be at least 12 months")
    @Max(value = 360, message = "Loan term cannot exceed 360 months")
    @Column(name = "loan_term_months", nullable = false)
    private Integer loanTermMonths;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Interest rate must be positive")
    @DecimalMax(value = "25.0", message = "Interest rate cannot exceed 25%")
    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "monthly_payment")
    private BigDecimal monthlyPayment;

    @Column(name = "total_payment")
    private BigDecimal totalPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanApplicationStatus status;

    @Column(name = "application_number", unique = true)
    private String applicationNumber;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "status_updated_by")
    private String statusUpdatedBy; // Officer/User ID who last updated the status
    
    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt; // When the status was last updated

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = LoanApplicationStatus.PENDING;
        }
        if (applicationNumber == null) {
            applicationNumber = generateApplicationNumber();
        }
    }

    private String generateApplicationNumber() {
        return "LOAN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }
} 