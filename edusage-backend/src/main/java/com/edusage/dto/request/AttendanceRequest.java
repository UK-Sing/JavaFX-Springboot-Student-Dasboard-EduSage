package com.edusage.dto.request;

import com.edusage.model.enums.AttendanceStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AttendanceRequest {
    private Long studentId;
    private LocalDate date;
    private AttendanceStatus status;
}
