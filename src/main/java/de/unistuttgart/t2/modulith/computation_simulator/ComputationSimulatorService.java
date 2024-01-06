package de.unistuttgart.t2.modulith.computation_simulator;

import de.unistuttgart.t2.modulith.computation_simulator.compute.IComputeOperation;
import de.unistuttgart.t2.modulith.computation_simulator.compute.PiCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Service that invokes the intensive computation simulation.
 *
 * @author davidkopp
 */
@Lazy
@Component
public class ComputationSimulatorService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final IComputeOperation<?> computeOperation;

    /**
     * Default constructor: initializing PI calculation
     * @param piTotalPoints config parameter used by the PiCalculator
     */
    @Autowired
    public ComputationSimulatorService(@Value("${t2.computation-simulator.pi.totalPoints}") int piTotalPoints) {
        this.computeOperation = new PiCalculator(piTotalPoints);
    }

    public ComputationSimulatorService(IComputeOperation<?> computeOperation) {
        this.computeOperation = computeOperation;
    }

    public double doCompute() {
        LOG.info("Start compute operation '{}'.", computeOperation.getName());
        long startTime = System.currentTimeMillis();

        computeOperation.doCompute();

        long endTime = System.currentTimeMillis();
        long elapsedTime = (endTime - startTime);
        LOG.info("Compute operation finished after {} ms.", elapsedTime);
        return elapsedTime;
    }
}
