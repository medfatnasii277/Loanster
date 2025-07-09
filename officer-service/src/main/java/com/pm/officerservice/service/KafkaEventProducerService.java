package com.pm.officerservice.service;

import com.pm.officerservice.events.DocumentStatusUpdateEvent;
import com.pm.officerservice.events.LoanStatusUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducerService {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    @Value("${kafka.topics.loan-status:loan-status}")
    private String loanStatusTopic;

    @Value("${kafka.topics.document-status:documents-status}")
    private String documentStatusTopic;

    public void publishLoanStatusUpdateEvent(LoanStatusUpdateEvent event) {
        try {
            String key = "loan-status-" + event.getApplicationId();
            CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(loanStatusTopic, key, event.toByteArray());
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published loan status update event for application ID: {} to topic: {}", 
                            event.getApplicationId(), loanStatusTopic);
                } else {
                    log.error("Failed to publish loan status update event for application ID: {}", 
                            event.getApplicationId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing loan status update event for application ID: {}", 
                    event.getApplicationId(), e);
            throw e;
        }
    }

    public void publishDocumentStatusUpdateEvent(DocumentStatusUpdateEvent event) {
        try {
            String key = "document-status-" + event.getDocumentId();
            CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(documentStatusTopic, key, event.toByteArray());
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published document status update event for document ID: {} to topic: {}", 
                            event.getDocumentId(), documentStatusTopic);
                } else {
                    log.error("Failed to publish document status update event for document ID: {}", 
                            event.getDocumentId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing document status update event for document ID: {}", 
                    event.getDocumentId(), e);
            throw e;
        }
    }

    public String generateEventId() {
        return UUID.randomUUID().toString();
    }
} 