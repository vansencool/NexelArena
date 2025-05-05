package net.vansen.nexelarena.utils;

public class NumberFormatter {

    public static String format(int number) {
        if (number >= 1_000_000) {
            return String.format("%.2f %s", number / 1_000_000.0, "million");
        } else if (number >= 1_000) {
            return String.format("%.0f %s", number / 1_000.0, "thousand");
        } else {
            return String.valueOf(number);
        }
    }
}
