package de.amr.games.pacman.actor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.ui.Spritesheet;

public class Pellet extends GameEntity {

	private int size = Spritesheet.TS / 8;

	@Override
	public Sprite currentSprite() {
		return null;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.empty();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.PINK);
		g.fillRect((Spritesheet.TS - size) / 2, (Spritesheet.TS - size) / 2, size, size);
	}
}
