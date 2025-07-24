package com.pm.officerservice.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentStatusTest {

    @Test
    void enumValues_ContainsExpectedStatuses() {
        // When
        DocumentStatus[] statuses = DocumentStatus.values();

        // Then
        assertThat(statuses).hasSize(4);
        assertThat(statuses).contains(
                DocumentStatus.PENDING,
                DocumentStatus.VERIFIED,
                DocumentStatus.REJECTED,
                DocumentStatus.EXPIRED
        );
    }

    @Test
    void getDisplayName_ReturnsCorrectDisplayNames() {
        assertThat(DocumentStatus.PENDING.getDisplayName()).isEqualTo("Pending Review");
        assertThat(DocumentStatus.VERIFIED.getDisplayName()).isEqualTo("Verified");
        assertThat(DocumentStatus.REJECTED.getDisplayName()).isEqualTo("Rejected");
        assertThat(DocumentStatus.EXPIRED.getDisplayName()).isEqualTo("Expired");
    }

    @Test
    void valueOf_ParsesCorrectly() {
        assertThat(DocumentStatus.valueOf("PENDING")).isEqualTo(DocumentStatus.PENDING);
        assertThat(DocumentStatus.valueOf("VERIFIED")).isEqualTo(DocumentStatus.VERIFIED);
        assertThat(DocumentStatus.valueOf("REJECTED")).isEqualTo(DocumentStatus.REJECTED);
        assertThat(DocumentStatus.valueOf("EXPIRED")).isEqualTo(DocumentStatus.EXPIRED);
    }

    @Test
    void toString_ReturnsEnumName() {
        assertThat(DocumentStatus.PENDING.toString()).isEqualTo("PENDING");
        assertThat(DocumentStatus.VERIFIED.toString()).isEqualTo("VERIFIED");
        assertThat(DocumentStatus.REJECTED.toString()).isEqualTo("REJECTED");
        assertThat(DocumentStatus.EXPIRED.toString()).isEqualTo("EXPIRED");
    }
}
