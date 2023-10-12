package de.unistuttgart.t2.modulith.common.scaling.cpu;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.DoubleConsumer;

/**
 * DTO in disguise.
 * <p>
 * Used so that users can configure the minimum CPU usage.
 *
 * @author Leon Hofmeister
 * @since 1.2.0
 */
public final class CPUUsageRequest {

    /**
     * SECONDS, HOURS, DAYS, ...<br>
     * -> anything, {@link ChronoUnit#valueOf(String)} can work with (case-insensitively)<br>
     * Default: {@link CPUUsage#DEFAULT_TIME_UNIT}
     */
    String timeUnit;

    // Wrappers because they are optional
    Long intervalLength;
    Double cpuPercentage;

    transient DoubleConsumer errorHandler; // for handling errors while converting the CPU percentage

    /**
     * @return this object converted to the most fitting representation of a {@link CPUUsage} when interpreting
     *         {@link #cpuPercentage} as a mathematical ratio
     *         ({@code 0.0 to 1.0 * number of cores instead of 0.0 [%] to 100.0 * number of cores [%]}).<br>
     *         Fallbacks for missing values are:
     *         <ul>
     *         <li>{@link CPUUsage#DEFAULT_TIME_UNIT} if no time unit is given</li>
     *         <li>{@link CPUUsage#DEFAULT_INTERVAL_LENGTH} if no interval length is given</li>
     *         <li>{@link CPUUsage#DEFAULT_REQUESTED_CPU_PERCENTAGE} if no requested CPU usage is given</li>
     *         </ul>
     * @since 1.2.0
     * @see #convertFromHumans()
     */
    public CPUUsage convertFromRatio() {
        return new CPUUsage(convertUnit().name(),
            Objects.requireNonNullElse(intervalLength, CPUUsage.DEFAULT_INTERVAL_LENGTH),
            Objects.requireNonNullElse(cpuPercentage, CPUUsage.DEFAULT_REQUESTED_CPU_PERCENTAGE), errorHandler);
    }

    /**
     * @return this object converted to the most fitting representation of a {@link CPUUsage} when interpreting
     *         {@link #cpuPercentage} as a human percentage
     *         ({@code 0.0 [%] to 100.0 * number of cores [%] instead of 0.0 to 1.0 * number of cores}).<br>
     *         Fallbacks for missing values are:
     *         <ul>
     *         <li>{@link CPUUsage#DEFAULT_TIME_UNIT} if no time unit is given</li>
     *         <li>{@link CPUUsage#DEFAULT_INTERVAL_LENGTH} if no interval length is given</li>
     *         <li>{@link CPUUsage#DEFAULT_REQUESTED_CPU_PERCENTAGE} if no requested CPU usage is given</li>
     *         </ul>
     * @since 1.2.0
     * @see #convertFromRatio()
     */
    public CPUUsage convertFromHumans() {
        return new CPUUsage(convertUnit().name(),
            Objects.requireNonNullElse(intervalLength, CPUUsage.DEFAULT_INTERVAL_LENGTH),
            Objects.requireNonNullElse(cpuPercentage, CPUUsage.DEFAULT_REQUESTED_CPU_PERCENTAGE), errorHandler, true);
    }

    private ChronoUnit convertUnit() {
        try {
            return ChronoUnit.valueOf(timeUnit.toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            return CPUUsage.DEFAULT_TIME_UNIT;
        }
    }

    @Override
    public String toString() {
        return String.format("CPUUsageRequest [timeUnit=%s, intervalLength=%s, cpuPercentage=%s]", timeUnit,
            intervalLength, cpuPercentage);
    }

    public String getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public Long getIntervalLength() {
        return intervalLength;
    }

    public void setIntervalLength(Long intervalLength) {
        this.intervalLength = intervalLength;
    }

    public Double getCpuPercentage() {
        return cpuPercentage;
    }

    public void setCpuPercentage(Double cpuPercentage) {
        this.cpuPercentage = cpuPercentage;
    }

    public DoubleConsumer getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(DoubleConsumer errorHandler) {
        this.errorHandler = errorHandler;
    }
}
