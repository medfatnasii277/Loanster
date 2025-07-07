package com.pm.borrowerservice.util;

import com.pm.borrowerservice.entity.Borrower;
import com.pm.borrowerservice.entity.Document;
import com.pm.borrowerservice.entity.LoanApplication;
import com.pm.borrowerservice.events.BorrowerCreatedEvent;
import com.pm.borrowerservice.events.DocumentUploadEvent;
import com.pm.borrowerservice.events.LoanApplicationEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class EventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public BorrowerCreatedEvent toBorrowerCreatedEvent(Borrower borrower) {
        return BorrowerCreatedEvent.newBuilder()
                .setBorrowerId(borrower.getId())
                .setFirstName(borrower.getFirstName())
                .setLastName(borrower.getLastName())
                .setEmail(borrower.getEmail())
                .setPhoneNumber(borrower.getPhoneNumber())
                .setDateOfBirth(borrower.getDateOfBirth())
                .setSsn(borrower.getSsn())
                .setAddress(borrower.getAddress())
                .setCity(borrower.getCity())
                .setState(borrower.getState())
                .setZipCode(borrower.getZipCode())
                .setAnnualIncome(borrower.getAnnualIncome()) // Already Double
                .setEmploymentStatus(borrower.getEmploymentStatus()) // Already String
                .setEmployerName(borrower.getEmployerName() != null ? borrower.getEmployerName() : "")
                .setEmploymentYears(borrower.getEmploymentYears() != null ? borrower.getEmploymentYears() : 0)
                .setCreatedAt(formatDateTime(borrower.getCreatedAt()))
                .setEventId(UUID.randomUUID().toString())
                .setEventTimestamp(formatDateTime(LocalDateTime.now()))
                .build();
    }

    public LoanApplicationEvent toLoanApplicationEvent(LoanApplication loanApplication) {
        return LoanApplicationEvent.newBuilder()
                .setApplicationId(loanApplication.getId())
                .setBorrowerId(loanApplication.getBorrower().getId())
                .setLoanAmount(loanApplication.getLoanAmount().doubleValue())
                .setLoanTermMonths(loanApplication.getLoanTermMonths())
                .setLoanPurpose(loanApplication.getPurpose() != null ? loanApplication.getPurpose() : "")
                .setInterestRate(loanApplication.getInterestRate().doubleValue())
                .setMonthlyPayment(loanApplication.getMonthlyPayment() != null ? loanApplication.getMonthlyPayment().doubleValue() : 0.0)
                .setStatus(loanApplication.getStatus().toString())
                .setAppliedAt(formatDateTime(loanApplication.getCreatedAt())) // Using createdAt as appliedAt
                .setEventId(UUID.randomUUID().toString())
                .setEventTimestamp(formatDateTime(LocalDateTime.now()))
                .build();
    }

    public DocumentUploadEvent toDocumentUploadEvent(Document document) {
        return DocumentUploadEvent.newBuilder()
                .setDocumentId(document.getId())
                .setBorrowerId(document.getBorrower().getId())
                .setLoanApplicationId(document.getLoanApplication() != null ? document.getLoanApplication().getId() : 0)
                .setDocumentType(document.getDocumentType().toString())
                .setFileName(document.getFileName())
                .setFilePath(document.getFilePath())
                .setFileSize(document.getFileSize())
                .setContentType(document.getContentType())
                .setEventId(UUID.randomUUID().toString())
                .setEventTimestamp(formatDateTime(LocalDateTime.now()))
                .build();
    }

    private String formatDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }
        return localDateTime.format(FORMATTER);
    }
}
