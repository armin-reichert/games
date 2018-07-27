package de.amr.games.pacman.behavior;

import de.amr.games.pacman.ui.MazeMover;

public interface MoveBehavior {

	Route getRoute(MazeMover<?> mover);

}
