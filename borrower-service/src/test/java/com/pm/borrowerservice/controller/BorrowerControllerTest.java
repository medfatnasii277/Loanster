package com.pm.borrowerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.borrowerservice.dto.BorrowerDto;
import com.pm.borrowerservice.dto.CreateBorrowerRequest;
import com.pm.borrowerservice.dto.LoanApplicationDto;
import com.pm.borrowerservice.entity.Document;
import com.pm.borrowerservice.entity.LoanApplicationStatus;
import com.pm.borrowerservice.service.BorrowerService;
import com.pm.borrowerservice.service.DocumentService;
import com.pm.borrowerservice.service.LoanApplicationService;
import com.pm.borrowerservice.service.LoanCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
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

    private BorrowerDto borrowerDto;
    private CreateBorrowerRequest createBorrowerRequest;
    private LoanApplicationDto loanApplicationDto;

    @BeforeEach
    void setUp() {
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

        loanApplicationDto = LoanApplicationDto.builder()
                .id(1L)
                .borrowerId(1L)
                .borrowerName("John Doe")
                .loanType("Personal")
                .loanAmount(BigDecimal.valueOf(10000))
                .loanTermMonths(36)
                .interestRate(BigDecimal.valueOf(5.5))
                .monthlyPayment(BigDecimal.valueOf(302.89))
                .totalPayment(BigDecimal.valueOf(10904.04))
                .status(LoanApplicationStatus.PENDING)
                .purpose("Home improvement")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createBorrower_ShouldReturnCreatedBorrower() throws Exception {
        when(borrowerService.createBorrower(any(CreateBorrowerRequest.class))).thenReturn(borrowerDto);

        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBorrowerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void createBorrower_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        CreateBorrowerRequest invalidRequest = CreateBorrowerRequest.builder()
                .firstName("") // Invalid: empty first name
                .lastName("Doe")
                .email("invalid-email") // Invalid: bad email format
                .build();

        mockMvc.perform(post("/api/borrowers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBorrower_ShouldReturnBorrower() throws Exception {
        when(borrowerService.getBorrower(1L)).thenReturn(borrowerDto);

        mockMvc.perform(get("/api/borrowers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getAllBorrowers_ShouldReturnBorrowersList() throws Exception {
        List<BorrowerDto> borrowers = Arrays.asList(borrowerDto);
        when(borrowerService.getAllBorrowers()).thenReturn(borrowers);

        mockMvc.perform(get("/api/borrowers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void getBorrowerByUserId_ShouldReturnBorrower() throws Exception {
        when(borrowerService.getBorrowerByUserId(1L)).thenReturn(borrowerDto);

        mockMvc.perform(get("/api/borrowers/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void deleteBorrower_ShouldReturnNoContent() throws Exception {
        doNothing().when(borrowerService).deleteBorrower(1L);

        mockMvc.perform(delete("/api/borrowers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void applyForLoan_ShouldReturnCreatedLoanApplication() throws Exception {
        when(loanApplicationService.applyForLoan(eq(1L), any(LoanApplicationDto.class)))
                .thenReturn(loanApplicationDto);

        mockMvc.perform(post("/api/borrowers/1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanApplicationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.borrowerId").value(1L))
                .andExpect(jsonPath("$.loanAmount").value(10000))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getLoanApplication_ShouldReturnLoanApplication() throws Exception {
        when(loanApplicationService.getLoanApplication(1L, 1L)).thenReturn(loanApplicationDto);

        mockMvc.perform(get("/api/borrowers/1/loans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.borrowerId").value(1L));
    }

    @Test
    void getLoanApplicationsForBorrower_ShouldReturnLoanApplicationsList() throws Exception {
        List<LoanApplicationDto> loanApplications = Arrays.asList(loanApplicationDto);
        when(loanApplicationService.getLoanApplicationsForBorrower(1L)).thenReturn(loanApplications);

        mockMvc.perform(get("/api/borrowers/1/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getLoanApplicationsByStatus_ShouldReturnFilteredApplications() throws Exception {
        List<LoanApplicationDto> pendingApplications = Arrays.asList(loanApplicationDto);
        when(loanApplicationService.getLoanApplicationsByStatus(1L, LoanApplicationStatus.PENDING))
                .thenReturn(pendingApplications);

        mockMvc.perform(get("/api/borrowers/1/loans/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void calculateLoan_ShouldReturnCalculatedPayments() throws Exception {
        BigDecimal monthlyPayment = BigDecimal.valueOf(302.89);
        BigDecimal totalPayment = BigDecimal.valueOf(10904.04);

        when(loanCalculatorService.calculateMonthlyPayment(any(BigDecimal.class), any(BigDecimal.class), anyInt()))
                .thenReturn(monthlyPayment);
        when(loanCalculatorService.calculateTotalPayment(any(BigDecimal.class), anyInt()))
                .thenReturn(totalPayment);

        Map<String, Object> request = new HashMap<>();
        request.put("amount", 10000);
        request.put("interestRate", 5.5);
        request.put("termMonths", 36);

        mockMvc.perform(post("/api/borrowers/loans/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyPayment").exists())
                .andExpect(jsonPath("$.totalPayment").exists());
    }

    @Test
    void uploadDocument_ShouldReturnUploadedDocument() throws Exception {
        Document document = Document.builder()
                .id(1L)
                .documentName("test.pdf")
                .documentType("ID")
                .fileName("test_document.pdf")
                .fileSize(1024L)
                .contentType("application/pdf")
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(documentService.uploadDocument(eq(1L), any(), eq("ID"), eq("Test document"), isNull()))
                .thenReturn(document);

        mockMvc.perform(multipart("/api/borrowers/1/documents")
                .file(file)
                .param("documentType", "ID")
                .param("description", "Test document"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentName").value("test.pdf"));
    }

    @Test
    void getDocumentsForBorrower_ShouldReturnDocumentsList() throws Exception {
        Document document = Document.builder()
                .id(1L)
                .documentName("test.pdf")
                .documentType("ID")
                .build();

        List<Document> documents = Arrays.asList(document);
        when(documentService.getDocumentsForBorrower(1L)).thenReturn(documents);

        mockMvc.perform(get("/api/borrowers/1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void deleteDocument_ShouldReturnNoContent() throws Exception {
        doNothing().when(documentService).deleteDocument(1L, 1L);

        mockMvc.perform(delete("/api/borrowers/1/documents/1"))
                .andExpect(status().isNoContent());
    }
}
