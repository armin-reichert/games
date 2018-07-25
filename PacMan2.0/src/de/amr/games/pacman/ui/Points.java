package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;

import java.awt.Graphics2D;
import java.util.Arrays;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Points extends GameEntity {

	private static int[] greenNumbers = { 200, 400, 800, 1600 };
	private Sprite sprite;

	public Points(int value) {
		sprite = createSprite(value).scale(2 * TS, 2 * TS);
	}

	private static Sprite createSprite(int value) {
		int index = Arrays.binarySearch(greenNumbers, value);
		if (index >= 0) {
			return new Sprite(Spritesheet.getGreenNumber(index));
		}
		throw new IllegalArgumentException("Points value not supported: " + value);
	}

	@Override
	public Sprite currentSprite() {
		return sprite;
	}

	@Override
	public void draw(Graphics2D g) {
		int offsetX = (getWidth() - 2 * TS) / 2, offsetY = (getHeight() - 2 * TS) / 2;
		g.translate(offsetX, offsetY);
		super.draw(g);
		g.translate(-offsetX, -offsetY);
	}
}