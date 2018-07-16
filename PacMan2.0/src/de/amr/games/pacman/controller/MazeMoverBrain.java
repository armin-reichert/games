package de.amr.games.pacman.controller;

import de.amr.games.pacman.ui.MazeMover;

public interface MazeMoverBrain<Mover extends MazeMover<?>> {

	void think(Mover mazeMover);

}
