package com.pm.officerservice.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.borrowerservice.events.BorrowerCreatedEvent;
import com.pm.borrowerservice.events.DocumentUploadEvent;
import com.pm.borrowerservice.events.LoanApplicationEvent;
import com.pm.officerservice.service.BorrowerService;
import com.pm.officerservice.service.DocumentService;
import com.pm.officerservice.service.LoanApplicationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventListener {

    private static final Logger log = LoggerFactory.getLogger(EventListener.class);

    private final BorrowerService borrowerService;
    private final LoanApplicationService loanApplicationService;
    private final DocumentService documentService;

    @KafkaListener(topics = "${kafka.topics.borrower-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleBorrowerCreatedEvent(
            @Payload byte[] eventData,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received borrower created event from topic: {}, key: {}", topic, key);
            
            BorrowerCreatedEvent event = BorrowerCreatedEvent.parseFrom(eventData);
            log.debug("Parsed borrower created event: borrowerId={}, email={}", 
                     event.getBorrowerId(), event.getEmail());
            
            borrowerService.processBorrowerCreatedEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed borrower created event for borrowerId: {}", event.getBorrowerId());
            
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse borrower created event from topic: {}, key: {}", topic, key, e);
            acknowledgment.acknowledge(); // Acknowledge to avoid infinite retry
        } catch (Exception e) {
            log.error("Failed to process borrower created event from topic: {}, key: {}", topic, key, e);
            acknowledgment.acknowledge(); // For now, acknowledge all to avoid blocking
        }
    }

    @KafkaListener(topics = "${kafka.topics.loan-application}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleLoanApplicationEvent(
            @Payload byte[] eventData,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received loan application event from topic: {}, key: {}", topic, key);
            
            LoanApplicationEvent event = LoanApplicationEvent.parseFrom(eventData);
            log.debug("Parsed loan application event: applicationId={}, borrowerId={}, amount={}", 
                     event.getApplicationId(), event.getBorrowerId(), event.getLoanAmount());
            
            loanApplicationService.processLoanApplicationEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed loan application event for applicationId: {}", event.getApplicationId());
            
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse loan application event from topic: {}, key: {}", topic, key, e);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process loan application event from topic: {}, key: {}", topic, key, e);
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = "${kafka.topics.documents-upload}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleDocumentUploadEvent(
            @Payload byte[] eventData,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received document upload event from topic: {}, key: {}", topic, key);
            
            DocumentUploadEvent event = DocumentUploadEvent.parseFrom(eventData);
            log.debug("Parsed document upload event: documentId={}, borrowerId={}, fileName={}", 
                     event.getDocumentId(), event.getBorrowerId(), event.getFileName());
            
            documentService.processDocumentUploadEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed document upload event for documentId: {}", event.getDocumentId());
            
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed to parse document upload event from topic: {}, key: {}", topic, key, e);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process document upload event from topic: {}, key: {}", topic, key, e);
            acknowledgment.acknowledge();
        }
    }
}
