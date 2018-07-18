package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.Tile;

public class BonusFoundEvent implements GameEvent {

	public final Tile tile;
	public final char bonus;

	public BonusFoundEvent(Tile tile, char bonus) {
		this.tile = tile;
		this.bonus = bonus;
	}
}