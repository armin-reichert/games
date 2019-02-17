package de.amr.games.puzzle15.solver;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Solver base class.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractSolver implements Solver {

	private final Predicate<Solver> givingUpCondition;
	private int maxFrontierSize;
	private long startTime; // nanoseconds

	protected AbstractSolver(Predicate<Solver> givingUpCondition) {
		this.givingUpCondition = givingUpCondition;
	}

	protected List<Node> solution(Node goal) {
		List<Node> solution = new LinkedList<>();
		for (Node current = goal; current != null; current = current.getParent()) {
			solution.add(0, current);
		}
		return solution;
	}

	protected abstract int getFrontierSize();

	@Override
	public int getMaxFrontierSize() {
		return maxFrontierSize;
	}

	protected void startClock() {
		startTime = System.nanoTime();
	}

	@Override
	public long getRunningTime() {
		return (System.nanoTime() - startTime) / 1_000_000;
	}

	protected void resetMaxFrontierSize() {
		maxFrontierSize = 0;
	}

	protected void updateMaxFrontierSize() {
		int size = getFrontierSize();
		if (size > maxFrontierSize) {
			if (size / 10_000 > maxFrontierSize / 10_000) {
				System.out.println(String.format("Frontier size: %,d", size));
			}
			maxFrontierSize = size;
		}
	}

	protected void maybeGiveUp() throws SolverGivingUpException {
		if (givingUpCondition.test(this)) {
			throw new SolverGivingUpException(String.format("Frontier size: %d, running time %d millseconds",
					getFrontierSize(), getRunningTime()));
		}
	}
}