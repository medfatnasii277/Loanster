package com.pm.borrowerservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBorrowerRequest {

    // Removed userId field

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    private String phoneNumber;

    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    @NotBlank(message = "Social Security Number is required")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{4}$", message = "SSN should be in format XXX-XX-XXXX")
    private String ssn;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min = 2, max = 2, message = "State should be 2 characters")
    private String state;

    @NotBlank(message = "ZIP code is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "ZIP code should be valid")
    private String zipCode;

    @NotNull(message = "Annual income is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Annual income must be positive")
    private Double annualIncome;

    @NotBlank(message = "Employment status is required")
    private String employmentStatus;

    private String employerName;

    @Min(value = 0, message = "Employment years cannot be negative")
    @Max(value = 50, message = "Employment years cannot exceed 50")
    private Integer employmentYears;
} 