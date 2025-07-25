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

    // getBorrowerByUserId fully removed
} 