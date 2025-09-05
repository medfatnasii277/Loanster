package com.pm.officerservice.dto;

import lombok.Data;

@Data
public class DocumentUploadRequest {
    private Long borrowerId;
    private Long loanApplicationId; // can be null or omitted
    private String documentType;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    // add other fields as needed
}
