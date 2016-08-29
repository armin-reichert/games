package de.amr.easy.grid.iterators;

import java.util.Collection;
import java.util.Iterator;

public class IteratorFactory<Cell> {

	private IteratorFactory() {
	}

	@SafeVarargs
	public final static <Cell> Iterator<Cell> seq(Iterator<Cell>... sources) {
		return new SequentialIterator<>(sources);
	}

	@SuppressWarnings("unchecked")
	public final static <Cell> Iterator<Cell> seq(Collection<Iterator<Cell>> sources) {
		Iterator<?>[] sourcesArray = new Iterator[sources.size()];
		sourcesArray = sources.toArray(sourcesArray);
		return seq((Iterator<Cell>[]) sourcesArray);
	}

	@SafeVarargs
	public final static <Cell> Iterator<Cell> par(Iterator<Cell>... sources) {
		return new ParallelIterator<>(sources);
	}

	@SuppressWarnings("unchecked")
	public final static <Cell> Iterator<Cell> par(Collection<Iterator<Cell>> sources) {
		Iterator<?>[] sourcesArray = new Iterator[sources.size()];
		sourcesArray = sources.toArray(sourcesArray);
		return par((Iterator<Cell>[]) sourcesArray);
	}
}