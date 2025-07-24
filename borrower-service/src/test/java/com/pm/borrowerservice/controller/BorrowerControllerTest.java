package com.pm.borrowerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.borrowerservice.dto.BorrowerDto;
import com.pm.borrowerservice.dto.CreateBorrowerRequest;
import com.pm.borrowerservice.dto.LoanApplicationDto;
import com.pm.borrowerservice.entity.Document;
import com.pm.borrowerservice.entity.DocumentStatus;
import com.pm.borrowerservice.entity.LoanApplicationStatus;
import com.pm.borrowerservice.service.BorrowerService;
import com.pm.borrowerservice.service.DocumentService;
import com.pm.borrowerservice.service.LoanApplicationService;
import com.pm.borrowerservice.service.LoanCalculatorService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowerController.class)
class BorrowerControllerTest {

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

    private BorrowerDto testBorrowerDto;
    private CreateBorrowerRequest testCreateRequest;
    private LoanApplicationDto testLoanApplicationDto;
    private Document testDocument;

    @BeforeEach
    void setUp() {
        testBorrowerDto = BorrowerDto.builder()
                .id(1L)
                .userId(100L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1-555-123-4567")
                .annualIncome(75000.0)
                .employmentStatus("EMPLOYED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCreateRequest = CreateBorrowerRequest.builder()
                .userId(100L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1-555-123-4567")
                .annualIncome(75000.0)
                .employmentStatus("EMPLOYED")
                .build();

        testLoanApplicationDto = LoanApplicationDto.builder()
                .id(1L)
                .borrowerId(1L)
                .loanType("PERSONAL")
                .loanAmount(new BigDecimal("10000.00"))
                .loanTermMonths(24)
                .interestRate(new BigDecimal("5.5"))
                .monthlyPayment(new BigDecimal("450.00"))
                .totalPayment(new BigDecimal("10800.00"))
                .status(LoanApplicationStatus.PENDING)
                .applicationNumber("APP-001")
                .purpose("Home improvement")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testDocument = Document.builder()
                .id(1L)
                .documentType("INCOME_VERIFICATION")
                .documentName("salary_slip")
                .fileName("salary_slip.pdf")
                .fileSize(1024L)
                .contentType("application/pdf")
                .filePath("/documents/salary_slip.pdf")
                .status(DocumentStatus.PENDING)
                .description("Monthly salary slip")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ========== Borrower CRUD Tests ==========

    @Test
    void createBorrower_ShouldReturnCreatedBorrower_WhenValidRequest() throws Exception {
        // Given
        when(borrowerService.createBorrower(any(CreateBorrowerRequest.class))).thenReturn(testBorrowerDto);

        // When & Then
        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.annualIncome").value(75000.0));

        verify(borrowerService).createBorrower(any(CreateBorrowerRequest.class));
    }

    @Test
    void getBorrower_ShouldReturnBorrower_WhenBorrowerExists() throws Exception {
        // Given
        when(borrowerService.getBorrower(1L)).thenReturn(testBorrowerDto);

        // When & Then
        mockMvc.perform(get("/api/borrowers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(borrowerService).getBorrower(1L);
    }

    @Test
    void getBorrower_ShouldReturnNotFound_WhenBorrowerNotExists() throws Exception {
        // Given
        when(borrowerService.getBorrower(1L)).thenThrow(new EntityNotFoundException("Borrower not found"));

        // When & Then
        mockMvc.perform(get("/api/borrowers/{id}", 1L))
                .andExpect(status().isNotFound());

        verify(borrowerService).getBorrower(1L);
    }

    @Test
    void getAllBorrowers_ShouldReturnBorrowersList() throws Exception {
        // Given
        List<BorrowerDto> borrowers = Arrays.asList(testBorrowerDto);
        when(borrowerService.getAllBorrowers()).thenReturn(borrowers);

        // When & Then
        mockMvc.perform(get("/api/borrowers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("John"));

        verify(borrowerService).getAllBorrowers();
    }

    @Test
    void getBorrowerByUserId_ShouldReturnBorrower_WhenUserExists() throws Exception {
        // Given
        when(borrowerService.getBorrowerByUserId(100L)).thenReturn(testBorrowerDto);

        // When & Then
        mockMvc.perform(get("/api/borrowers/user/{userId}", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(100L))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(borrowerService).getBorrowerByUserId(100L);
    }

    @Test
    void deleteBorrower_ShouldReturnNoContent_WhenBorrowerDeleted() throws Exception {
        // Given
        doNothing().when(borrowerService).deleteBorrower(1L);

        // When & Then
        mockMvc.perform(delete("/api/borrowers/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(borrowerService).deleteBorrower(1L);
    }

    // ========== Loan Application Tests ==========

    @Test
    void applyForLoan_ShouldReturnCreatedLoanApplication_WhenValidRequest() throws Exception {
        // Given
        when(loanApplicationService.applyForLoan(eq(1L), any(LoanApplicationDto.class)))
                .thenReturn(testLoanApplicationDto);

        // When & Then
        mockMvc.perform(post("/api/borrowers/{borrowerId}/loans", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testLoanApplicationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.loanType").value("PERSONAL"))
                .andExpect(jsonPath("$.loanAmount").value(10000.00))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(loanApplicationService).applyForLoan(eq(1L), any(LoanApplicationDto.class));
    }

    @Test
    void getLoanApplication_ShouldReturnLoanApplication_WhenExists() throws Exception {
        // Given
        when(loanApplicationService.getLoanApplication(1L, 1L)).thenReturn(testLoanApplicationDto);

        // When & Then
        mockMvc.perform(get("/api/borrowers/{borrowerId}/loans/{applicationId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.borrowerId").value(1L))
                .andExpect(jsonPath("$.loanType").value("PERSONAL"));

        verify(loanApplicationService).getLoanApplication(1L, 1L);
    }

    @Test
    void getLoanApplicationsForBorrower_ShouldReturnLoanApplicationsList() throws Exception {
        // Given
        List<LoanApplicationDto> loanApplications = Arrays.asList(testLoanApplicationDto);
        when(loanApplicationService.getLoanApplicationsForBorrower(1L)).thenReturn(loanApplications);

        // When & Then
        mockMvc.perform(get("/api/borrowers/{borrowerId}/loans", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].loanType").value("PERSONAL"));

        verify(loanApplicationService).getLoanApplicationsForBorrower(1L);
    }

    @Test
    void getLoanApplicationsByStatus_ShouldReturnFilteredLoanApplications() throws Exception {
        // Given
        List<LoanApplicationDto> loanApplications = Arrays.asList(testLoanApplicationDto);
        when(loanApplicationService.getLoanApplicationsByStatus(1L, LoanApplicationStatus.PENDING))
                .thenReturn(loanApplications);

        // When & Then
        mockMvc.perform(get("/api/borrowers/{borrowerId}/loans/status/{status}", 1L, "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(loanApplicationService).getLoanApplicationsByStatus(1L, LoanApplicationStatus.PENDING);
    }

    // ========== Loan Calculator Tests ==========

    @Test
    void calculateLoan_ShouldReturnCalculation_WhenValidRequest() throws Exception {
        // Given
        BigDecimal monthlyPayment = new BigDecimal("450.00");
        BigDecimal totalPayment = new BigDecimal("10800.00");
        when(loanCalculatorService.calculateMonthlyPayment(
                any(BigDecimal.class), any(BigDecimal.class), anyInt()))
                .thenReturn(monthlyPayment);
        when(loanCalculatorService.calculateTotalPayment(any(BigDecimal.class), anyInt()))
                .thenReturn(totalPayment);

        Map<String, Object> request = Map.of(
                "amount", "10000",
                "interestRate", "5.5",
                "termMonths", "24"
        );

        // When & Then
        mockMvc.perform(post("/api/borrowers/loans/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyPayment").value(450.00))
                .andExpect(jsonPath("$.totalPayment").value(10800.00));

        verify(loanCalculatorService).calculateMonthlyPayment(
                any(BigDecimal.class), any(BigDecimal.class), anyInt());
        verify(loanCalculatorService).calculateTotalPayment(any(BigDecimal.class), anyInt());
    }

    // ========== Document Management Tests ==========

    @Test
    void uploadDocument_ShouldReturnCreatedDocument_WhenValidRequest() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());
        when(documentService.uploadDocument(eq(1L), any(), eq("INCOME_VERIFICATION"), 
                eq("Test document"), eq(1L))).thenReturn(testDocument);

        // When & Then
        mockMvc.perform(multipart("/api/borrowers/{borrowerId}/documents", 1L)
                        .file(file)
                        .param("documentType", "INCOME_VERIFICATION")
                        .param("description", "Test document")
                        .param("loanApplicationId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentType").value("INCOME_VERIFICATION"))
                .andExpect(jsonPath("$.fileName").value("salary_slip.pdf"));

        verify(documentService).uploadDocument(eq(1L), any(), eq("INCOME_VERIFICATION"), 
                eq("Test document"), eq(1L));
    }

    @Test
    void uploadDocument_ShouldReturnBadRequest_WhenIOException() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "test content".getBytes());
        when(documentService.uploadDocument(eq(1L), any(), eq("INCOME_VERIFICATION"), 
                eq("Test document"), eq(1L))).thenThrow(new IOException("File processing error"));

        // When & Then
        mockMvc.perform(multipart("/api/borrowers/{borrowerId}/documents", 1L)
                        .file(file)
                        .param("documentType", "INCOME_VERIFICATION")
                        .param("description", "Test document")
                        .param("loanApplicationId", "1"))
                .andExpect(status().isInternalServerError());

        verify(documentService).uploadDocument(eq(1L), any(), eq("INCOME_VERIFICATION"), 
                eq("Test document"), eq(1L));
    }

    @Test
    void getDocumentsForBorrower_ShouldReturnDocumentsList() throws Exception {
        // Given
        List<Document> documents = Arrays.asList(testDocument);
        when(documentService.getDocumentsForBorrower(1L)).thenReturn(documents);

        // When & Then
        mockMvc.perform(get("/api/borrowers/{borrowerId}/documents", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].documentType").value("INCOME_VERIFICATION"));

        verify(documentService).getDocumentsForBorrower(1L);
    }

    @Test
    void deleteDocument_ShouldReturnNoContent_WhenDocumentDeleted() throws Exception {
        // Given
        doNothing().when(documentService).deleteDocument(1L, 1L);

        // When & Then
        mockMvc.perform(delete("/api/borrowers/{borrowerId}/documents/{documentId}", 1L, 1L))
                .andExpect(status().isNoContent());

        verify(documentService).deleteDocument(1L, 1L);
    }

    // ========== Validation Tests ==========

    @Test
    void createBorrower_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given - Invalid request with missing required fields
        CreateBorrowerRequest invalidRequest = new CreateBorrowerRequest();

        // When & Then
        mockMvc.perform(post("/api/borrowers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(borrowerService, never()).createBorrower(any(CreateBorrowerRequest.class));
    }

    @Test
    void applyForLoan_ShouldReturnBadRequest_WhenInvalidLoanApplication() throws Exception {
        // Given - Invalid loan application with missing required fields
        LoanApplicationDto invalidLoanApp = new LoanApplicationDto();

        // When & Then
        mockMvc.perform(post("/api/borrowers/{borrowerId}/loans", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoanApp)))
                .andExpect(status().isBadRequest());

        verify(loanApplicationService, never()).applyForLoan(anyLong(), any(LoanApplicationDto.class));
    }
}
