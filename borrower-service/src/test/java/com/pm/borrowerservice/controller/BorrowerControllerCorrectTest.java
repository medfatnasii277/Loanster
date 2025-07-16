package com.pm.borrowerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.borrowerservice.dto.BorrowerDto;
import com.pm.borrowerservice.dto.CreateBorrowerRequest;
import com.pm.borrowerservice.dto.LoanApplicationDto;
import com.pm.borrowerservice.entity.DocumentStatus;
import com.pm.borrowerservice.service.BorrowerService;
import com.pm.borrowerservice.service.DocumentService;
import com.pm.borrowerservice.service.LoanApplicationService;
import com.pm.borrowerservice.service.LoanCalculatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for BorrowerController
 * This version uses proper @MockBean from Spring Boot Test
 * and creates test objects manually to avoid Lombok issues
 */
@WebMvcTest(BorrowerController.class)
class BorrowerControllerCorrectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowerService borrowerService;

    @MockBean
    private LoanApplicationService loanApplicationService;

    @MockBean
    private LoanCalculatorService loanCalculatorService;

    @MockBean
    private DocumentService documentService;

    @Test
    void createBorrower_ShouldReturnCreatedBorrower() throws Exception {
        // Create test data manually (avoiding Lombok builders)
        CreateBorrowerRequest request = createTestBorrowerRequest();
        BorrowerDto expectedResponse = createTestBorrowerDto();

        when(borrowerService.createBorrower(any(CreateBorrowerRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("123-456-7890"));
    }

    @Test
    void createBorrower_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        CreateBorrowerRequest invalidRequest = new CreateBorrowerRequest();
        // Don't set required fields to trigger validation

        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBorrowerById_ShouldReturnBorrower() throws Exception {
        BorrowerDto borrower = createTestBorrowerDto();
        
        when(borrowerService.getBorrowerById(1L)).thenReturn(borrower);

        mockMvc.perform(get("/api/borrowers/1"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getAllBorrowers_ShouldReturnBorrowerList() throws Exception {
        List<BorrowerDto> borrowers = Arrays.asList(
                createTestBorrowerDto(),
                createTestBorrowerDto2()
        );

        when(borrowerService.getAllBorrowers()).thenReturn(borrowers);

        mockMvc.perform(get("/api/borrowers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    void updateBorrower_ShouldReturnUpdatedBorrower() throws Exception {
        CreateBorrowerRequest updateRequest = createTestBorrowerRequest();
        updateRequest.setFirstName("UpdatedJohn");
        
        BorrowerDto updatedBorrower = createTestBorrowerDto();
        updatedBorrower.setFirstName("UpdatedJohn");

        when(borrowerService.updateBorrower(eq(1L), any(CreateBorrowerRequest.class)))
                .thenReturn(updatedBorrower);

        mockMvc.perform(put("/api/borrowers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedJohn"));
    }

    @Test
    void deleteBorrower_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/borrowers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void applyForLoan_ShouldReturnLoanApplication() throws Exception {
        LoanApplicationDto loanApplication = createTestLoanApplicationDto();

        when(loanApplicationService.applyForLoan(eq(1L), any(BigDecimal.class), anyInt()))
                .thenReturn(loanApplication);

        mockMvc.perform(post("/api/borrowers/1/loans/apply")
                .param("amount", "10000")
                .param("termInMonths", "12"))
                .andExpect(status().isCreated())
                .andExpected(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(10000))
                .andExpect(jsonPath("$.termInMonths").value(12));
    }

    @Test
    void uploadDocument_ShouldReturnSuccessMessage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        mockMvc.perform(multipart("/api/borrowers/1/documents/upload")
                .file(file)
                .param("documentType", "INCOME_VERIFICATION"))
                .andExpect(status().isOk())
                .andExpect(content().string("Document uploaded successfully"));
    }

    @Test
    void getDocumentsByBorrower_ShouldReturnDocumentList() throws Exception {
        when(documentService.getDocumentsByBorrowerId(1L))
                .thenReturn(Arrays.asList(/* mock documents */));

        mockMvc.perform(get("/api/borrowers/1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void calculateLoanPayment_ShouldReturnPaymentAmount() throws Exception {
        when(loanCalculatorService.calculateMonthlyPayment(
                any(BigDecimal.class), any(BigDecimal.class), anyInt()))
                .thenReturn(new BigDecimal("850.50"));

        mockMvc.perform(get("/api/borrowers/calculate-payment")
                .param("amount", "10000")
                .param("interestRate", "5.5")
                .param("termInMonths", "12"))
                .andExpect(status().isOk())
                .andExpected(content().string("850.50"));
    }

    // Helper methods to create test data manually (avoiding Lombok builders)
    private CreateBorrowerRequest createTestBorrowerRequest() {
        CreateBorrowerRequest request = new CreateBorrowerRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPhoneNumber("123-456-7890");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setSsn("123-45-6789");
        request.setEmploymentStatus("EMPLOYED");
        request.setAnnualIncome(new BigDecimal("75000"));
        request.setAddress("123 Main St");
        request.setCity("Anytown");
        request.setState("CA");
        request.setZipCode("12345");
        return request;
    }

    private BorrowerDto createTestBorrowerDto() {
        BorrowerDto dto = new BorrowerDto();
        dto.setId(1L);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john.doe@example.com");
        dto.setPhoneNumber("123-456-7890");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setSsn("123-45-6789");
        dto.setEmploymentStatus("EMPLOYED");
        dto.setAnnualIncome(new BigDecimal("75000"));
        dto.setAddress("123 Main St");
        dto.setCity("Anytown");
        dto.setState("CA");
        dto.setZipCode("12345");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    private BorrowerDto createTestBorrowerDto2() {
        BorrowerDto dto = new BorrowerDto();
        dto.setId(2L);
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setEmail("jane.smith@example.com");
        dto.setPhoneNumber("987-654-3210");
        dto.setDateOfBirth(LocalDate.of(1985, 5, 15));
        dto.setSsn("987-65-4321");
        dto.setEmploymentStatus("EMPLOYED");
        dto.setAnnualIncome(new BigDecimal("65000"));
        dto.setAddress("456 Oak Ave");
        dto.setCity("Another City");
        dto.setState("NY");
        dto.setZipCode("54321");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    private LoanApplicationDto createTestLoanApplicationDto() {
        LoanApplicationDto dto = new LoanApplicationDto();
        dto.setId(1L);
        dto.setBorrowerId(1L);
        dto.setAmount(new BigDecimal("10000"));
        dto.setTermInMonths(12);
        dto.setInterestRate(new BigDecimal("5.5"));
        dto.setStatus("PENDING");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }
}
