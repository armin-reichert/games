package de.amr.games.birdy.entities;

import java.util.Random;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.birdy.utils.Util;

public class Star extends GameEntity {

	public Star() {
		Sprite blinking = new Sprite("blink_00", "blink_01", "blink_02");
		blinking.createAnimation(
				new Random().nextBoolean() ? AnimationMode.BACK_AND_FORTH : AnimationMode.CYCLIC,
				Util.randomInt(300, 2000));
		setSprites(blinking);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}

}
