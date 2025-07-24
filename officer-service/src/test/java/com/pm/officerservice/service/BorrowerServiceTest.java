package com.pm.officerservice.service;

import com.pm.borrowerservice.events.BorrowerCreatedEvent;
import com.pm.officerservice.model.Borrower;
import com.pm.officerservice.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private BorrowerService borrowerService;

    private BorrowerCreatedEvent borrowerCreatedEvent;

    @BeforeEach
    void setUp() {
        borrowerCreatedEvent = BorrowerCreatedEvent.newBuilder()
                .setBorrowerId(1L)
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john.doe@example.com")
                .setPhoneNumber("1234567890")
                .setDateOfBirth("1990-01-01")
                .setSsn("123-45-6789")
                .setAddress("123 Main St")
                .setCity("New York")
                .setState("NY")
                .setZipCode("10001")
                .setAnnualIncome(50000.0)
                .setEmploymentStatus("EMPLOYED")
                .setEmployerName("ABC Corp")
                .setEmploymentYears(5)
                .setCreatedAt(LocalDateTime.now().toString())
                .build();
    }

    @Test
    void processBorrowerCreatedEvent_NewBorrower_Success() {
        // Given
        when(borrowerRepository.existsById(1L)).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(new Borrower());

        // When
        borrowerService.processBorrowerCreatedEvent(borrowerCreatedEvent);

        // Then
        verify(borrowerRepository).existsById(1L);
        verify(borrowerRepository).save(any(Borrower.class));
    }

    @Test
    void processBorrowerCreatedEvent_ExistingBorrower_Skip() {
        // Given
        when(borrowerRepository.existsById(1L)).thenReturn(true);

        // When
        borrowerService.processBorrowerCreatedEvent(borrowerCreatedEvent);

        // Then
        verify(borrowerRepository).existsById(1L);
        verify(borrowerRepository, never()).save(any());
    }

    @Test
    void processBorrowerCreatedEvent_RepositoryException_ThrowsException() {
        // Given
        when(borrowerRepository.existsById(1L)).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> borrowerService.processBorrowerCreatedEvent(borrowerCreatedEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");
    }

    @Test
    void processBorrowerCreatedEvent_InvalidTimestamp_UsesCurrentTime() {
        // Given
        BorrowerCreatedEvent eventWithInvalidTimestamp = borrowerCreatedEvent.toBuilder()
                .setCreatedAt("invalid-timestamp")
                .build();
        
        when(borrowerRepository.existsById(1L)).thenReturn(false);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(new Borrower());

        // When
        borrowerService.processBorrowerCreatedEvent(eventWithInvalidTimestamp);

        // Then
        verify(borrowerRepository).save(any(Borrower.class));
    }
}
