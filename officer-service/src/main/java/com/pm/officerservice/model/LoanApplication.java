package com.pm.officerservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    private Long applicationId; // Matches ID from borrower-service

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @Column(name = "loan_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "loan_term_months", nullable = false)
    private Integer loanTermMonths;

    @Column(name = "loan_purpose")
    private String loanPurpose;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "monthly_payment", precision = 19, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "status_updated_by")
    private String statusUpdatedBy; // Officer/User ID who last updated the status
    
    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt; // When the status was last updated

    @Column(name = "applied_at_source", nullable = false)
    private LocalDateTime appliedAtSource;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loanApplication", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents;
}
