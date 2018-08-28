package de.amr.games.birdy.entities;

import static de.amr.easy.game.sprite.AnimationType.BACK_AND_FORTH;
import static de.amr.easy.game.sprite.AnimationType.CYCLIC;
import static de.amr.games.birdy.utils.Util.randomInt;

import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;

/**
 * A shining and blinking star...
 * 
 * @author Armin Reichert
 */
public class Star extends GameEntityUsingSprites {

	private final Sprite s_star;

	public Star() {
		s_star = new Sprite("blink_00", "blink_01", "blink_02")
				.animate(new Random().nextBoolean() ? BACK_AND_FORTH : CYCLIC, randomInt(300, 2000));
	}

	@Override
	public int getWidth() {
		return currentSprite().getWidth();
	}

	@Override
	public int getHeight() {
		return currentSprite().getHeight();
	}

	@Override
	public Sprite currentSprite() {
		return s_star;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_star);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}
