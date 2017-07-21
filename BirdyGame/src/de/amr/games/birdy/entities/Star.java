package de.amr.games.birdy.entities;

import static de.amr.easy.game.sprite.AnimationMode.BACK_AND_FORTH;
import static de.amr.easy.game.sprite.AnimationMode.CYCLIC;
import static de.amr.games.birdy.utils.Util.randomInt;

import java.util.Random;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.birdy.play.BirdyGame;

/**
 * A shining and blinking star...
 * 
 * @author Armin Reichert
 */
public class Star extends GameEntity {

	public Star(BirdyGame app) {
		Sprite sprite = new Sprite(Assets.OBJECT, "blink_00", "blink_01", "blink_02");
		sprite.makeAnimated(new Random().nextBoolean() ? BACK_AND_FORTH : CYCLIC, randomInt(300, 2000));
		setSprites(sprite);
	}
}
