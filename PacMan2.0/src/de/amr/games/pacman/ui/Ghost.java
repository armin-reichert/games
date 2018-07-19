package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;

import java.awt.Graphics2D;
import java.util.EnumMap;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class Ghost extends MazeMover<Ghost.State> {

	public enum State {
		ATTACKING, SCATTERING, FRIGHTENED, DEAD, STARRED
	}

	private final Sprite[] spriteNormal = new Sprite[4];
	private final Sprite[] spriteDead = new Sprite[4];
	private final Sprite spriteFrightened;
	private final Sprite[] allSprites;

	public Ghost(Maze maze, String name, int color) {
		super(maze, new EnumMap<>(State.class));
		setName(name);
		Maze.TOPOLOGY.dirs().forEach(dir -> {
			spriteNormal[dir] = new Sprite(Spritesheet.getNormalGhostImages(color, dir)).scale(getSpriteSize(),
					getSpriteSize());
			spriteNormal[dir].makeAnimated(AnimationMode.BACK_AND_FORTH, 300);
			spriteDead[dir] = new Sprite(Spritesheet.getDeadGhostImage(dir)).scale(getSpriteSize(), getSpriteSize());
		});
		spriteFrightened = new Sprite(Spritesheet.getFrightenedGhostImages()).scale(getSpriteSize(), getSpriteSize());
		spriteFrightened.makeAnimated(AnimationMode.CYCLIC, 200);

		// TODO HACK
		allSprites = new Sprite[spriteNormal.length + spriteDead.length + 1];
		System.arraycopy(spriteNormal, 0, allSprites, 0, spriteNormal.length);
		System.arraycopy(spriteDead, 0, allSprites, spriteNormal.length, spriteDead.length);
		allSprites[allSprites.length - 1] = spriteFrightened;
	}

	@Override
	public void update() {
		move();
		if (getState() == State.DEAD && stateDurationSeconds() > 6) {
			setState(State.ATTACKING);
		}
		if (getState() == State.FRIGHTENED && stateDurationSeconds() > 3) {
			setState(State.ATTACKING);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		// TODO hack
		if (maze.getContent(getTile()) == Tile.GHOSTHOUSE) {
			g.translate(TS / 2, 0);
			super.draw(g);
			g.translate(-TS / 2, 0);
		} else {
			super.draw(g);
		}
	}

	@Override
	public int getSpriteSize() {
		return TS * 2;
	}

	@Override
	public Sprite currentSprite() {
		switch (getState()) {
		case ATTACKING:
		case STARRED:
			return spriteNormal[getMoveDirection()];
		case FRIGHTENED:
			return spriteFrightened;
		case DEAD:
			return spriteDead[getMoveDirection()];
		default:
			throw new IllegalStateException("Illegal ghost state: " + getState());
		}
	}

	@Override
	protected Sprite[] getSprites() {
		return allSprites;
	}
}