
package ro.usv.backend.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.usv.backend.dto.ReservationRequest;
import ro.usv.backend.dto.ReservationResponse;
import ro.usv.backend.dto.ReservationStatusRequest;
import ro.usv.backend.service.ReservationService;
import ro.usv.backend.service.UserService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final UserService userService;

    public ReservationController(
            ReservationService reservationService,
            UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    @GetMapping
    public List<ReservationResponse> list(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "mine") String scope) {
        return reservationService.list(userService.require(userId), scope);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody ReservationRequest request) {
        return reservationService.create(userService.require(userId), request);
    }

    @PutMapping("/{id}")
    public ReservationResponse update(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ReservationRequest request) {
        return reservationService.update(userService.require(userId), id, request);
    }

    @PatchMapping("/{id}/status")
    public ReservationResponse updateStatus(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusRequest request) {
        return reservationService.updateStatus(
                userService.require(userId), id, request.status());
    }
}
