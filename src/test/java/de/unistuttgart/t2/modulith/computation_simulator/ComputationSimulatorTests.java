package de.unistuttgart.t2.modulith.computation_simulator;

import de.unistuttgart.t2.modulith.computation_simulator.compute.IComputeOperation;
import de.unistuttgart.t2.modulith.computation_simulator.compute.PiCalculator;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the computation simulator.
 *
 * @author davidkopp
 */
@ActiveProfiles("test")
class ComputationSimulatorTests {

    @Test
    public void serviceCallsComputeOperation() {
        IComputeOperation<Double> piCalculator = new PiCalculator(100);
        ComputationSimulatorService service = new ComputationSimulatorService(piCalculator);
        service.doCompute();
    }

    @Test
    public void testPiCalculator() {
        IComputeOperation<Double> piCalculator = new PiCalculator(100000);
        Double pi = piCalculator.doCompute();
        assertEquals(Math.PI, pi, 0.01);
    }
}
