package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;

import java.util.Arrays;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Points extends GameEntity {

	private static int[] greenNumbers = { 200, 400, 800, 1600 };
	private static int[] pinkNumbers = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
	private Sprite sprite;

	public Points(int value) {
		sprite = createSprite(value).scale(2 * TS, 2 * TS);
	}

	private static Sprite createSprite(int value) {
		int index = Arrays.binarySearch(greenNumbers, value);
		if (index >= 0) {
			return new Sprite(Spritesheet.getGreenNumber(index));
		}
		index = Arrays.binarySearch(pinkNumbers, value);
		if (index >= 0) {
			return new Sprite(Spritesheet.getPinkNumber(index));
		}
		throw new IllegalArgumentException("Points value not supported: " + value);
	}

	@Override
	public Sprite currentSprite() {
		return sprite;
	}
}