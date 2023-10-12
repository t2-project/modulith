package de.unistuttgart.t2.modulith.common.scaling;

import de.unistuttgart.t2.modulith.common.scaling.cpu.CPUManager;
import de.unistuttgart.t2.modulith.common.scaling.cpu.CPUUsage;
import de.unistuttgart.t2.modulith.common.scaling.cpu.CPUUsageRequest;
import de.unistuttgart.t2.modulith.common.scaling.memory.MemoryInfo;
import de.unistuttgart.t2.modulith.common.scaling.memory.MemoryLeaker;
import de.unistuttgart.t2.modulith.common.scaling.request.RequestDenier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

/**
 * Contains the routes that (deterministically) influence the scaling behavior.
 *
 * @author Leon Hofmeister
 */
@RestController
public class AutoscalingController {

    private static final Logger logger = LoggerFactory.getLogger(AutoscalingController.class);

    @Autowired
    CPUManager cpuManager;

    @Operation(summary = "Unblock all requests", description = "Lifts the block for any route", tags = "Block")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Successfully lifted the route block"))
    @PostMapping("/autoscaling/unblock-routes")
    public void unblockRoutes() {
        RequestDenier.shouldBlockAllRoutes(false);
        logger.info("Unblocked all non-autoscaling routes");
    }

    @Operation(summary = "Block all subsequent requests", description = "deterministically produces a SLO for all subsequent requests outside of \"/autoscaling/*\" ", tags = "Block")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Successfully blocked all further routes"))
    @PostMapping("/autoscaling/block-routes")
    public void blockRoutes() {
        RequestDenier.shouldBlockAllRoutes(true);
        logger.warn("Blocked all non-autoscaling routes");
    }

    @Operation(summary = "Ensures that consistently at least (100 * {memory})% memory is used", description = "{memory} must be a mathematical ratio ( {memory} ∈ (-∞, 1.0), i.e. 0.052 = 5.2% ), 0 disables the leak, negative values clear the leak.", tags = "Memory")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully demanded a memory usage of at least {memory}%"),
        @ApiResponse(responseCode = "400", description = "{memory} >= 1.0") })
    @PostMapping("/autoscaling/require-memory/ratio-{memory}")
    @ResponseBody
    public MemoryInfo requireMemory(@PathVariable(name = "memory") double memory) {
        MemoryLeaker.changeExpectedMemoryPercentage(memory);
        logger.warn("Required {}% memory", 100 * memory);
        return reportMemory();
    }

    @Operation(summary = "Ensures that consistently at least {memory}% is used", description = "{memory} must be a human percentage ( {memory} ∈ (-∞, 100), i.e. 50.2 = 50.2%), 0 disables the leak, negative values clear the leak.", tags = "Memory")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully demanded a memory usage of at least {memory}%"),
        @ApiResponse(responseCode = "400", description = "{memory} >= 100.0") })
    @PostMapping("/autoscaling/require-memory/{memory}-percent")
    @ResponseBody
    public MemoryInfo requireMemoryFromHumanPercentage(@PathVariable(name = "memory") double memory) {
        MemoryLeaker.changeExpectedMemoryFromHumanPercentage(memory);
        logger.warn("Required {}% memory", memory);
        return reportMemory();
    }

    @Operation(summary = "Clear the memory leak if it exists", description = "Let's the service return to its normal memory usage.", tags = "Memory")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Successfully cleared the memory leak"))
    @PostMapping("/autoscaling/clear-memory-leak")
    @ResponseBody
    public MemoryInfo clearMemoryLeak() {
        MemoryLeaker.clearMemoryLeak();
        logger.info("Cleared memory leak");
        return reportMemory();
    }

    @Operation(summary = "Disable adding more unneeded memory", description = "Needed when wanting to keep an already existing memory leak without increasing the leaked amount of memory once the GC frees some other memory.", tags = "Memory")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Successfully disabled the memory leak"))
    @PostMapping("/autoscaling/disable-memory-leak")
    @ResponseBody
    public MemoryInfo disableMemoryLeak() {
        MemoryLeaker.changeExpectedMemoryPercentage(0.0);
        logger.warn("Locked memory leak to its current size");
        return reportMemory();
    }

    @Operation(summary = "Show current memory information", description = "Returns how many bytes are currently used, free, and in total vailable.", tags = "Memory")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Successfully returned the current memory information"))
    @GetMapping(path = "/autoscaling/memory-info", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MemoryInfo getMemoryInformation() {
        logger.debug("Retrieved current memory information");
        return reportMemory();
    }

    @Operation(summary = "Ensures that consistently at least {cpu}% is used", description = "time unit = values known to https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/temporal/ChronoUnit.html#valueOf(java.lang.String), case insensitive, default seconds.\n"
        + "CPU percentage defines the percentage of CPU to use at all times, for all cores combined.\n"
        + "{cpu percentage} must be a mathematical ratio ( {cpu percentage} ∈ (-∞, 1.0 * {number of available cores}), i.e. 7.5 = 750.5% (full load for 7 cores and a half)).\n"
        + "{cpu percentage} <= 0 disables the CPU waste.\n"
        + "The mechanism works by using 100% per core for an interval of length {requested CPU percentage} * {interval length} / {number of cores} periodically.\n"
        + "Interval length decides how long the interval is in {time unit}. Default 10.", tags = "CPU")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully demanded a CPU usage of at least {cpu}%") })
    @PostMapping("/autoscaling/require-cpu")
    @ResponseBody
    public CPUUsage requireCPU(@RequestBody @Valid CPUUsageRequest cpu) {
        logger.warn("Got a request to adapt CPU Usage to {}", cpu);
        cpu.setErrorHandler(d -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("Cannot require %d%% CPU.", d * 100));
        });
        cpuManager.requireCPU(cpu.convertFromRatio());
        return reportCPU();
    }

    @Operation(summary = "Ensures that consistently at least {cpu}% is used", description = "time unit = values known to https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/temporal/ChronoUnit.html#valueOf(java.lang.String), case insensitive, default seconds.\n"
        + "CPU percentage defines the percentage of CPU to use at all times, for all cores combined.\n"
        + "{cpu percentage} must be a human percentage ( {cpu percentage} ∈ (-∞, 100.0 * {number of available cores}), i.e. 750.5 = 750.5% (full load for 7 cores and a half)).\n"
        + "{cpu percentage} <= 0 disables the CPU waste.\n"
        + "The mechanism works by using 100% per core for an interval of length {requested CPU percentage} * {interval length} / {number of cores} periodically.\n"
        + "Interval length decides how long the interval is in {time unit}. Default 10.", tags = "CPU")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully demanded a CPU usage of at least {cpu}%") })
    @PostMapping("/autoscaling/require-cpu-from-humans")
    @ResponseBody
    public CPUUsage requireCPUFromHumanPercent(@RequestBody @Valid CPUUsageRequest cpu) {
        logger.warn("Got a request to adapt CPU Usage from human limits to {}", cpu);
        cpu.setErrorHandler(d -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Cannot require %d%% CPU.", d));
        });
        cpuManager.requireCPU(cpu.convertFromHumans());
        return reportCPU();
    }

    @Operation(summary = "Removes the requirement to use a minimum of CPU at all times", tags = "CPU")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Successfully disabled the minimum usage requirements for the CPU"))
    @PostMapping("/autoscaling/remove-cpu-usage-requirements")
    @ResponseBody
    public CPUUsage removeCPURequirements() {
        logger.info("Got a request to no longer inflate CPU usage artificially");
        cpuManager.stop();
        return reportCPU();
    }

    @Operation(summary = "Show current CPU information", description = "Returns how many cores are vailable, what interval is used and .", tags = "CPU")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Successfully returned the current CPU information"))
    @GetMapping(path = "/autoscaling/cpu-info", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CPUUsage getCPUInformation() {
        logger.debug("Retrieved current CPU information");
        return reportCPU();
    }

    private MemoryInfo reportMemory() {
        final MemoryInfo status = new MemoryInfo();
        logger.info("Memory stats: {}", status);
        return status;
    }

    private CPUUsage reportCPU() {
        final CPUUsage status = cpuManager.getCurrentStatus();
        logger.info("Current CPU Usage is {}", status);
        return status;
    }
}
