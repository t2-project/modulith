package de.unistuttgart.t2.modulith.computation_simulator.compute;

public interface IComputeOperation<T> {

    String getName();

    T doCompute();
}
