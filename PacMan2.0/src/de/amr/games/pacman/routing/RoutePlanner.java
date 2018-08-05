package de.amr.games.pacman.routing;

import de.amr.games.pacman.actor.MazeMover;

public interface RoutePlanner {

	Route computeRoute(MazeMover<?> mover);

}
