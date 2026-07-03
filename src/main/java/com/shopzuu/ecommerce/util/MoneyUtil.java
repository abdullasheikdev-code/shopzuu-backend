package com.shopzuu.ecommerce.util;

public final class MoneyUtil {

    private MoneyUtil() {
    }

    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
