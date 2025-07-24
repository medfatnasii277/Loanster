package com.pm.officerservice.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoanApplicationStatusTest {

    @Test
    void enumValues_ContainsExpectedStatuses() {
        // When
        LoanApplicationStatus[] statuses = LoanApplicationStatus.values();

        // Then
        assertThat(statuses).hasSize(7);
        assertThat(statuses).contains(
                LoanApplicationStatus.PENDING,
                LoanApplicationStatus.UNDER_REVIEW,
                LoanApplicationStatus.DOCUMENTS_REQUIRED,
                LoanApplicationStatus.APPROVED,
                LoanApplicationStatus.REJECTED,
                LoanApplicationStatus.CANCELLED,
                LoanApplicationStatus.FUNDED
        );
    }

    @Test
    void getDisplayName_ReturnsCorrectDisplayNames() {
        assertThat(LoanApplicationStatus.PENDING.getDisplayName()).isEqualTo("Pending Review");
        assertThat(LoanApplicationStatus.UNDER_REVIEW.getDisplayName()).isEqualTo("Under Review");
        assertThat(LoanApplicationStatus.DOCUMENTS_REQUIRED.getDisplayName()).isEqualTo("Documents Required");
        assertThat(LoanApplicationStatus.APPROVED.getDisplayName()).isEqualTo("Approved");
        assertThat(LoanApplicationStatus.REJECTED.getDisplayName()).isEqualTo("Rejected");
        assertThat(LoanApplicationStatus.CANCELLED.getDisplayName()).isEqualTo("Cancelled");
        assertThat(LoanApplicationStatus.FUNDED.getDisplayName()).isEqualTo("Funded");
    }

    @Test
    void valueOf_ParsesCorrectly() {
        assertThat(LoanApplicationStatus.valueOf("PENDING")).isEqualTo(LoanApplicationStatus.PENDING);
        assertThat(LoanApplicationStatus.valueOf("APPROVED")).isEqualTo(LoanApplicationStatus.APPROVED);
        assertThat(LoanApplicationStatus.valueOf("REJECTED")).isEqualTo(LoanApplicationStatus.REJECTED);
        assertThat(LoanApplicationStatus.valueOf("FUNDED")).isEqualTo(LoanApplicationStatus.FUNDED);
    }

    @Test
    void toString_ReturnsEnumName() {
        assertThat(LoanApplicationStatus.PENDING.toString()).isEqualTo("PENDING");
        assertThat(LoanApplicationStatus.APPROVED.toString()).isEqualTo("APPROVED");
        assertThat(LoanApplicationStatus.REJECTED.toString()).isEqualTo("REJECTED");
    }
}
