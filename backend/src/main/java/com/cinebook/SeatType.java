package com.cinebook;

public enum SeatType {
    REGULAR(150), PREMIUM(250), VIP(400);

    private final int price;

    SeatType(int price) { this.price = price; }

    public int getPrice() { return price; }

    /** Rows A-B regular, C-D premium, E VIP (row = 0..4). */
    public static SeatType forRow(int row) {
        return row < 2 ? REGULAR : row < 4 ? PREMIUM : VIP;
    }
}
