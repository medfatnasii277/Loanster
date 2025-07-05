package com.pm.borrowerservice.mapper;

import com.pm.borrowerservice.dto.BorrowerDto;
import com.pm.borrowerservice.dto.CreateBorrowerRequest;
import com.pm.borrowerservice.entity.Borrower;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BorrowerMapper {
    BorrowerMapper INSTANCE = Mappers.getMapper(BorrowerMapper.class);

    BorrowerDto toDto(Borrower borrower);
    Borrower toEntity(BorrowerDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanApplications", ignore = true)
    @Mapping(target = "documents", ignore = true)
    Borrower toEntity(CreateBorrowerRequest request);
} 