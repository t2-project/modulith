package de.unistuttgart.t2.modulith.common.scaling.cpu;

import de.unistuttgart.t2.modulith.common.scaling.Percentage;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.DoubleConsumer;

/**
 * Data class to query the current CPU statistics.
 *
 * @author Leon Hofmeister
 * @since 1.2.0
 */
public final class CPUUsage {

    static final ChronoUnit DEFAULT_TIME_UNIT = ChronoUnit.SECONDS;
    static final long DEFAULT_INTERVAL_LENGTH = 10;
    static final double DEFAULT_REQUESTED_CPU_PERCENTAGE = Double.NEGATIVE_INFINITY;

    final Duration interval;
    final double minCPUUsagePerCore;
    final double minCPUUsageTotal; // Only for the tests and the JSON reporting

    // Not final as it is stated in the Javadoc that the number can change dynamically
    int availableCores = Runtime.getRuntime().availableProcessors();

    /**
     * Creates a cpu usage object without any limits imposed.
     */
    CPUUsage() {
        this(DEFAULT_TIME_UNIT.name(), DEFAULT_INTERVAL_LENGTH, DEFAULT_REQUESTED_CPU_PERCENTAGE, d -> {
            throw new IllegalStateException("The default CPU Percentage now sets limits. Please adapt it. Value: " + d);
        });
    }

    /**
     * Creates a cpu usage object that optionally imposes limits and has no error handling.
     *
     * @param timeUnit         value accepted by {@link ChronoUnit#valueOf(String)}, case insensitive. null = Seconds
     * @param intervalLength   the length of the "waste" interval in {@code timeUnit} steps
     * @param minCPUPercentage percentage of CPU to "waste". Must be a valid mathematic ratio
     *                         ({@code minCPUPercentage ∈ (-∞, 1.0 * numberOfCores)} )
     */
    CPUUsage(String timeUnit, long intervalLength, double minCPUPercentage) {
        this(timeUnit, intervalLength, minCPUPercentage, d -> {}, false);
    }

    /**
     * Creates a cpu usage object that optionally imposes limits.
     *
     * @param timeUnit         value accepted by {@link ChronoUnit#valueOf(String)}, case insensitive. null = Seconds
     * @param intervalLength   the length of the "waste" interval in {@code timeUnit} steps
     * @param minCPUPercentage percentage of CPU to "waste". Must be a valid mathematic ratio
     *                         ({@code minCPUPercentage ∈ (-∞, 1.0 * numberOfCores)} )
     * @param onInvalidValue   action to perform when {@code minCPUPercentage >= numberOfCores}
     */
    CPUUsage(String timeUnit, long intervalLength, double minCPUPercentage, DoubleConsumer onInvalidValue) {
        this(timeUnit, intervalLength, minCPUPercentage, onInvalidValue, false);
    }

    /**
     * Creates a cpu usage object that optionally imposes limits.
     *
     * @param timeUnit           value accepted by {@link ChronoUnit#valueOf(String)}, case insensitive. null =
     *                           {@link CPUUsage#DEFAULT_TIME_UNIT}
     * @param intervalLength     the length of the "waste" interval in {@code timeUnit} steps
     * @param minCPUPercentage   percentage of CPU to "waste". Must be either a valid mathematic percentage
     *                           ({@code minCPUPercentage ∈ (-∞, 1.0 * numberOfCores)}, i.e. 7.5 = occupy seven cores
     *                           and a half completely),<br>
     *                           or a valid human percentage ({@code minCPUPercentage ∈ (-∞, 100.0 * numberOfCores)},
     *                           i.e. 750.0 = occupy seven cores and a half completely)
     * @param onInvalidValue     action to perform when {@code minCPUPercentage >=} upper interval border
     * @param useHumanPercentage whether to require the interval in human percentages, or in mathematical ratios
     */
    CPUUsage(String timeUnit, long intervalLength, double minCPUPercentage, DoubleConsumer onInvalidValue,
        boolean useHumanPercentage) {
        interval = Duration.of(intervalLength,
            ChronoUnit.valueOf(Objects.requireNonNullElse(timeUnit, DEFAULT_TIME_UNIT.name()).toUpperCase())).abs();
        onInvalidValue = Objects.requireNonNullElse(onInvalidValue, d -> {});
        minCPUUsagePerCore =
            useHumanPercentage ? Percentage.fromHumanPercentage(minCPUPercentage / availableCores, onInvalidValue)
                : Percentage.validateMathematicalRatio(minCPUPercentage / availableCores, onInvalidValue);
        minCPUUsageTotal = minCPUPercentage;
    }

    /**
     * Asks the JVM (again), how many cores are available.
     */
    public void refreshAvailableCores() {
        availableCores = Runtime.getRuntime().availableProcessors();
    }

    /**
     * @return whether this usage actually imposes any limits
     */
    public boolean limitsPresent() {
        return minCPUUsagePerCore > 0;
    }

    /**
     * @return the length a 100% occupying task should run for per core in nanoseconds
     */
    public long limitInNanosecondsPerCore() {
        return (long) (interval.toNanos() * minCPUUsagePerCore);
    }

    public int getAvailableCores() {
        return availableCores;
    }

    public Duration getInterval() {
        return interval;
    }

    public double getMinCPUUsagePerCore() {
        return minCPUUsagePerCore;
    }

    public double getMinCPUUsageTotal() {
        return minCPUUsageTotal;
    }

    public void setAvailableCores(int availableCores) {
        this.availableCores = availableCores;
    }

    /**
     * @return a new {@code CPUInfo} that has no CPU limits set
     * @see #CPUUsage()
     */
    public static CPUUsage newUsageWithoutLimits() {
        return new CPUUsage();
    }

    @Override
    public String toString() {
        return String.format("CPUUsage [interval=%s, minCPUUsagePerCore=%s, minCPUUsageTotal=%s, availableCores=%s]",
            interval, minCPUUsagePerCore, minCPUUsageTotal, availableCores);
    }
}
