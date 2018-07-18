package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.Tile;

public class FoodFoundEvent implements GameEvent {

	public final Tile tile;
	public final char food;

	public FoodFoundEvent(Tile tile, char food) {
		this.tile = tile;
		this.food = food;
	}
}