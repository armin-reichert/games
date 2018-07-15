package de.amr.games.pacman.ui;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.pacman.model.GameState;

public class HUD extends GameEntity {

	private final GameState gameState;

	public HUD(GameState gameState) {
		this.gameState = gameState;
	}

}
