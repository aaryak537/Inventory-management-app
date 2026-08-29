package com.example.inventory;

import java.text.NumberFormat;
import java.util.Locale;


public final class StockUtils {

    public static final int LOW_STOCK_LIMIT = 10;

    private StockUtils() {
    }

    public static String getStockStatus(int quantity) {
        if (quantity <= 0) {
            return "Out of Stock";
        } else if (quantity <= LOW_STOCK_LIMIT) {
            return "Low Stock";
        }
        return "In Stock";
    }

    public static String formatINR(double amount) {
        NumberFormat format =
                NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(amount);
    }
}
