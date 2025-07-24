package com.pm.officerservice.service;

import com.pm.officerservice.events.DocumentStatusUpdateEvent;
import com.pm.officerservice.events.LoanStatusUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEventProducerServiceTest {

    @Mock
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @InjectMocks
    private KafkaEventProducerService kafkaEventProducerService;

    private LoanStatusUpdateEvent loanStatusUpdateEvent;
    private DocumentStatusUpdateEvent documentStatusUpdateEvent;

    @BeforeEach
    void setUp() {
        // Set the topics using reflection
        ReflectionTestUtils.setField(kafkaEventProducerService, "loanStatusTopic", "loan-status");
        ReflectionTestUtils.setField(kafkaEventProducerService, "documentStatusTopic", "document-status");

        loanStatusUpdateEvent = LoanStatusUpdateEvent.newBuilder()
                .setApplicationId(1L)
                .setBorrowerId(1L)
                .setOldStatus("PENDING")
                .setNewStatus("APPROVED")
                .setUpdatedBy("officer123")
                .setUpdatedAt("2025-01-01T10:00:00")
                .setEventId("event-123")
                .setEventTimestamp("2025-01-01T10:00:00")
                .build();

        documentStatusUpdateEvent = DocumentStatusUpdateEvent.newBuilder()
                .setDocumentId(1L)
                .setBorrowerId(1L)
                .setLoanApplicationId(1L)
                .setOldStatus("PENDING")
                .setNewStatus("VERIFIED")
                .setUpdatedBy("officer123")
                .setUpdatedAt("2025-01-01T10:00:00")
                .setEventId("event-123")
                .setEventTimestamp("2025-01-01T10:00:00")
                .build();
    }

    @Test
    void publishLoanStatusUpdateEvent_Success() {
        // Given
        CompletableFuture<SendResult<String, byte[]>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("loan-status"), anyString(), any(byte[].class))).thenReturn(future);

        // When
        kafkaEventProducerService.publishLoanStatusUpdateEvent(loanStatusUpdateEvent);

        // Then
        verify(kafkaTemplate).send(eq("loan-status"), eq("loan-status-1"), any(byte[].class));
    }

    @Test
    void publishLoanStatusUpdateEvent_KafkaException_ThrowsException() {
        // Given
        when(kafkaTemplate.send(anyString(), anyString(), any(byte[].class)))
                .thenThrow(new RuntimeException("Kafka error"));

        // When & Then
        assertThatThrownBy(() -> kafkaEventProducerService.publishLoanStatusUpdateEvent(loanStatusUpdateEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kafka error");
    }

    @Test
    void publishDocumentStatusUpdateEvent_Success() {
        // Given
        CompletableFuture<SendResult<String, byte[]>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("document-status"), anyString(), any(byte[].class))).thenReturn(future);

        // When
        kafkaEventProducerService.publishDocumentStatusUpdateEvent(documentStatusUpdateEvent);

        // Then
        verify(kafkaTemplate).send(eq("document-status"), eq("document-status-1"), any(byte[].class));
    }

    @Test
    void publishDocumentStatusUpdateEvent_KafkaException_ThrowsException() {
        // Given
        when(kafkaTemplate.send(anyString(), anyString(), any(byte[].class)))
                .thenThrow(new RuntimeException("Kafka error"));

        // When & Then
        assertThatThrownBy(() -> kafkaEventProducerService.publishDocumentStatusUpdateEvent(documentStatusUpdateEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kafka error");
    }

    @Test
    void generateEventId_ReturnsUUID() {
        // When
        String eventId1 = kafkaEventProducerService.generateEventId();
        String eventId2 = kafkaEventProducerService.generateEventId();

        // Then
        assertThat(eventId1).isNotNull();
        assertThat(eventId2).isNotNull();
        assertThat(eventId1).isNotEqualTo(eventId2);
        assertThat(eventId1).hasSize(36); // UUID length
        assertThat(eventId2).hasSize(36); // UUID length
    }
}
