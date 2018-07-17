package de.amr.games.pacman.ui;

import java.awt.Graphics2D;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class Ghost extends MazeMover<Ghost.State> {

	public enum State {
		ATTACKING, SCATTERING, FRIGHTENED, DEAD, STARRED
	}

	private final int color;
	private final Sprite[] spriteNormal = new Sprite[4];
	private final Sprite[] spriteDead = new Sprite[4];
	private final Sprite spriteFrightened;
	private final Sprite[] allSprites;

	public Ghost(Maze maze, int color) {
		super(maze);
		this.color = color;
		Maze.TOPOLOGY.dirs().forEach(dir -> {
			spriteNormal[dir] = new Sprite(Spritesheet.getNormalGhostImages(color, dir)).scale(getSpriteSize(),
					getSpriteSize());
			spriteNormal[dir].makeAnimated(AnimationMode.BACK_AND_FORTH, 300);
			spriteDead[dir] = new Sprite(Spritesheet.getDeadGhostImage(dir)).scale(getSpriteSize(), getSpriteSize());
		});
		spriteFrightened = new Sprite(Spritesheet.getFrightenedGhostImages()).scale(getSpriteSize(), getSpriteSize());
		spriteFrightened.makeAnimated(AnimationMode.CYCLIC, 200);

		// TODO make setAnimated(boolean) work
		allSprites = new Sprite[spriteNormal.length + spriteDead.length + 1];
		System.arraycopy(spriteNormal, 0, allSprites, 0, spriteNormal.length);
		System.arraycopy(spriteDead, 0, allSprites, spriteNormal.length, spriteDead.length);
		allSprites[allSprites.length - 1] = spriteFrightened;
	}

	public int getColor() {
		return color;
	}

	@Override
	public String toString() {
		switch (color) {
		case Spritesheet.BLUE_GHOST:
			return "Inky";
		case Spritesheet.ORANGE_GHOST:
			return "Clyde";
		case Spritesheet.PINK_GHOST:
			return "Pinky";
		case Spritesheet.RED_GHOST:
			return "Blinky";
		}
		throw new IllegalArgumentException("Illegal ghost color: " + color);
	}

	@Override
	public void update() {
		if (getState() != State.STARRED) {
			move();
			setNextMoveDirection(moveBehaviour.getNextMoveDirection());
			if (canMove(nextMoveDirection)) {
				setMoveDirection(nextMoveDirection);
			}
		}
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
		if (maze.getContent(col(), row()) == Tile.GHOSTHOUSE) {
			g.translate(PacManApp.TS / 2, 0);
			super.draw(g);
			g.translate(-PacManApp.TS / 2, 0);
		} else {
			super.draw(g);
		}
	}

	@Override
	protected int getSpriteSize() {
		return PacManApp.TS * 2;
	}

	@Override
	public Sprite currentSprite() {
		if (getState() == State.ATTACKING || getState() == State.STARRED) {
			return spriteNormal[moveDirection];
		} else if (getState() == State.FRIGHTENED) {
			return spriteFrightened;
		} else if (getState() == State.DEAD) {
			return spriteDead[moveDirection];
		}
		throw new IllegalStateException("Illegal ghost state: " + getState());
	}

	@Override
	protected Sprite[] getSprites() {
		return allSprites;
	}
}