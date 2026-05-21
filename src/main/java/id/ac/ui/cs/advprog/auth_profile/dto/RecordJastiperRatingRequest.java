package id.ac.ui.cs.advprog.auth_profile.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RecordJastiperRatingRequest(
        @NotNull
        @Min(1)
        @Max(5)
        Integer rating
) {
}
