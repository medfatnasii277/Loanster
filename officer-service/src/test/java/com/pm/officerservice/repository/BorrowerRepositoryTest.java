package com.pm.officerservice.repository;

import com.pm.officerservice.model.Borrower;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BorrowerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BorrowerRepository borrowerRepository;

    private Borrower borrower1;
    private Borrower borrower2;

    @BeforeEach
    void setUp() {
        borrower1 = Borrower.builder()
                .borrowerId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .dateOfBirth("1990-01-01")
                .ssn("123-45-6789")
                .address("123 Main St")
                .city("New York")
                .state("NY")
                .zipCode("10001")
                .annualIncome(50000.0)
                .employmentStatus("EMPLOYED")
                .employerName("ABC Corp")
                .employmentYears(5)
                .createdAtSource(LocalDateTime.now())
                .build();

        borrower2 = Borrower.builder()
                .borrowerId(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("9876543210")
                .dateOfBirth("1985-05-15")
                .ssn("987-65-4321")
                .address("456 Oak Ave")
                .city("Los Angeles")
                .state("CA")
                .zipCode("90001")
                .annualIncome(75000.0)
                .employmentStatus("EMPLOYED")
                .employerName("XYZ Inc")
                .employmentYears(8)
                .createdAtSource(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(borrower1);
        entityManager.persistAndFlush(borrower2);
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsBorrower() {
        // When
        Optional<Borrower> found = borrowerRepository.findByEmail("john.doe@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getBorrowerId()).isEqualTo(1L);
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        // When
        Optional<Borrower> found = borrowerRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findBySsn_ExistingSsn_ReturnsBorrower() {
        // When
        Optional<Borrower> found = borrowerRepository.findBySsn("123-45-6789");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getBorrowerId()).isEqualTo(1L);
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void findBySsn_NonExistingSsn_ReturnsEmpty() {
        // When
        Optional<Borrower> found = borrowerRepository.findBySsn("000-00-0000");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void save_CreatesNewBorrower() {
        // Given
        Borrower newBorrower = Borrower.builder()
                .borrowerId(3L)
                .firstName("Bob")
                .lastName("Johnson")
                .email("bob.johnson@example.com")
                .phoneNumber("5555555555")
                .dateOfBirth("1992-03-10")
                .ssn("555-55-5555")
                .address("789 Pine St")
                .city("Chicago")
                .state("IL")
                .zipCode("60601")
                .annualIncome(60000.0)
                .employmentStatus("EMPLOYED")
                .employerName("DEF Ltd")
                .employmentYears(3)
                .createdAtSource(LocalDateTime.now())
                .build();

        // When
        Borrower saved = borrowerRepository.save(newBorrower);

        // Then
        assertThat(saved.getBorrowerId()).isEqualTo(3L);
        assertThat(saved.getFirstName()).isEqualTo("Bob");
        assertThat(saved.getLastName()).isEqualTo("Johnson");
        assertThat(saved.getEmail()).isEqualTo("bob.johnson@example.com");
    }

    @Test
    void findById_ExistingId_ReturnsBorrower() {
        // When
        Optional<Borrower> found = borrowerRepository.findById(1L);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getBorrowerId()).isEqualTo(1L);
        assertThat(found.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void findById_NonExistingId_ReturnsEmpty() {
        // When
        Optional<Borrower> found = borrowerRepository.findById(999L);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ReturnsAllBorrowers() {
        // When
        var allBorrowers = borrowerRepository.findAll();

        // Then
        assertThat(allBorrowers).hasSize(2);
        assertThat(allBorrowers).extracting(Borrower::getBorrowerId).containsExactly(1L, 2L);
    }
}
