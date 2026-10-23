package com.cinebook;

import java.util.List;

public interface Bookable {
    Booking bookSeats(int showId, List<String> seats, String coupon, String payment, boolean paymentConfirmed);
    double cancelBooking(long id);
}
