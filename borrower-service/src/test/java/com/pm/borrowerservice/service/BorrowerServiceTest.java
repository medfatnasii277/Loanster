package com.pm.borrowerservice.service;

import com.pm.borrowerservice.dto.BorrowerDto;
import com.pm.borrowerservice.dto.CreateBorrowerRequest;
import com.pm.borrowerservice.entity.Borrower;
import com.pm.borrowerservice.mapper.BorrowerMapper;
import com.pm.borrowerservice.repository.BorrowerRepository;
import com.pm.borrowerservice.util.EventMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BorrowerMapper borrowerMapper;

    @Mock
    private KafkaEventProducerService kafkaEventProducerService;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private BorrowerService borrowerService;

    private Borrower borrower;
    private BorrowerDto borrowerDto;
    private CreateBorrowerRequest createBorrowerRequest;

    @BeforeEach
    void setUp() {
        borrower = Borrower.builder()
                .id(1L)
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .dateOfBirth("1990-01-01")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .annualIncome(75000.0)
                .employmentStatus("Employed")
                .employerName("Tech Corp")
                .employmentYears(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        borrowerDto = BorrowerDto.builder()
                .id(1L)
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .dateOfBirth("1990-01-01")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .annualIncome(75000.0)
                .employmentStatus("Employed")
                .employerName("Tech Corp")
                .employmentYears(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createBorrowerRequest = CreateBorrowerRequest.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .dateOfBirth("1990-01-01")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .annualIncome(75000.0)
                .employmentStatus("Employed")
                .employerName("Tech Corp")
                .employmentYears(5)
                .build();
    }

    @Test
    void createBorrower_ShouldReturnBorrowerDto_WhenValidRequest() {
        // Arrange
        when(borrowerMapper.toEntity(createBorrowerRequest)).thenReturn(borrower);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(borrower);
        when(borrowerMapper.toDto(borrower)).thenReturn(borrowerDto);
        when(eventMapper.toBorrowerCreatedEvent(borrower)).thenReturn(null);

        // Act
        BorrowerDto result = borrowerService.createBorrower(createBorrowerRequest);

        // Assert
        assertNotNull(result);
        assertEquals(borrowerDto.getId(), result.getId());
        assertEquals(borrowerDto.getFirstName(), result.getFirstName());
        assertEquals(borrowerDto.getLastName(), result.getLastName());
        assertEquals(borrowerDto.getEmail(), result.getEmail());

        verify(borrowerRepository).save(any(Borrower.class));
        verify(kafkaEventProducerService).publishBorrowerCreatedEvent(any());
        verify(borrowerMapper).toEntity(createBorrowerRequest);
        verify(borrowerMapper).toDto(borrower);
    }

    @Test
    void createBorrower_ShouldHandleNullUserId() {
        // Arrange
        CreateBorrowerRequest requestWithoutUserId = CreateBorrowerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .dateOfBirth("1990-01-01")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .annualIncome(75000.0)
                .employmentStatus("Employed")
                .employerName("Tech Corp")
                .employmentYears(5)
                .build();

        when(borrowerMapper.toEntity(any(CreateBorrowerRequest.class))).thenReturn(borrower);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(borrower);
        when(borrowerMapper.toDto(borrower)).thenReturn(borrowerDto);
        when(eventMapper.toBorrowerCreatedEvent(borrower)).thenReturn(null);

        // Act
        BorrowerDto result = borrowerService.createBorrower(requestWithoutUserId);

        // Assert
        assertNotNull(result);
        verify(borrowerRepository).save(any(Borrower.class));
    }

    @Test
    void getBorrower_ShouldReturnBorrowerDto_WhenBorrowerExists() {
        // Arrange
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(borrower));
        when(borrowerMapper.toDto(borrower)).thenReturn(borrowerDto);

        // Act
        BorrowerDto result = borrowerService.getBorrower(1L);

        // Assert
        assertNotNull(result);
        assertEquals(borrowerDto.getId(), result.getId());
        assertEquals(borrowerDto.getFirstName(), result.getFirstName());

        verify(borrowerRepository).findById(1L);
        verify(borrowerMapper).toDto(borrower);
    }

    @Test
    void getBorrower_ShouldThrowEntityNotFoundException_WhenBorrowerNotExists() {
        // Arrange
        when(borrowerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> borrowerService.getBorrower(1L));
        verify(borrowerRepository).findById(1L);
        verifyNoInteractions(borrowerMapper);
    }

    @Test
    void getAllBorrowers_ShouldReturnListOfBorrowerDtos() {
        // Arrange
        List<Borrower> borrowers = Arrays.asList(borrower);
        when(borrowerRepository.findAll()).thenReturn(borrowers);
        when(borrowerMapper.toDto(borrower)).thenReturn(borrowerDto);

        // Act
        List<BorrowerDto> result = borrowerService.getAllBorrowers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(borrowerDto.getId(), result.get(0).getId());

        verify(borrowerRepository).findAll();
        verify(borrowerMapper).toDto(borrower);
    }

    @Test
    void deleteBorrower_ShouldCallRepository() {
        // Act
        borrowerService.deleteBorrower(1L);

        // Assert
        verify(borrowerRepository).deleteById(1L);
    }

    @Test
    void findByEmail_ShouldReturnOptionalBorrower() {
        // Arrange
        String email = "john.doe@example.com";
        when(borrowerRepository.findByEmail(email)).thenReturn(Optional.of(borrower));

        // Act
        Optional<Borrower> result = borrowerService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(borrower.getEmail(), result.get().getEmail());

        verify(borrowerRepository).findByEmail(email);
    }

    @Test
    void findBySsn_ShouldReturnOptionalBorrower() {
        // Arrange
        String ssn = "123-45-6789";
        when(borrowerRepository.findBySsn(ssn)).thenReturn(Optional.of(borrower));

        // Act
        Optional<Borrower> result = borrowerService.findBySsn(ssn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(borrower.getSsn(), result.get().getSsn());

        verify(borrowerRepository).findBySsn(ssn);
    }

    @Test
    void getBorrowerByUserId_ShouldReturnBorrowerDto_WhenBorrowerExists() {
        // Arrange
        Long userId = 1L;
        when(borrowerRepository.findByUserId(userId)).thenReturn(Optional.of(borrower));
        when(borrowerMapper.toDto(borrower)).thenReturn(borrowerDto);

        // Act
        BorrowerDto result = borrowerService.getBorrowerByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(borrowerDto.getUserId(), result.getUserId());

        verify(borrowerRepository).findByUserId(userId);
        verify(borrowerMapper).toDto(borrower);
    }

    @Test
    void getBorrowerByUserId_ShouldThrowEntityNotFoundException_WhenBorrowerNotExists() {
        // Arrange
        Long userId = 1L;
        when(borrowerRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> borrowerService.getBorrowerByUserId(userId)
        );

        assertTrue(exception.getMessage().contains("Borrower not found for user ID: " + userId));
        verify(borrowerRepository).findByUserId(userId);
        verifyNoInteractions(borrowerMapper);
    }
}
