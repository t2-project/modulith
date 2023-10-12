package de.unistuttgart.t2.modulith.common.scaling;

import java.util.function.DoubleConsumer;

/**
 * Utility class to handle percentages correctly.
 * <p>
 * Only located inside this package because adding its own package only for this file would be overkill in this case.
 *
 * @author Leon Hofmeister
 * @since 1.2.0
 */
public final class Percentage {

    private Percentage() {}

    /**
     * Converts human percentages in range {@code toCheck ∈ (-∞, 100.0)} to their common mathematical form, i.e.
     * {@code 70.0[%] = 0.7}.<br>
     *
     * @param toConvert      the double to convert
     * @param onInvalidValue action to perform when {@code toConvert >= 100.0}
     * @return {@code toConvert}, or {@link Double#NaN} if {@code toConvert >= 100.0}
     * @since 1.2.0
     */
    public static double fromHumanPercentage(double toConvert, DoubleConsumer onInvalidValue) {
        return checkBelow(toConvert, 100, onInvalidValue) / 100;
    }

    /**
     * Validates that {@code toValidate < 1.0}.
     *
     * @param toValidate     the double to validate
     * @param onInvalidValue action to perform when {@code toValidate >= 1.0}
     * @return {@code toValidate}, or {@link Double#NaN} if {@code toValidate >= 1 || toValidate < 0}
     * @since 1.2.0
     */
    public static double validateMathematicalRatio(double toValidate, DoubleConsumer onInvalidValue) {
        return checkBelow(toValidate, 1, onInvalidValue);
    }

    /**
     * Tests that {@code toCheck < maximumValue}.
     *
     * @param toCheck        the double to check for
     * @param maximumValue   the maximum allowed value (exclusive)
     * @param onInvalidValue action to perform when {@code toCheck >= maximumValue}
     * @return {@code toCheck}, or {@link Double#NaN} {@code toCheck >= maximumValue}
     * @since 1.2.0
     */
    private static double checkBelow(double toCheck, double maximumValue, DoubleConsumer onInvalidValue) {
        if (toCheck >= maximumValue) {
            onInvalidValue.accept(toCheck);
            return Double.NaN;
        }
        return toCheck;
    }
}
