package de.unistuttgart.t2.modulith.computation_simulator.compute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Calculates PI with a given number of points.
 *
 * @author davidkopp
 */
public class PiCalculator implements IComputeOperation<Double> {

    private final int totalPoints;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public PiCalculator(int totalPoints) {
        this.totalPoints = totalPoints;
        LOG.info("Initialized PiCalculator with totalPoints: {}", totalPoints);
    }

    @Override
    public String getName() {
        return "PiCalculator";
    }

    /**
     * Calculates PI with a given number of points.
     * Source: <a href="https://www.baeldung.com/java-monte-carlo-compute-pi">Baeldung</a>
     *
     * @return result of calculation
     */
    @Override
    public Double doCompute() {
        int insideCircle = 0;

        Random random = new Random();
        for (long i = 0; i < totalPoints; i++) {
            double x = random.nextDouble() * 2 - 1;
            double y = random.nextDouble() * 2 - 1;
            if (x * x + y * y <= 1) {
                insideCircle++;
            }
        }
        return 4.0 * insideCircle / totalPoints;
    }
}
