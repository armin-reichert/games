package de.amr.games.pacman.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Pellet extends GameEntity {

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
		g.fillRect(MazeUI.TS * 3 / 8, MazeUI.TS * 3 / 8, MazeUI.TS / 4, MazeUI.TS / 4);
	}
}
