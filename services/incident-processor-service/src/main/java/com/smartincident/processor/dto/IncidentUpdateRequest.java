package com.smartincident.processor.dto;

import com.smartincident.processor.entity.Category;
import com.smartincident.processor.entity.Severity;
import com.smartincident.processor.entity.Status;
import lombok.Data;

@Data
public class IncidentUpdateRequest {
    private String title;
    private String description;
    private Category category;
    private Severity severity;
    private Status status;
    private String assignedTo;
}
