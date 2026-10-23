package com.cinebook;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class BookingService implements Bookable {

    private final JdbcTemplate db;

    public BookingService(JdbcTemplate db) { this.db = db; }

    private static ResponseStatusException bad(String m) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, m);
    }

    private static double round2(double v) { return Math.round(v * 100) / 100.0; }

    private static final RowMapper<Booking> MAP = (rs, i) -> {
        Booking b = new Booking();
        b.id = rs.getLong("id");
        b.showId = rs.getInt("show_id");
        b.movie = rs.getString("movie");
        b.showTime = rs.getString("show_time");
        b.hue = rs.getInt("hue");
        b.seats = Arrays.asList(rs.getString("seats").split(","));
        b.originalAmount = rs.getDouble("original_amount");
        b.discount = rs.getDouble("discount");
        b.amount = rs.getDouble("amount");
        b.refund = rs.getDouble("refund");
        b.coupon = rs.getString("coupon");
        b.paymentMethod = rs.getString("payment_method");
        b.paymentStatus = rs.getString("payment_status");
        b.status = rs.getString("status");
        return b;
    };

    private static final String SELECT =
        "select b.*, s.movie, s.show_time, s.hue from bookings b join shows s on s.id = b.show_id ";

    public List<Map<String, Object>> shows() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> s : db.queryForList("select id, movie, show_time, hue from shows order by id")) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.get("id"));
            m.put("movie", s.get("movie"));
            m.put("time", s.get("show_time"));
            m.put("hue", s.get("hue"));
            m.put("taken", db.queryForList("select seat_code from booked_seats where show_id = ? order by seat_code",
                    String.class, s.get("id")));
            out.add(m);
        }
        return out;
    }

    public List<Booking> history() {
        return db.query(SELECT + "order by b.id desc", MAP);
    }

    @Transactional
    @Override
    public Booking bookSeats(int showId, List<String> raw, String coupon, String payment, boolean paymentConfirmed) {
        if (raw == null || raw.isEmpty()) throw bad("Select at least one seat.");

        List<String> seats = new ArrayList<>();
        for (String s : raw) {
            s = s.trim().toUpperCase();
            if (!s.matches("[A-E][1-8]")) throw bad("Invalid seat code: " + s);
            if (seats.contains(s)) throw bad("Seat repeated: " + s);
            seats.add(s);
        }

        Integer n = db.queryForObject("select count(*) from shows where id = ?", Integer.class, showId);
        if (n == null || n == 0) throw bad("Show not found.");

        String method;
        if ("Google Pay".equals(payment)) {
            if (!paymentConfirmed) throw bad("Payment not confirmed. Booking cancelled.");
            method = "Google Pay";
        } else if ("Cash".equals(payment)) {
            method = "Cash";
        } else {
            throw bad("Invalid payment method.");
        }

        coupon = coupon == null ? "" : coupon.trim().toUpperCase();
        double original = 0;
        for (String s : seats) original += SeatType.forRow(s.charAt(0) - 'A').getPrice();
        double discount = round2(coupon.equals("CINE10") ? original * 0.10 : 0);
        double amount = round2(original - discount);
        String usedCoupon = coupon.equals("CINE10") ? coupon : "";

        Long id = db.queryForObject(
            "insert into bookings(show_id, seats, original_amount, discount, amount, coupon, payment_method) " +
            "values (?,?,?,?,?,?,?) returning id",
            Long.class, showId, String.join(",", seats), original, discount, amount, usedCoupon, method);

        try {
            for (String s : seats)
                db.update("insert into booked_seats(show_id, seat_code, booking_id) values (?,?,?)", showId, s, id);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat already booked. Someone just grabbed it.");
        }

        return db.queryForObject(SELECT + "where b.id = ?", MAP, id);
    }

    @Transactional
    @Override
    public double cancelBooking(long id) {
        List<Double> rows = db.queryForList(
            "select amount from bookings where id = ? and status = 'BOOKED' for update", Double.class, id);
        if (rows.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found or already cancelled.");

        double refund = round2(rows.get(0) * 0.80);
        db.update("delete from booked_seats where booking_id = ?", id);
        db.update("update bookings set status='CANCELLED', refund=?, payment_status='REFUND SIMULATED' where id=?",
                refund, id);
        return refund;
    }
}
