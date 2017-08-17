package de.amr.games.muehle.rules;

@FunctionalInterface
public interface TriFunction<U, V, W, R> {

	R apply(U u, V v, W w);
}
