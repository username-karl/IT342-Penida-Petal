package com.petal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedDateRequest {

    @NotBlank(message = "Important date label is required")
    @Size(max = 100, message = "Important date label must be 100 characters or fewer")
    private String label;

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    @JsonAlias("isRecurring")
    @NotNull(message = "Recurring flag is required")
    private Boolean recurring;
}
