package de.amr.games.pacman.behavior;

import de.amr.games.pacman.ui.MazeMover;

public interface RoutePlanner {

	Route computeRoute(MazeMover<?> mover);

}
