package de.amr.games.pacman.ui;

import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Spritesheet;

public class Bonus extends GameEntity {

	private BonusSymbol symbol;
	private int points;
	private boolean honored;
	private Sprite s_symbol;
	private Sprite s_points;

	public Bonus(BonusSymbol symbol, int points) {
		this.symbol = symbol;
		this.points = points;
		this.honored = false;
		s_symbol = new Sprite(Spritesheet.getBonus(symbol)).scale(2 * MazeUI.TS, 2 * MazeUI.TS);
		int index = Arrays.binarySearch(Game.BONUS_POINTS, points);
		if (index >= 0) {
			s_points = new Sprite(Spritesheet.getPinkNumber(index)).scale(2 * MazeUI.TS, 2 * MazeUI.TS);
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
		return honored ? s_points : s_symbol;
	}
	
	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_points, s_symbol);
	}

	@Override
	public String toString() {
		return String.format("Bonus(%s)", symbol);
	}
}