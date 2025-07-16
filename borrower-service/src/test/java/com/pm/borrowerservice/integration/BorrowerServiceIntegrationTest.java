package com.pm.borrowerservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.borrowerservice.dto.BorrowerDto;
import com.pm.borrowerservice.dto.CreateBorrowerRequest;
import com.pm.borrowerservice.dto.LoanApplicationDto;
import com.pm.borrowerservice.entity.LoanApplicationStatus;
import com.pm.borrowerservice.repository.BorrowerRepository;
import com.pm.borrowerservice.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.file.upload.path=/tmp/test-uploads",
    "app.file.allowed-extensions=pdf,jpg,jpeg,png,txt",
    "app.file.max-size-mb=5"
})
@Transactional
class BorrowerServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    private CreateBorrowerRequest createBorrowerRequest;

    @BeforeEach
    void setUp() {
        // Clean up repositories
        loanApplicationRepository.deleteAll();
        borrowerRepository.deleteAll();

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
    void createBorrower_ShouldCreateAndReturnBorrower() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBorrowerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.annualIncome").value(75000.0))
                .andExpect(jsonPath("$.employmentStatus").value("Employed"));

        // Verify data was saved to database
        assertEquals(1, borrowerRepository.count());
    }

    @Test
    void createBorrower_ShouldFailWithDuplicateEmail() throws Exception {
        // First creation should succeed
        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBorrowerRequest)))
                .andExpect(status().isCreated());

        // Second creation with same email should fail
        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBorrowerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void getBorrower_ShouldReturnBorrower_WhenExists() throws Exception {
        // Arrange - Create a borrower first
        String response = mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBorrowerRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        BorrowerDto createdBorrower = objectMapper.readValue(response, BorrowerDto.class);

        // Act & Assert
        mockMvc.perform(get("/api/borrowers/" + createdBorrower.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdBorrower.getId()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getBorrower_ShouldReturnNotFound_WhenNotExists() throws Exception {
        mockMvc.perform(get("/api/borrowers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBorrowers_ShouldReturnListOfBorrowers() throws Exception {
        // Arrange - Create two borrowers
        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBorrowerRequest)))
                .andExpect(status().isCreated());

        CreateBorrowerRequest secondBorrower = CreateBorrowerRequest.builder()
                .userId(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phoneNumber("+9876543210")
                .dateOfBirth("1985-05-15")
                .ssn("987-65-4321")
                .address("456 Oak Ave")
                .city("Los Angeles")
                .state("CA")
                .zipCode("90210")
                .annualIncome(85000.0)
                .employmentStatus("Employed")
                .employerName("Another Corp")
                .employmentYears(7)
                .build();

        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondBorrower)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/api/borrowers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].firstName", containsInAnyOrder("John", "Jane")));
    }

    @Test
    void applyForLoan_ShouldCreateLoanApplication() throws Exception {
        // Arrange - Create a borrower first
        String borrowerResponse = mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBorrowerRequest)))
                .andExpected(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        BorrowerDto borrower = objectMapper.readValue(borrowerResponse, BorrowerDto.class);

        LoanApplicationDto loanApplicationDto = LoanApplicationDto.builder()
                .loanType("Personal")
                .loanAmount(BigDecimal.valueOf(10000))
                .loanTermMonths(36)
                .interestRate(BigDecimal.valueOf(5.5))
                .purpose("Home improvement")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/borrowers/" + borrower.getId() + "/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanApplicationDto)))
                .andExpected(status().isCreated())
                .andExpected(jsonPath("$.id").exists())
                .andExpected(jsonPath("$.borrowerId").value(borrower.getId()))
                .andExpected(jsonPath("$.loanAmount").value(10000))
                .andExpected(jsonPath("$.loanTermMonths").value(36))
                .andExpected(jsonPath("$.status").value("PENDING"))
                .andExpected(jsonPath("$.monthlyPayment").exists())
                .andExpected(jsonPath("$.totalPayment").exists())
                .andExpected(jsonPath("$.applicationNumber").exists());

        // Verify data was saved to database
        assertEquals(1, loanApplicationRepository.count());
    }

    @Test
    void calculateLoan_ShouldReturnCalculatedPayments() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 10000);
        request.put("interestRate", 5.5);
        request.put("termMonths", 36);

        mockMvc.perform(post("/api/borrowers/loans/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.monthlyPayment").exists())
                .andExpected(jsonPath("$.totalPayment").exists())
                .andExpected(jsonPath("$.monthlyPayment").value(closeTo(302.89, 0.01)))
                .andExpected(jsonPath("$.totalPayment").value(closeTo(10904.04, 0.01)));
    }

    @Test
    void getLoanApplicationsForBorrower_ShouldReturnApplicationsList() throws Exception {
        // Arrange - Create borrower and loan application
        String borrowerResponse = mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBorrowerRequest)))
                .andExpected(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        BorrowerDto borrower = objectMapper.readValue(borrowerResponse, BorrowerDto.class);

        LoanApplicationDto loanApplicationDto = LoanApplicationDto.builder()
                .loanType("Personal")
                .loanAmount(BigDecimal.valueOf(5000))
                .loanTermMonths(24)
                .interestRate(BigDecimal.valueOf(4.5))
                .purpose("Car purchase")
                .build();

        mockMvc.perform(post("/api/borrowers/" + borrower.getId() + "/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanApplicationDto)))
                .andExpected(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/api/borrowers/" + borrower.getId() + "/loans"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpected(jsonPath("$", hasSize(1)))
                .andExpected(jsonPath("$[0].borrowerId").value(borrower.getId()))
                .andExpected(jsonPath("$[0].loanType").value("Personal"))
                .andExpected(jsonPath("$[0].loanAmount").value(5000));
    }

    @Test
    void getLoanApplicationsByStatus_ShouldReturnFilteredApplications() throws Exception {
        // Arrange - Create borrower and loan application
        String borrowerResponse = mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBorrowerRequest)))
                .andExpected(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        BorrowerDto borrower = objectMapper.readValue(borrowerResponse, BorrowerDto.class);

        LoanApplicationDto loanApplicationDto = LoanApplicationDto.builder()
                .loanType("Mortgage")
                .loanAmount(BigDecimal.valueOf(250000))
                .loanTermMonths(360)
                .interestRate(BigDecimal.valueOf(3.5))
                .purpose("Home purchase")
                .build();

        mockMvc.perform(post("/api/borrowers/" + borrower.getId() + "/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanApplicationDto)))
                .andExpected(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/api/borrowers/" + borrower.getId() + "/loans/status/PENDING"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpected(jsonPath("$", hasSize(1)))
                .andExpected(jsonPath("$[0].status").value("PENDING"));

        // Test with non-existing status
        mockMvc.perform(get("/api/borrowers/" + borrower.getId() + "/loans/status/APPROVED"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpected(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteBorrower_ShouldRemoveBorrower() throws Exception {
        // Arrange - Create a borrower first
        String borrowerResponse = mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBorrowerRequest)))
                .andExpected(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        BorrowerDto borrower = objectMapper.readValue(borrowerResponse, BorrowerDto.class);
        assertEquals(1, borrowerRepository.count());

        // Act
        mockMvc.perform(delete("/api/borrowers/" + borrower.getId()))
                .andExpected(status().isNoContent());

        // Assert
        assertEquals(0, borrowerRepository.count());
    }

    @Test
    void getBorrowerByUserId_ShouldReturnBorrower() throws Exception {
        // Arrange
        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBorrowerRequest)))
                .andExpected(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/api/borrowers/user/1"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.userId").value(1))
                .andExpected(jsonPath("$.firstName").value("John"))
                .andExpected(jsonPath("$.lastName").value("Doe"));
    }

    private static void assertEquals(int expected, long actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
