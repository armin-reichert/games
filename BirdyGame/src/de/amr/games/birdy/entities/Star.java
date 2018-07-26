package de.amr.games.birdy.entities;

import static de.amr.easy.game.sprite.AnimationMode.BACK_AND_FORTH;
import static de.amr.easy.game.sprite.AnimationMode.CYCLIC;
import static de.amr.games.birdy.utils.Util.randomInt;

import java.util.Random;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

/**
 * A shining and blinking star...
 * 
 * @author Armin Reichert
 */
public class Star extends GameEntity {

	public Star() {
		Sprite sprite = new Sprite("blink_00", "blink_01", "blink_02");
		sprite.animation(new Random().nextBoolean() ? BACK_AND_FORTH : CYCLIC, randomInt(300, 2000));
		setSprites(sprite);
	}
}
