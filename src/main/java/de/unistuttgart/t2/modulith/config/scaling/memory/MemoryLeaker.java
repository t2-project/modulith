package de.unistuttgart.t2.modulith.config.scaling.memory;

import de.unistuttgart.t2.modulith.config.scaling.Percentage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.HashSet;

/**
 * Allows to create a memory leak of arbitrary size.
 * <p>
 * This component has to ignore the principles of OOP programming as Spring does not seem to be able to autowire
 * interceptors.<br>
 * Also, in this case setting this globally makes sense as there is no reason to have a deterministic memory leak that
 * only affects part of the application (however that should be possible in the first place).
 *
 * @author Leon Hofmeister
 */
public final class MemoryLeaker implements HandlerInterceptor {

    static final HashSet<Instant> memoryLeak = new HashSet<>();
    static volatile double expectedMemoryPercentage;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        adaptMemory();
        return true;
    }

    /**
     * Clears the memory leak if the is expected memory percentage is {@code < 0}.<br>
     * If the expected memory percentage is {@code > 0}, the memory will be filled until it reaches the expected memory
     * percentage.<br>
     * Nothing will be done if the value is {@code = 0}.
     */
    private static void adaptMemory() {
        Runtime runtime = Runtime.getRuntime();
        if (expectedMemoryPercentage < 0) {
            memoryLeak.clear();
            System.gc();
        } else if (expectedMemoryPercentage > 0) {
            do {
                memoryLeak.add(Instant.now());
                // i.e. 1 - 0.95 <= (20/100) -> allocate more memory
            } while (1 - expectedMemoryPercentage <= (double) runtime.freeMemory()
                / runtime.totalMemory());
        }
    }

    /**
     * Changes the size of the memory leak: values {@code < 0} clear the memory leak.<br>
     * {@code 0} disables the memory leak without clearing the potentially already allocated memory.<br>
     * {@code newPercentage ∈ (0.0, 1.0)} is the mathematical ratio of the memory leak ({@code 0.5} for example
     * means "use half of all available memory").
     *
     * @param newPercentage the minimal percentage of memory to use
     * @throws ResponseStatusException (400) if {@code newPercentage >= 1.0}
     * @see Percentage#validateMathematicalRatio(double, java.util.function.DoubleConsumer)
     */
    public static void changeExpectedMemoryPercentage(double newPercentage) {
        expectedMemoryPercentage = Percentage.validateMathematicalRatio(newPercentage, invalid -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("Cannot request memory above 1.0 or below 0.0. You requested %f (%.2f%%).", invalid,
                    invalid * 100));
        });
        adaptMemory();
    }

    /**
     * Changes the size of the memory leak: values {@code < 0} clear the memory leak.<br>
     * {@code 0} disables the memory leak without clearing the potentially already allocated memory.<br>
     * {@code newPercentage ∈ (0.0, 100.0)} is the human percentage of the memory leak ({@code 50.0} for example means
     * "use half of all available memory").
     *
     * @param newPercentage the minimal percentage of memory to use
     * @throws ResponseStatusException (400) if {@code newPercentage >= 100.0}
     * @see Percentage#fromHumanPercentage(double, java.util.function.DoubleConsumer)
     */
    public static void changeExpectedMemoryFromHumanPercentage(double newPercentage) {
        expectedMemoryPercentage = Percentage.fromHumanPercentage(newPercentage, invalid -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("Cannot request memory above 100%% or below 0%%. You requested %.2f%%.", invalid));
        });
        adaptMemory();
    }

    /**
     * Clears the memory leak, if present.
     */
    public static void clearMemoryLeak() {
        expectedMemoryPercentage = -1;
        adaptMemory();
    }
}
