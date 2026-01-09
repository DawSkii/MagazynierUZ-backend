package org.example.magazynieruz.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;


@Builder
public record ErrorResponse (
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'HH:mm:ss'Z'")
    LocalDateTime timestamp,
    
    int status,
    
    String error,
    
    String message,
    
    String path,
    
    List<String> details
) {}
