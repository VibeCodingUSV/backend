package ro.usv.backend.dto;

import jakarta.validation.constraints.NotNull;
import ro.usv.backend.entity.ReservationStatus;

public record ReservationStatusRequest(@NotNull ReservationStatus status) {}