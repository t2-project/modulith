package de.unistuttgart.t2.modulith.common.scaling.cpu;

import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages how much CPU is consistently used as minimum.
 *
 * @author Leon Hofmeister
 * @since 1.2.0
 */
@Component
public final class CPUManager {

    CPUUsage status = CPUUsage.newUsageWithoutLimits();
    Optional<ScheduledExecutorService> taskExecutor = Optional.empty();

    public CPUManager() {
        setupExecutor();
    }

    /**
     * Requires at least the usage given by {@code cpu}.
     *
     * @param cpu the CPU usage to use
     */
    public void requireCPU(CPUUsage cpu) {
        status = Objects.requireNonNullElseGet(cpu, CPUUsage::newUsageWithoutLimits);
        setupExecutor();
        taskExecutor.ifPresent(e -> addTasks());
    }

    /**
     * Flushes and deletes the runner, if present.
     */
    public void stop() {
        taskExecutor.ifPresent(ExecutorService::shutdownNow);
        taskExecutor = Optional.empty();
        status = CPUUsage.newUsageWithoutLimits();
    }

    /**
     * @return the current CPU status
     */
    public CPUUsage getCurrentStatus() {
        status.refreshAvailableCores();
        return status;
    }

    private void setupExecutor() {
        taskExecutor.ifPresent(ExecutorService::shutdownNow);
        if (status.limitsPresent()) {
            taskExecutor = Optional.of(Executors.newScheduledThreadPool(status.getAvailableCores()));
        }
    }

    /**
     * Adds {@link CPUUsage#getAvailableCores()}} tasks to the periodically running executor completely blocking one
     * core, each running for a duration of {@code requestedCPUPercentage * intervalLength / availableCores}.
     */
    private void addTasks() {
        if (taskExecutor.isEmpty() || !status.limitsPresent()) {
            return;
        }
        status.refreshAvailableCores();
        for (int i = 0; i < status.getAvailableCores(); ++i) {
            taskExecutor.orElseThrow().scheduleAtFixedRate(this::simulateWork, 0L,
                status.getInterval().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Occupies one core completely for {@link CPUUsage#limitInNanosecondsPerCore()} nanoseconds.<br>
     * Callers must ensure to call this method after every {@link CPUUsage#getInterval()}, so that the minimally
     * required CPU usage is {@link CPUUsage#getMinCPUUsagePerCore()}.
     */
    private void simulateWork() {
        long busyTime = status.limitInNanosecondsPerCore();
        long start = System.nanoTime();
        while (System.nanoTime() <= start + busyTime && !Thread.currentThread().isInterrupted()) {}
    }
}
