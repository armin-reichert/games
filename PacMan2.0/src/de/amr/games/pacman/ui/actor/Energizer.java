package de.amr.games.pacman.ui.actor;

import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.ui.Spritesheet;

public class Energizer extends GameEntity {

	private final Sprite s_energizer;

	public Energizer() {
		s_energizer = Spritesheet.getEnergizer();
	}

	@Override
	public Sprite currentSprite() {
		return s_energizer;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_energizer);
	}
}