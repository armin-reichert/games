package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Bonus;

public class BonusFoundEvent extends GameEvent {

	public final Tile tile;
	public final Bonus bonus;

	public BonusFoundEvent(Tile tile, Bonus bonus) {
		this.tile = tile;
		this.bonus = bonus;
	}

	@Override
	public String toString() {
		return String.format("Bonus(%c)", bonus.getSymbol());
	}
}