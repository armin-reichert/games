package de.amr.games.pacman.behavior;

import de.amr.games.pacman.ui.actor.MazeMover;

public interface RoutePlanner {

	Route computeRoute(MazeMover<?> mover);

}
