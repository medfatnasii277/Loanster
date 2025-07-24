package com.pm.borrowerservice.mapper;

import com.pm.borrowerservice.dto.BorrowerDto;
import com.pm.borrowerservice.dto.CreateBorrowerRequest;
import com.pm.borrowerservice.entity.Borrower;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BorrowerMapperTest {

    private BorrowerMapper borrowerMapper;

    @BeforeEach
    void setUp() {
        borrowerMapper = Mappers.getMapper(BorrowerMapper.class);
    }

    @Test
    void toDto_Success() {
        // Given
        Borrower borrower = new Borrower();
        borrower.setId(1L);
        borrower.setUserId(100L);
        borrower.setFirstName("John");
        borrower.setLastName("Doe");
        borrower.setEmail("john.doe@example.com");
        borrower.setPhoneNumber("555-1234");
        borrower.setDateOfBirth("1990-01-15");
        borrower.setSsn("123-45-6789");
        borrower.setAddress("123 Main St");
        borrower.setCity("Anytown");
        borrower.setState("CA");
        borrower.setZipCode("12345");
        borrower.setAnnualIncome(75000.0);
        borrower.setEmploymentStatus("Employed");
        borrower.setEmployerName("Tech Corp");
        borrower.setEmploymentYears(5);
        borrower.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));
        borrower.setUpdatedAt(LocalDateTime.of(2023, 1, 2, 10, 0));

        // When
        BorrowerDto dto = borrowerMapper.toDto(borrower);

        // Then
        assertNotNull(dto);
        assertEquals(borrower.getId(), dto.getId());
        assertEquals(borrower.getUserId(), dto.getUserId());
        assertEquals(borrower.getFirstName(), dto.getFirstName());
        assertEquals(borrower.getLastName(), dto.getLastName());
        assertEquals(borrower.getEmail(), dto.getEmail());
        assertEquals(borrower.getPhoneNumber(), dto.getPhoneNumber());
        assertEquals(borrower.getDateOfBirth(), dto.getDateOfBirth());
        assertEquals(borrower.getSsn(), dto.getSsn());
        assertEquals(borrower.getAddress(), dto.getAddress());
        assertEquals(borrower.getCity(), dto.getCity());
        assertEquals(borrower.getState(), dto.getState());
        assertEquals(borrower.getZipCode(), dto.getZipCode());
        assertEquals(borrower.getAnnualIncome(), dto.getAnnualIncome());
        assertEquals(borrower.getEmploymentStatus(), dto.getEmploymentStatus());
        assertEquals(borrower.getEmployerName(), dto.getEmployerName());
        assertEquals(borrower.getEmploymentYears(), dto.getEmploymentYears());
        assertEquals(borrower.getCreatedAt(), dto.getCreatedAt());
        assertEquals(borrower.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    void toDto_WithNullValues() {
        // Given
        Borrower borrower = new Borrower();
        borrower.setId(1L);
        borrower.setFirstName("John");
        borrower.setLastName("Doe");
        borrower.setEmail("john.doe@example.com");
        // Other fields are null

        // When
        BorrowerDto dto = borrowerMapper.toDto(borrower);

        // Then
        assertNotNull(dto);
        assertEquals(borrower.getId(), dto.getId());
        assertEquals(borrower.getFirstName(), dto.getFirstName());
        assertEquals(borrower.getLastName(), dto.getLastName());
        assertEquals(borrower.getEmail(), dto.getEmail());
        assertNull(dto.getUserId());
        assertNull(dto.getPhoneNumber());
        assertNull(dto.getAnnualIncome());
    }

    @Test
    void toEntity_FromDto() {
        // Given
        BorrowerDto dto = new BorrowerDto();
        dto.setId(1L);
        dto.setUserId(100L);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPhoneNumber("555-1234");
        dto.setDateOfBirth("1990-01-15");
        dto.setSsn("123-45-6789");
        dto.setAddress("123 Main St");
        dto.setCity("Anytown");
        dto.setState("CA");
        dto.setZipCode("12345");
        dto.setAnnualIncome(75000.0);
        dto.setEmploymentStatus("Employed");
        dto.setEmployerName("Tech Corp");
        dto.setEmploymentYears(5);
        dto.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));
        dto.setUpdatedAt(LocalDateTime.of(2023, 1, 2, 10, 0));

        // When
        Borrower entity = borrowerMapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getUserId(), entity.getUserId());
        assertEquals(dto.getFirstName(), entity.getFirstName());
        assertEquals(dto.getLastName(), entity.getLastName());
        assertEquals(dto.getEmail(), entity.getEmail());
        assertEquals(dto.getPhoneNumber(), entity.getPhoneNumber());
        assertEquals(dto.getDateOfBirth(), entity.getDateOfBirth());
        assertEquals(dto.getSsn(), entity.getSsn());
        assertEquals(dto.getAddress(), entity.getAddress());
        assertEquals(dto.getCity(), entity.getCity());
        assertEquals(dto.getState(), entity.getState());
        assertEquals(dto.getZipCode(), entity.getZipCode());
        assertEquals(dto.getAnnualIncome(), entity.getAnnualIncome());
        assertEquals(dto.getEmploymentStatus(), entity.getEmploymentStatus());
        assertEquals(dto.getEmployerName(), entity.getEmployerName());
        assertEquals(dto.getEmploymentYears(), entity.getEmploymentYears());
        assertEquals(dto.getCreatedAt(), entity.getCreatedAt());
        assertEquals(dto.getUpdatedAt(), entity.getUpdatedAt());
    }

    @Test
    void toEntity_FromCreateRequest() {
        // Given
        CreateBorrowerRequest request = CreateBorrowerRequest.builder()
                .userId(100L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("555-1234")
                .dateOfBirth("1990-01-15")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("Anytown")
                .state("CA")
                .zipCode("12345")
                .annualIncome(75000.0)
                .employmentStatus("Employed")
                .employerName("Tech Corp")
                .employmentYears(5)
                .build();

        // When
        Borrower entity = borrowerMapper.toEntity(request);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId()); // ID should not be set from create request
        assertEquals(request.getUserId(), entity.getUserId());
        assertEquals(request.getFirstName(), entity.getFirstName());
        assertEquals(request.getLastName(), entity.getLastName());
        assertEquals(request.getEmail(), entity.getEmail());
        assertEquals(request.getPhoneNumber(), entity.getPhoneNumber());
        assertEquals(request.getDateOfBirth(), entity.getDateOfBirth());
        assertEquals(request.getSsn(), entity.getSsn());
        assertEquals(request.getAddress(), entity.getAddress());
        assertEquals(request.getCity(), entity.getCity());
        assertEquals(request.getState(), entity.getState());
        assertEquals(request.getZipCode(), entity.getZipCode());
        assertEquals(request.getAnnualIncome(), entity.getAnnualIncome());
        assertEquals(request.getEmploymentStatus(), entity.getEmploymentStatus());
        assertEquals(request.getEmployerName(), entity.getEmployerName());
        assertEquals(request.getEmploymentYears(), entity.getEmploymentYears());
        assertNull(entity.getCreatedAt()); // Timestamps should not be set from request
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void toEntity_FromCreateRequest_WithMinimalData() {
        // Given
        CreateBorrowerRequest request = CreateBorrowerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .annualIncome(50000.0)
                .build();

        // When
        Borrower entity = borrowerMapper.toEntity(request);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals(request.getFirstName(), entity.getFirstName());
        assertEquals(request.getLastName(), entity.getLastName());
        assertEquals(request.getEmail(), entity.getEmail());
        assertEquals(request.getAnnualIncome(), entity.getAnnualIncome());
        assertNull(entity.getUserId());
        assertNull(entity.getPhoneNumber());
        assertNull(entity.getSsn());
    }

    @Test
    void bidirectionalMapping_Consistency() {
        // Given - Create a borrower entity
        Borrower originalBorrower = new Borrower();
        originalBorrower.setId(1L);
        originalBorrower.setUserId(100L);
        originalBorrower.setFirstName("John");
        originalBorrower.setLastName("Doe");
        originalBorrower.setEmail("john.doe@example.com");
        originalBorrower.setAnnualIncome(75000.0);
        originalBorrower.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));

        // When - Convert to DTO and back to entity
        BorrowerDto dto = borrowerMapper.toDto(originalBorrower);
        Borrower convertedBorrower = borrowerMapper.toEntity(dto);

        // Then - Should maintain consistency
        assertEquals(originalBorrower.getId(), convertedBorrower.getId());
        assertEquals(originalBorrower.getUserId(), convertedBorrower.getUserId());
        assertEquals(originalBorrower.getFirstName(), convertedBorrower.getFirstName());
        assertEquals(originalBorrower.getLastName(), convertedBorrower.getLastName());
        assertEquals(originalBorrower.getEmail(), convertedBorrower.getEmail());
        assertEquals(originalBorrower.getAnnualIncome(), convertedBorrower.getAnnualIncome());
        assertEquals(originalBorrower.getCreatedAt(), convertedBorrower.getCreatedAt());
    }
}
