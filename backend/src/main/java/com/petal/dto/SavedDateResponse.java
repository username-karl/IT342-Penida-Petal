package com.petal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedDateResponse {
    private Long id;
    private String label;
    private LocalDate eventDate;
    private boolean recurring;
    private Integer notifiedYear;
    private LocalDate nextOccurrenceDate;
    private LocalDate reminderDate;
    private boolean reminderDue;
    private boolean reminderSentForYear;
}
