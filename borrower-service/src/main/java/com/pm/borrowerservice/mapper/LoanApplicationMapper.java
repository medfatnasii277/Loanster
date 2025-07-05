package com.pm.borrowerservice.mapper;

import com.pm.borrowerservice.dto.LoanApplicationDto;
import com.pm.borrowerservice.entity.LoanApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LoanApplicationMapper {
    LoanApplicationMapper INSTANCE = Mappers.getMapper(LoanApplicationMapper.class);

    @Mapping(target = "borrowerId", source = "borrower.id")
    @Mapping(target = "borrowerName", expression = "java(loanApplication.getBorrower().getFirstName() + ' ' + loanApplication.getBorrower().getLastName())")
    LoanApplicationDto toDto(LoanApplication loanApplication);
    LoanApplication toEntity(LoanApplicationDto dto);
} 