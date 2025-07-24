package com.pm.officerservice.dto;

import com.pm.officerservice.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LoanApplicationResponseTest {

    private Borrower borrower;
    private LoanApplication loanApplication;

    @BeforeEach
    void setUp() {
        borrower = Borrower.builder()
                .borrowerId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .dateOfBirth("1990-01-01")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .annualIncome(50000.0)
                .employmentStatus("EMPLOYED")
                .employerName("ABC Corp")
                .employmentYears(5)
                .createdAtSource(LocalDateTime.now())
                .build();

        loanApplication = LoanApplication.builder()
                .applicationId(1L)
                .borrower(borrower)
                .loanAmount(BigDecimal.valueOf(100000))
                .loanTermMonths(36)
                .loanPurpose("Home Purchase")
                .interestRate(BigDecimal.valueOf(5.5))
                .monthlyPayment(BigDecimal.valueOf(3000))
                .status("PENDING")
                .statusUpdatedBy("officer123")
                .statusUpdatedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .appliedAtSource(LocalDateTime.of(2024, 12, 1, 9, 0))
                .createdAt(LocalDateTime.of(2024, 12, 1, 9, 30))
                .updatedAt(LocalDateTime.of(2025, 1, 1, 10, 30))
                .build();
    }

    @Test
    void fromEntity_CreatesCorrectResponse() {
        // When
        LoanApplicationResponse response = LoanApplicationResponse.fromEntity(loanApplication);

        // Then
        assertThat(response.getApplicationId()).isEqualTo(1L);
        assertThat(response.getBorrowerId()).isEqualTo(1L);
        assertThat(response.getBorrowerName()).isEqualTo("John Doe");
        assertThat(response.getLoanAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(response.getLoanTermMonths()).isEqualTo(36);
        assertThat(response.getLoanPurpose()).isEqualTo("Home Purchase");
        assertThat(response.getInterestRate()).isEqualByComparingTo(BigDecimal.valueOf(5.5));
        assertThat(response.getMonthlyPayment()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(response.getStatus()).isEqualTo(LoanApplicationStatus.PENDING);
        assertThat(response.getStatusUpdatedBy()).isEqualTo("officer123");
        assertThat(response.getStatusUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 0));
        assertThat(response.getAppliedAtSource()).isEqualTo(LocalDateTime.of(2024, 12, 1, 9, 0));
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 12, 1, 9, 30));
        assertThat(response.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 10, 30));
    }

    @Test
    void builderAndAccessors_WorkCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        LoanApplicationResponse response = LoanApplicationResponse.builder()
                .applicationId(2L)
                .borrowerId(2L)
                .borrowerName("Jane Smith")
                .loanAmount(BigDecimal.valueOf(75000))
                .loanTermMonths(24)
                .loanPurpose("Car Purchase")
                .interestRate(BigDecimal.valueOf(4.5))
                .monthlyPayment(BigDecimal.valueOf(2500))
                .status(LoanApplicationStatus.APPROVED)
                .statusUpdatedBy("officer456")
                .statusUpdatedAt(now)
                .appliedAtSource(now.minusDays(1))
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .build();

        // Then
        assertThat(response.getApplicationId()).isEqualTo(2L);
        assertThat(response.getBorrowerId()).isEqualTo(2L);
        assertThat(response.getBorrowerName()).isEqualTo("Jane Smith");
        assertThat(response.getLoanAmount()).isEqualByComparingTo(BigDecimal.valueOf(75000));
        assertThat(response.getLoanTermMonths()).isEqualTo(24);
        assertThat(response.getLoanPurpose()).isEqualTo("Car Purchase");
        assertThat(response.getInterestRate()).isEqualByComparingTo(BigDecimal.valueOf(4.5));
        assertThat(response.getMonthlyPayment()).isEqualByComparingTo(BigDecimal.valueOf(2500));
        assertThat(response.getStatus()).isEqualTo(LoanApplicationStatus.APPROVED);
        assertThat(response.getStatusUpdatedBy()).isEqualTo("officer456");
        assertThat(response.getStatusUpdatedAt()).isEqualTo(now);
        assertThat(response.getAppliedAtSource()).isEqualTo(now.minusDays(1));
        assertThat(response.getCreatedAt()).isEqualTo(now.minusDays(1));
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void equalsAndHashCode_WorkCorrectly() {
        // Given
        LoanApplicationResponse response1 = LoanApplicationResponse.fromEntity(loanApplication);
        LoanApplicationResponse response2 = LoanApplicationResponse.fromEntity(loanApplication);
        
        LoanApplication differentLoanApplication = LoanApplication.builder()
                .applicationId(2L)
                .borrower(borrower)
                .loanAmount(BigDecimal.valueOf(50000))
                .loanTermMonths(24)
                .loanPurpose("Car Purchase")
                .interestRate(BigDecimal.valueOf(4.5))
                .monthlyPayment(BigDecimal.valueOf(2000))
                .status("APPROVED")
                .appliedAtSource(LocalDateTime.now())
                .build();
        LoanApplicationResponse response3 = LoanApplicationResponse.fromEntity(differentLoanApplication);

        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        assertThat(response1).isNotEqualTo(response3);
    }

    @Test
    void toString_ContainsRelevantInfo() {
        // Given
        LoanApplicationResponse response = LoanApplicationResponse.fromEntity(loanApplication);

        // When
        String toString = response.toString();

        // Then
        assertThat(toString).contains("applicationId=1");
        assertThat(toString).contains("borrowerName=John Doe");
        assertThat(toString).contains("loanAmount=100000");
        assertThat(toString).contains("status=PENDING");
    }
}
