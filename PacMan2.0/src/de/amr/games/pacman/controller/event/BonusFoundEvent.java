package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;
import de.amr.games.pacman.ui.PacMan.State;

public class BonusFoundEvent implements GameEvent {

	public final MazeMover<State> pacMan;
	public final Tile tile;
	public final char bonus;

	public BonusFoundEvent(MazeMover<State> pacMan, Tile tile, char bonus) {
		this.pacMan = pacMan;
		this.tile = tile;
		this.bonus = bonus;
	}
}