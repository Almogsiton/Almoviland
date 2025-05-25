package Utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for mathematical operations used across the application.
 */
public class MathUtils {

    /**
     * Rounds a double value to one decimal place.
     *
     * @param value the value to round.
     * @return the value rounded to 1 decimal place
     */
    public static double roundTo1Decimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /**
     * Ensures the returned value is not negative. If value < 0, returns 0.
     * @param
     *
     * value the value to check.
     * @return the original value or 0 if negative.
     */
    public static int ensureNonNegative(int value) {
        return Math.max(0, value);
    }

    /**
     * Adds two integers.
     *
     * @param a first value
     * @param b second value
     * @return the sum of a and b
     */
    public static int sum(int a, int b) {
        return a + b;
    }

    /**
     * Returns the minimum between (a + b) and max.
     *
     * @param a first value
     * @param b second value
     * @param max the maximum allowed value
     * @return Math.min(a + b, max)
     */
    public static int minSum(int a, int b, int max) {
        return Math.min(a + b, max);
    }

    /**
     * Converts a numeric rating into a list of booleans representing full
     * stars.
     *
     * @param rating the rating value
     * @param maxStars the maximum number of stars (e.g., 5)
     * @return list of booleans where true = full star, false = empty
     */
    public static List<Boolean> getStarBooleans(double rating, int maxStars) {
        int fullStars = (int) Math.floor(rating);
        List<Boolean> stars = new ArrayList<>();
        for (int i = 0; i < maxStars; i++) {
            stars.add(i < fullStars);
        }
        return stars;
    }

    /**
     * Generates a list of formatted two-digit month strings from min to max
     * (inclusive).
     *
     * @param min the starting month (e.g., 1)
     * @param max the ending month (e.g., 12)
     * @return list of strings "01" to "12"
     */
    public static List<String> generateMonthOptions(int min, int max) {
        List<String> months = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            months.add(String.format("%02d", i));
        }
        return months;
    }

    /**
     * Generates a list of credit card expiry years from the current year, using
     * configured offset and range.
     *
     * @param startOffset number of years from now to start
     * @param range how many years ahead to include
     * @return list of year strings (e.g., "2025", "2026", ...)
     */
    public static List<String> generateYearOptions(int startOffset, int range) {
        List<String> years = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = startOffset; i <= range; i++) {
            years.add(String.valueOf(currentYear + i));
        }
        return years;
    }

    /**
     * Checks if a password contains at least one lowercase letter, one
     * uppercase letter, and one digit.
     *
     * @param password the password string to validate
     * @return true if the password meets the criteria, false otherwise
     */
    public static boolean isStrongPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
    }

}
