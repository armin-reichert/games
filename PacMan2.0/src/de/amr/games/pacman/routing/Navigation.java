package de.amr.games.pacman.routing;

public interface Navigation<T> {

	MazeRoute computeRoute(T target);
}