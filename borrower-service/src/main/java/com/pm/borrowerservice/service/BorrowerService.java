package com.pm.borrowerservice.service;

import com.pm.borrowerservice.dto.BorrowerDto;
import com.pm.borrowerservice.dto.CreateBorrowerRequest;
import com.pm.borrowerservice.entity.Borrower;
import com.pm.borrowerservice.mapper.BorrowerMapper;
import com.pm.borrowerservice.repository.BorrowerRepository;
import com.pm.borrowerservice.util.EventMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowerService {
    private final BorrowerRepository borrowerRepository;
    private final BorrowerMapper borrowerMapper;
    private final KafkaEventProducerService kafkaEventProducerService;
    private final EventMapper eventMapper;
    private static final Logger logger = LoggerFactory.getLogger(BorrowerService.class);


    @Transactional
    public BorrowerDto createBorrower(CreateBorrowerRequest request) {
        try {
            log.info("Creating new borrower with email: {}", request.getEmail());
            
            // Set a default user ID if not provided (for testing purposes)
            // In production, this would come from JWT token via controller
            if (request.getUserId() == null) {
                // You can modify this logic to extract user ID from authentication context
                // For now, we'll use a default value for testing
                Long defaultUserId = 1L; // This should be extracted from JWT token in production
                
                // Create a new request with the user ID
                request = CreateBorrowerRequest.builder()
                    .userId(defaultUserId)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .dateOfBirth(request.getDateOfBirth())
                    .ssn(request.getSsn())
                    .address(request.getAddress())
                    .city(request.getCity())
                    .state(request.getState())
                    .zipCode(request.getZipCode())
                    .annualIncome(request.getAnnualIncome())
                    .employmentStatus(request.getEmploymentStatus())
                    .employerName(request.getEmployerName())
                    .employmentYears(request.getEmploymentYears())
                    .build();
            }
            
            Borrower borrower = borrowerMapper.toEntity(request);
            borrower = borrowerRepository.save(borrower);
            
            // Publish borrower created event to Kafka
            var borrowerCreatedEvent = eventMapper.toBorrowerCreatedEvent(borrower);
            kafkaEventProducerService.publishBorrowerCreatedEvent(borrowerCreatedEvent);

            
            log.info("Successfully created borrower with ID: {} and published event", borrower.getId());
            
            return borrowerMapper.toDto(borrower);
        } catch (Exception e) {
            log.error("Error creating borrower with email: {}", request.getEmail(), e);
            throw e;
        }
    }

    public BorrowerDto getBorrower(Long id) {
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Borrower not found"));
        return borrowerMapper.toDto(borrower);
    }

    public List<BorrowerDto> getAllBorrowers() {
        logger.info("Get All borrower");

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

    public BorrowerDto getBorrowerByUserId(Long userId) {
        Borrower borrower = borrowerRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Borrower not found for user ID: " + userId));
        return borrowerMapper.toDto(borrower);
    }
} 