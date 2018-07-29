package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;

import java.util.Arrays;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Spritesheet;

public class Bonus extends GameEntity {

	private boolean honored;
	private BonusSymbol symbol;
	private int points;
	private Sprite spriteSymbol;
	private Sprite spritePoints;

	public Bonus(BonusSymbol symbol, int points) {
		this.symbol = symbol;
		this.points = points;
		this.honored = false;
		spriteSymbol = new Sprite(Spritesheet.getBonus(symbol)).scale(2 * TS, 2 * TS);
		int index = Arrays.binarySearch(Game.BONUS_POINTS, points);
		if (index >= 0) {
			spritePoints = new Sprite(Spritesheet.getPinkNumber(index)).scale(2 * TS, 2 * TS);
		} else {
			throw new IllegalArgumentException("Bonus value not supported: " + points);
		}
	}

	public int getValue() {
		return points;
	}

	public BonusSymbol getSymbol() {
		return symbol;
	}

	public void setHonored() {
		honored = true;
	}

	@Override
	public Sprite currentSprite() {
		return honored ? spritePoints : spriteSymbol;
	}

	@Override
	public String toString() {
		return String.format("Bonus(%s)", symbol);
	}
}