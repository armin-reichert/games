package de.amr.games.pacman.ui.actor;

import static de.amr.games.pacman.ui.MazeUI.TS;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Pellet extends GameEntity {

	private int size = TS / 8;

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
		g.fillRect((TS - size) / 2, (TS - size) / 2, size, size);
	}
}
