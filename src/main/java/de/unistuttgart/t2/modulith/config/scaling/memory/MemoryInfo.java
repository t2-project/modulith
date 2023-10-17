package de.unistuttgart.t2.modulith.config.scaling.memory;

/**
 * Data class to query the current memory statistics.
 * <p>
 * It offers no methods because it is only intended to be serialized as a JSON object.
 *
 * @author Leon Hofmeister
 * @since 1.1
 */
public final class MemoryInfo {

    private final long used, free, total, max;
    private final double usedRatio;

    public MemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        max = runtime.maxMemory();
        total = runtime.totalMemory();
        free = runtime.freeMemory();
        used = total - free;
        usedRatio = (double) used / total;
    }

    public long getUsed() {
        return used;
    }

    public long getFree() {
        return free;
    }

    public long getTotal() {
        return total;
    }

    public long getMax() {
        return max;
    }

    public double getUsedRatio() {
        return usedRatio;
    }

    @Override
    public String toString() {
        return String.format("MemoryInfo [used=%s, free=%s, total=%s, max=%s, usedRatio=%s]", used, free, total, max,
            usedRatio);
    }
}
