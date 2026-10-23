package com.cinebook;

import java.util.List;

/** Plain data holder; Jackson serialises the public fields straight to the frontend. */
public class Booking {
    public long id;
    public int showId;
    public String movie;
    public String showTime;
    public int hue;
    public List<String> seats;
    public double originalAmount;
    public double discount;
    public double amount;
    public double refund;
    public String coupon;
    public String paymentMethod;
    public String paymentStatus;
    public String status;
}
