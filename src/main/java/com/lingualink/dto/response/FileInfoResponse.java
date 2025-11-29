package com.lingualink.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoResponse {
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String folder;
    private Long entityId; // User ID, Event ID, or Interpreter ID
    private String entityType; // "user", "event", or "interpreter"
    private LocalDateTime uploadedAt;
    private boolean exists;
}

