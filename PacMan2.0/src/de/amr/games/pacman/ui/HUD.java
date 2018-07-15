package de.amr.games.pacman.ui;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.pacman.model.Game;

public class HUD extends GameEntity {

	private final Game gameState;

	public HUD(Game gameState) {
		this.gameState = gameState;
	}

}
