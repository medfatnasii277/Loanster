package com.pm.officerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.officerservice.dto.DocumentResponse;
import com.pm.officerservice.dto.DocumentStatusUpdateRequest;
import com.pm.officerservice.dto.LoanApplicationResponse;
import com.pm.officerservice.dto.LoanStatusUpdateRequest;
import com.pm.officerservice.model.*;
import com.pm.officerservice.repository.DocumentRepository;
import com.pm.officerservice.repository.LoanApplicationRepository;
import com.pm.officerservice.service.DocumentService;
import com.pm.officerservice.service.LoanApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanApplicationService loanApplicationService;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private LoanApplicationRepository loanApplicationRepository;

    @MockBean
    private DocumentRepository documentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private LoanApplication loanApplication;
    private Document document;
    private Borrower borrower;

    @BeforeEach
    void setUp() {
        borrower = Borrower.builder()
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

        loanApplication = LoanApplication.builder()
                .applicationId(1L)
                .borrower(borrower)
                .loanAmount(BigDecimal.valueOf(100000))
                .loanTermMonths(36)
                .loanPurpose("Home Purchase")
                .interestRate(BigDecimal.valueOf(5.5))
                .monthlyPayment(BigDecimal.valueOf(3000))
                .status("PENDING")
                .appliedAtSource(LocalDateTime.now())
                .build();

        document = Document.builder()
                .documentId(1L)
                .borrower(borrower)
                .loanApplication(loanApplication)
                .documentType("INCOME_STATEMENT")
                .fileName("income.pdf")
                .filePath("/documents/income.pdf")
                .fileSize(1024L)
                .contentType("application/pdf")
                .status(DocumentStatus.PENDING)
                .uploadedAtSource(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllLoanApplications_ReturnsListOfApplications() throws Exception {
        List<LoanApplication> applications = Arrays.asList(loanApplication);
        when(loanApplicationRepository.findAll()).thenReturn(applications);

        mockMvc.perform(get("/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].applicationId").value(1L))
                .andExpect(jsonPath("$[0].borrowerName").value("John Doe"));

        verify(loanApplicationRepository).findAll();
    }

    @Test
    void getLoanApplication_ExistingId_ReturnsApplication() throws Exception {
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loanApplication));

        mockMvc.perform(get("/loans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(1L))
                .andExpect(jsonPath("$.borrowerName").value("John Doe"));

        verify(loanApplicationRepository).findById(1L);
    }

    @Test
    void getLoanApplication_NonExistingId_ReturnsNotFound() throws Exception {
        when(loanApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/loans/999"))
                .andExpect(status().isNotFound());

        verify(loanApplicationRepository).findById(999L);
    }

    @Test
    void getLoanApplicationsByStatus_ValidStatus_ReturnsApplications() throws Exception {
        List<LoanApplication> applications = Arrays.asList(loanApplication);
        when(loanApplicationRepository.findByStatus("PENDING")).thenReturn(applications);

        mockMvc.perform(get("/loans/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].applicationId").value(1L));

        verify(loanApplicationRepository).findByStatus("PENDING");
    }

    @Test
    void getLoanApplicationsByStatus_InvalidStatus_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/loans/status/INVALID_STATUS"))
                .andExpect(status().isBadRequest());

        verify(loanApplicationRepository, never()).findByStatus(anyString());
    }

    @Test
    void updateLoanStatus_ValidRequest_ReturnsOk() throws Exception {
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.APPROVED)
                .updatedBy("officer123")
                .build();

        doNothing().when(loanApplicationService).updateLoanStatus(eq(1L), any(LoanStatusUpdateRequest.class));

        mockMvc.perform(put("/loans/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(loanApplicationService).updateLoanStatus(eq(1L), any(LoanStatusUpdateRequest.class));
    }

    @Test
    void updateLoanStatus_InvalidRequest_ReturnsBadRequest() throws Exception {
        LoanStatusUpdateRequest request = LoanStatusUpdateRequest.builder()
                .newStatus(LoanApplicationStatus.APPROVED)
                // Missing required updatedBy field
                .build();

        mockMvc.perform(put("/loans/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(loanApplicationService, never()).updateLoanStatus(anyLong(), any());
    }

    @Test
    void getAllDocuments_ReturnsListOfDocuments() throws Exception {
        List<Document> documents = Arrays.asList(document);
        when(documentRepository.findAll()).thenReturn(documents);

        mockMvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].documentId").value(1L))
                .andExpect(jsonPath("$[0].fileName").value("income.pdf"));

        verify(documentRepository).findAll();
    }

    @Test
    void getDocument_ExistingId_ReturnsDocument() throws Exception {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        mockMvc.perform(get("/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(1L))
                .andExpect(jsonPath("$.fileName").value("income.pdf"));

        verify(documentRepository).findById(1L);
    }

    @Test
    void getDocument_NonExistingId_ReturnsNotFound() throws Exception {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/documents/999"))
                .andExpect(status().isNotFound());

        verify(documentRepository).findById(999L);
    }

    @Test
    void getDocumentsByStatus_ValidStatus_ReturnsDocuments() throws Exception {
        List<Document> documents = Arrays.asList(document);
        when(documentRepository.findByStatus(DocumentStatus.PENDING)).thenReturn(documents);

        mockMvc.perform(get("/documents/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].documentId").value(1L));

        verify(documentRepository).findByStatus(DocumentStatus.PENDING);
    }

    @Test
    void getDocumentsByStatus_InvalidStatus_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/documents/status/INVALID_STATUS"))
                .andExpect(status().isBadRequest());

        verify(documentRepository, never()).findByStatus(any());
    }

    @Test
    void getDocumentsForLoanApplication_ReturnsDocuments() throws Exception {
        List<Document> documents = Arrays.asList(document);
        when(documentRepository.findByLoanApplicationApplicationId(1L)).thenReturn(documents);

        mockMvc.perform(get("/loans/1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].documentId").value(1L));

        verify(documentRepository).findByLoanApplicationApplicationId(1L);
    }

    @Test
    void updateDocumentStatus_ValidRequest_ReturnsOk() throws Exception {
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.VERIFIED)
                .updatedBy("officer123")
                .build();

        doNothing().when(documentService).updateDocumentStatus(eq(1L), any(DocumentStatusUpdateRequest.class));

        mockMvc.perform(put("/documents/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(documentService).updateDocumentStatus(eq(1L), any(DocumentStatusUpdateRequest.class));
    }

    @Test
    void updateDocumentStatus_InvalidRequest_ReturnsBadRequest() throws Exception {
        DocumentStatusUpdateRequest request = DocumentStatusUpdateRequest.builder()
                .newStatus(DocumentStatus.VERIFIED)
                // Missing required updatedBy field
                .build();

        mockMvc.perform(put("/documents/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(documentService, never()).updateDocumentStatus(anyLong(), any());
    }

    @Test
    void getAvailableLoanStatuses_ReturnsAllStatuses() throws Exception {
        mockMvc.perform(get("/status/loan-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(LoanApplicationStatus.values().length));
    }

    @Test
    void getAvailableDocumentStatuses_ReturnsAllStatuses() throws Exception {
        mockMvc.perform(get("/status/document-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(DocumentStatus.values().length));
    }
}
