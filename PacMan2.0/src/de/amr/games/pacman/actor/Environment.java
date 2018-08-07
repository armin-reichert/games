package de.amr.games.pacman.actor;

import java.util.Optional;
import java.util.stream.Stream;

public interface Environment {

	static Environment EMPTYNESS = new Environment() {

		@Override
		public Stream<Ghost> ghosts() {
			return Stream.empty();
		}

		@Override
		public Optional<Bonus> bonus() {
			return Optional.empty();
		}
	};

	Stream<Ghost> ghosts();

	Optional<Bonus> bonus();

}
