package de.amr.games.pacman.actor;

import java.util.Optional;
import java.util.stream.Stream;

public interface Environment {

	static Environment EMPTYNESS = new Environment() {

		@Override
		public Stream<Ghost> activeGhosts() {
			return Stream.empty();
		}

		@Override
		public Optional<Bonus> activeBonus() {
			return Optional.empty();
		}
	};

	Stream<Ghost> activeGhosts();

	Optional<Bonus> activeBonus();

}
