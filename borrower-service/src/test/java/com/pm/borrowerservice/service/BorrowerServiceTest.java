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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

    private Borrower testBorrower;
    private BorrowerDto testBorrowerDto;
    private CreateBorrowerRequest testCreateRequest;

    @BeforeEach
    void setUp() {
        testBorrower = Borrower.builder()
                .id(1L)
                .userId(100L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("555-0123")
                .dateOfBirth("1990-01-01")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .annualIncome(75000.0)
                .employmentStatus("EMPLOYED")
                .employerName("Tech Corp")
                .employmentYears(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testBorrowerDto = BorrowerDto.builder()
                .id(1L)
                .userId(100L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("555-0123")
                .dateOfBirth("1990-01-01")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .annualIncome(75000.0)
                .employmentStatus("EMPLOYED")
                .employerName("Tech Corp")
                .employmentYears(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCreateRequest = CreateBorrowerRequest.builder()
                .userId(100L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("555-0123")
                .dateOfBirth("1990-01-01")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .annualIncome(75000.0)
                .employmentStatus("EMPLOYED")
                .employerName("Tech Corp")
                .employmentYears(5)
                .build();
    }

    @Test
    void createBorrower_ShouldReturnBorrowerDto_WhenValidRequest() {
        // Given
        when(borrowerMapper.toEntity(any(CreateBorrowerRequest.class))).thenReturn(testBorrower);
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(testBorrower);
        when(borrowerMapper.toDto(testBorrower)).thenReturn(testBorrowerDto);
        when(eventMapper.toBorrowerCreatedEvent(any())).thenReturn(any());

        // When
        BorrowerDto result = borrowerService.createBorrower(testCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");

        verify(borrowerRepository).save(any(Borrower.class));
        verify(borrowerMapper).toEntity(any(CreateBorrowerRequest.class));
        verify(borrowerMapper).toDto(testBorrower);
        verify(kafkaEventProducerService).publishBorrowerCreatedEvent(any());
    }

    @Test
    void getBorrower_ShouldReturnBorrowerDto_WhenBorrowerExists() {
        // Given
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(testBorrower));
        when(borrowerMapper.toDto(testBorrower)).thenReturn(testBorrowerDto);

        // When
        BorrowerDto result = borrowerService.getBorrower(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");

        verify(borrowerRepository).findById(1L);
        verify(borrowerMapper).toDto(testBorrower);
    }

    @Test
    void getBorrower_ShouldThrowEntityNotFoundException_WhenBorrowerNotExists() {
        // Given
        when(borrowerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> borrowerService.getBorrower(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Borrower not found");

        verify(borrowerRepository).findById(1L);
        verify(borrowerMapper, never()).toDto(any(Borrower.class));
    }

    @Test
    void getAllBorrowers_ShouldReturnListOfBorrowerDto() {
        // Given
        Borrower borrower2 = Borrower.builder()
                .id(2L)
                .userId(200L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();

        BorrowerDto borrowerDto2 = BorrowerDto.builder()
                .id(2L)
                .userId(200L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .build();

        List<Borrower> borrowers = Arrays.asList(testBorrower, borrower2);
        when(borrowerRepository.findAll()).thenReturn(borrowers);
        when(borrowerMapper.toDto(testBorrower)).thenReturn(testBorrowerDto);
        when(borrowerMapper.toDto(borrower2)).thenReturn(borrowerDto2);

        // When
        List<BorrowerDto> result = borrowerService.getAllBorrowers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        assertThat(result.get(1).getFirstName()).isEqualTo("Jane");

        verify(borrowerRepository).findAll();
        verify(borrowerMapper, times(2)).toDto(any(Borrower.class));
    }

    @Test
    void deleteBorrower_ShouldDeleteBorrower_WhenCalled() {
        // Given
        doNothing().when(borrowerRepository).deleteById(1L);

        // When
        borrowerService.deleteBorrower(1L);

        // Then
        verify(borrowerRepository).deleteById(1L);
    }
}