package com.pm.officerservice.dto;

import com.pm.officerservice.model.DocumentStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentStatusUpdateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest_NoViolations() {
        // Given
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.VERIFIED)
                .updatedBy("officer123")
                .build();

        // When
        Set<ConstraintViolation<DocumentStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void validRequestWithRejectionReason_NoViolations() {
        // Given
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.REJECTED)
                .rejectionReason("Poor quality document")
                .updatedBy("officer123")
                .build();

        // When
        Set<ConstraintViolation<DocumentStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void nullNewStatus_HasViolation() {
        // Given
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(null)
                .updatedBy("officer123")
                .build();

        // When
        Set<ConstraintViolation<DocumentStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("New status is required");
    }

    @Test
    void blankUpdatedBy_HasViolation() {
        // Given
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.VERIFIED)
                .updatedBy("")
                .build();

        // When
        Set<ConstraintViolation<DocumentStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Updated by is required");
    }

    @Test
    void nullUpdatedBy_HasViolation() {
        // Given
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.VERIFIED)
                .updatedBy(null)
                .build();

        // When
        Set<ConstraintViolation<DocumentStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Updated by is required");
    }

    @Test
    void tooLongUpdatedBy_HasViolation() {
        // Given
        String longUpdatedBy = "a".repeat(101); // 101 characters
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.VERIFIED)
                .updatedBy(longUpdatedBy)
                .build();

        // When
        Set<ConstraintViolation<DocumentStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Updated by cannot exceed 100 characters");
    }

    @Test
    void tooLongRejectionReason_HasViolation() {
        // Given
        String longReason = "a".repeat(501); // 501 characters
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.REJECTED)
                .rejectionReason(longReason)
                .updatedBy("officer123")
                .build();

        // When
        Set<ConstraintViolation<DocumentStatusUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Rejection reason cannot exceed 500 characters");
    }

    @Test
    void builderAndAccessors_WorkCorrectly() {
        // Given
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.REJECTED)
                .rejectionReason("Document is expired")
                .updatedBy("officer456")
                .build();

        // Then
        assertThat(request.getNewStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(request.getRejectionReason()).isEqualTo("Document is expired");
        assertThat(request.getUpdatedBy()).isEqualTo("officer456");
    }

    @Test
    void equalsAndHashCode_WorkCorrectly() {
        // Given
        DocumentStatusUpdateRequest request1 = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.VERIFIED)
                .updatedBy("officer123")
                .build();

        DocumentStatusUpdateRequest request2 = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.VERIFIED)
                .updatedBy("officer123")
                .build();

        DocumentStatusUpdateRequest request3 = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.REJECTED)
                .updatedBy("officer123")
                .build();

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1).isNotEqualTo(request3);
    }
}
