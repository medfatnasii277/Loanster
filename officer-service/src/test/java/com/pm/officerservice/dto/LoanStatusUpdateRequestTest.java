package com.pm.officerservice.dto;

import com.pm.officerservice.model.LoanApplicationStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoanStatusUpdateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest_NoViolations() {
        // Given
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.APPROVED)
                .updatedBy("officer123")
                .build();

        // When
        Set<ConstraintViolation<LoanStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void validRequestWithRejectionReason_NoViolations() {
        // Given
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.REJECTED)
                .rejectionReason("Insufficient income")
                .updatedBy("officer123")
                .build();

        // When
        Set<ConstraintViolation<LoanStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void nullNewStatus_HasViolation() {
        // Given
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(null)
                .updatedBy("officer123")
                .build();

        // When
        Set<ConstraintViolation<LoanStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("New status is required");
    }

    @Test
    void blankUpdatedBy_HasViolation() {
        // Given
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.APPROVED)
                .updatedBy("")
                .build();

        // When
        Set<ConstraintViolation<LoanStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Updated by is required");
    }

    @Test
    void nullUpdatedBy_HasViolation() {
        // Given
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.APPROVED)
                .updatedBy(null)
                .build();

        // When
        Set<ConstraintViolation<LoanStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Updated by is required");
    }

    @Test
    void tooLongUpdatedBy_HasViolation() {
        // Given
        String longUpdatedBy = "a".repeat(101); // 101 characters
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.APPROVED)
                .updatedBy(longUpdatedBy)
                .build();

        // When
        Set<ConstraintViolation<LoanStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Updated by cannot exceed 100 characters");
    }

    @Test
    void tooLongRejectionReason_HasViolation() {
        // Given
        String longReason = "a".repeat(501); // 501 characters
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.REJECTED)
                .rejectionReason(longReason)
                .updatedBy("officer123")
                .build();

        // When
        Set<ConstraintViolation<LoanStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Rejection reason cannot exceed 500 characters");
    }

    @Test
    void builderAndAccessors_WorkCorrectly() {
        // Given
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.REJECTED)
                .rejectionReason("Credit score too low")
                .updatedBy("officer456")
                .build();

        // Then
        assertThat(request.getNewStatus()).isEqualTo(LoanApplicationStatus.REJECTED);
        assertThat(request.getRejectionReason()).isEqualTo("Credit score too low");
        assertThat(request.getUpdatedBy()).isEqualTo("officer456");
    }

    @Test
    void equalsAndHashCode_WorkCorrectly() {
        // Given
        LoanStatusUpdateRequest request1 = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.APPROVED)
                .updatedBy("officer123")
                .build();

        LoanStatusUpdateRequest request2 = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.APPROVED)
                .updatedBy("officer123")
                .build();

        LoanStatusUpdateRequest request3 = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.REJECTED)
                .updatedBy("officer123")
                .build();

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1).isNotEqualTo(request3);
    }
}
