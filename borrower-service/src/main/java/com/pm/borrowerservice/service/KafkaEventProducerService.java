package com.pm.borrowerservice.service;

import com.pm.borrowerservice.events.BorrowerCreatedEvent;
import com.pm.borrowerservice.events.DocumentUploadEvent;
import com.pm.borrowerservice.events.LoanApplicationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducerService {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    @Value("${kafka.topics.borrower-created}")
    private String borrowerCreatedTopic;

    @Value("${kafka.topics.loan-application}")
    private String loanApplicationTopic;

    @Value("${kafka.topics.documents-upload}")
    private String documentsUploadTopic;

    public void publishBorrowerCreatedEvent(BorrowerCreatedEvent event) {
        try {
            String eventKey = "borrower-" + event.getBorrowerId();
            log.info("Publishing borrower created event for borrower ID: {}", event.getBorrowerId());
            
            byte[] eventData = event.toByteArray();
            CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(borrowerCreatedTopic, eventKey, eventData);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Successfully published borrower created event for borrower ID: {} to topic: {} with offset: {}",
                            event.getBorrowerId(), borrowerCreatedTopic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish borrower created event for borrower ID: {} to topic: {}",
                            event.getBorrowerId(), borrowerCreatedTopic, exception);
                }
            });
        } catch (Exception e) {
            log.error("Exception occurred while publishing borrower created event for borrower ID: {}", 
                     event.getBorrowerId(), e);
        }
    }

    public void publishLoanApplicationEvent(LoanApplicationEvent event) {
        try {
            String eventKey = "loan-application-" + event.getApplicationId();
            log.info("Publishing loan application event for application ID: {}", event.getApplicationId());
            
            byte[] eventData = event.toByteArray();
            CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(loanApplicationTopic, eventKey, eventData);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Successfully published loan application event for application ID: {} to topic: {} with offset: {}",
                            event.getApplicationId(), loanApplicationTopic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish loan application event for application ID: {} to topic: {}",
                            event.getApplicationId(), loanApplicationTopic, exception);
                }
            });
        } catch (Exception e) {
            log.error("Exception occurred while publishing loan application event for application ID: {}", 
                     event.getApplicationId(), e);
        }
    }

    public void publishDocumentUploadEvent(DocumentUploadEvent event) {
        try {
            String eventKey = "document-" + event.getDocumentId();
            log.info("Publishing document upload event for document ID: {}", event.getDocumentId());
            
            byte[] eventData = event.toByteArray();
            CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(documentsUploadTopic, eventKey, eventData);
            
            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    log.info("Successfully published document upload event for document ID: {} to topic: {} with offset: {}",
                            event.getDocumentId(), documentsUploadTopic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish document upload event for document ID: {} to topic: {}",
                            event.getDocumentId(), documentsUploadTopic, exception);
                }
            });
        } catch (Exception e) {
            log.error("Exception occurred while publishing document upload event for document ID: {}", 
                     event.getDocumentId(), e);
        }
    }
}
