package com.edusage.dto.request;

import com.edusage.model.enums.CounsellingOutcome;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CounsellingSessionRequest {
    private LocalDate sessionDate;
    private String counsellorName;
    private CounsellingOutcome outcome;
    private String comments;
}
