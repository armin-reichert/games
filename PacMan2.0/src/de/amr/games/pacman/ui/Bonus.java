package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.Tile;

public class Bonus extends GameEntity {

	private char symbol;
	private int value;
	private Sprite sprite;

	public Bonus(char symbol, int value) {
		this.symbol = symbol;
		this.value = value;
		sprite = new Sprite(Spritesheet.getBonus(symbol)).scale(2 * TS, 2 * TS);
	}

	public int getValue() {
		return value;
	}

	public char getSymbol() {
		return symbol;
	}

	public Tile getTile() {
		return new Tile((int) tf.getX() / TS, (int) tf.getY() / TS);
	}

	@Override
	public Sprite currentSprite() {
		return sprite;
	}

	@Override
	public String toString() {
		return String.format("Bonus(%c)", symbol);
	}
}