package com.cinebook;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiController {

    record BookReq(int showId, List<String> seats, String coupon, String payment, boolean paymentConfirmed) {}

    private final BookingService svc;

    public ApiController(BookingService svc) { this.svc = svc; }

    @GetMapping("/shows")
    public List<Map<String, Object>> shows() { return svc.shows(); }

    @GetMapping("/bookings")
    public List<Booking> bookings() { return svc.history(); }

    @PostMapping("/bookings")
    public Booking book(@RequestBody BookReq r) {
        return svc.bookSeats(r.showId(), r.seats(), r.coupon(), r.payment(), r.paymentConfirmed());
    }

    @PostMapping("/bookings/{id}/cancel")
    public Map<String, Object> cancel(@PathVariable long id) {
        return Map.of("id", id, "refund", svc.cancelBooking(id));
    }
}
