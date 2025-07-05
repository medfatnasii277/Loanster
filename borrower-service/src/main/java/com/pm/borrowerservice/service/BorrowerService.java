package com.pm.borrowerservice.service;

import com.pm.borrowerservice.dto.BorrowerDto;
import com.pm.borrowerservice.dto.CreateBorrowerRequest;
import com.pm.borrowerservice.entity.Borrower;
import com.pm.borrowerservice.mapper.BorrowerMapper;
import com.pm.borrowerservice.repository.BorrowerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowerService {
    private final BorrowerRepository borrowerRepository;
    private final BorrowerMapper borrowerMapper;

    public BorrowerDto createBorrower(CreateBorrowerRequest request) {
        Borrower borrower = borrowerMapper.toEntity(request);
        borrower = borrowerRepository.save(borrower);
        return borrowerMapper.toDto(borrower);
    }

    public BorrowerDto getBorrower(Long id) {
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borrower not found"));
        return borrowerMapper.toDto(borrower);
    }

    public List<BorrowerDto> getAllBorrowers() {
        return borrowerRepository.findAll().stream()
                .map(borrowerMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteBorrower(Long id) {
        borrowerRepository.deleteById(id);
    }

    public Optional<Borrower> findByEmail(String email) {
        return borrowerRepository.findByEmail(email);
    }

    public Optional<Borrower> findBySsn(String ssn) {
        return borrowerRepository.findBySsn(ssn);
    }
} 