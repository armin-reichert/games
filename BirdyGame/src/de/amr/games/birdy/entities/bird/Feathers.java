package de.amr.games.birdy.entities.bird;

import static de.amr.games.birdy.Globals.BIRD_FLAP_DURATION_MILLIS;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;

public enum Feathers {
	YELLOW("bird0"), BLUE("bird1"), RED("bird2");

	private final Sprite sprite;

	private Feathers(String prefix) {
		sprite = new Sprite(prefix + "_0", prefix + "_1", prefix + "_2");
		sprite.createAnimation(AnimationMode.BACK_AND_FORTH, BIRD_FLAP_DURATION_MILLIS);
	}

	Sprite getSprite() {
		return sprite;
	}
}