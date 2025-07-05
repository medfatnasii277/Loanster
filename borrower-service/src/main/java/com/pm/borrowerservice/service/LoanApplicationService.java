package com.pm.borrowerservice.service;

import com.pm.borrowerservice.dto.LoanApplicationDto;
import com.pm.borrowerservice.entity.Borrower;
import com.pm.borrowerservice.entity.LoanApplication;
import com.pm.borrowerservice.entity.LoanApplicationStatus;
import com.pm.borrowerservice.mapper.LoanApplicationMapper;
import com.pm.borrowerservice.repository.BorrowerRepository;
import com.pm.borrowerservice.repository.LoanApplicationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {
    private final LoanApplicationRepository loanApplicationRepository;
    private final BorrowerRepository borrowerRepository;
    private final LoanApplicationMapper loanApplicationMapper;
    private final LoanCalculatorService loanCalculatorService;

    @Transactional
    public LoanApplicationDto applyForLoan(Long borrowerId, LoanApplicationDto dto) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new EntityNotFoundException("Borrower not found"));
        LoanApplication loanApplication = loanApplicationMapper.toEntity(dto);
        loanApplication.setBorrower(borrower);
        // Calculate monthly and total payment
        BigDecimal monthly = loanCalculatorService.calculateMonthlyPayment(
                loanApplication.getLoanAmount(),
                loanApplication.getInterestRate(),
                loanApplication.getLoanTermMonths()
        );
        loanApplication.setMonthlyPayment(monthly);
        loanApplication.setTotalPayment(
                loanCalculatorService.calculateTotalPayment(monthly, loanApplication.getLoanTermMonths())
        );
        loanApplication.setStatus(LoanApplicationStatus.PENDING);
        loanApplication = loanApplicationRepository.save(loanApplication);
        return loanApplicationMapper.toDto(loanApplication);
    }

    public LoanApplicationDto getLoanApplication(Long borrowerId, Long applicationId) {
        LoanApplication loanApplication = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Loan application not found"));
        if (!loanApplication.getBorrower().getId().equals(borrowerId)) {
            throw new EntityNotFoundException("Loan application does not belong to this borrower");
        }
        return loanApplicationMapper.toDto(loanApplication);
    }

    public List<LoanApplicationDto> getLoanApplicationsForBorrower(Long borrowerId) {
        return loanApplicationRepository.findByBorrowerId(borrowerId).stream()
                .map(loanApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<LoanApplicationDto> getLoanApplicationsByStatus(Long borrowerId, LoanApplicationStatus status) {
        return loanApplicationRepository.findByBorrowerIdAndStatus(borrowerId, status).stream()
                .map(loanApplicationMapper::toDto)
                .collect(Collectors.toList());
    }
} 