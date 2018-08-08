package de.amr.games.pacman.actor;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.ui.Spritesheet;

public class Bonus extends GameEntity {

	private static final int[] BONUS_POINTS = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	private BonusSymbol symbol;
	private int value;
	private boolean honored;
	private Sprite s_symbol;
	private Sprite s_points;

	public Bonus(BonusSymbol symbol, int value) {
		this.symbol = symbol;
		this.value = value;
		this.honored = false;
		int index = Arrays.binarySearch(BONUS_POINTS, value);
		if (index < 0) {
			throw new IllegalArgumentException("Illegal bonus value: " + value);
		}
		int size = 2 * Spritesheet.TS;
		s_symbol = Spritesheet.symbol(symbol).scale(size);
		s_points = Spritesheet.pinkNumber(index).scale(size);
	}

	public int getValue() {
		return value;
	}

	public BonusSymbol getSymbol() {
		return symbol;
	}

	public boolean isHonored() {
		return honored;
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
	public void draw(Graphics2D g) {
		// center sprite over tile
		int dx = (getWidth() - currentSprite().getWidth()) / 2, dy = (getHeight() - currentSprite().getHeight()) / 2;
		g.translate(dx, dy);
		super.draw(g);
		g.translate(-dx, -dy);
	}


	@Override
	public String toString() {
		return String.format("Bonus(%s,%d)", symbol, value);
	}
}