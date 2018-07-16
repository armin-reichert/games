package de.amr.games.pacman.ui;

import java.awt.Graphics2D;
import java.util.Objects;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.util.StreamUtils;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.Brain;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class Ghost extends MazeMover<Ghost.State> {

	public enum State {
		ATTACKING, SCATTERING, FRIGHTENED, DEAD
	}

	private Brain<Ghost> brain;
	private final int color;
	private final Sprite[] spriteNormal = new Sprite[4];
	private final Sprite[] spriteDead = new Sprite[4];
	private final Sprite spriteFrightened;

	public Ghost(Game game, int color) {
		super(game);
		this.color = color;
		Maze.TOPOLOGY.dirs().forEach(dir -> {
			spriteNormal[dir] = new Sprite(Spritesheet.getNormalGhostImages(color, dir)).scale(getSpriteSize(),
					getSpriteSize());
			spriteNormal[dir].makeAnimated(AnimationMode.BACK_AND_FORTH, 300);
		});
		spriteFrightened = new Sprite(Spritesheet.getFrightenedGhostImages()).scale(getSpriteSize(), getSpriteSize());
		spriteFrightened.makeAnimated(AnimationMode.CYCLIC, 200);
		Maze.TOPOLOGY.dirs().forEach(dir -> {
			spriteDead[dir] = new Sprite(Spritesheet.getDeadGhostImage(dir)).scale(getSpriteSize(), getSpriteSize());
		});
	}

	public void setBrain(Brain<Ghost> brain) {
		Objects.nonNull(brain);
		this.brain = brain;
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
		if (getState() == State.ATTACKING) {
			move();
			setNextMoveDirection(brain.recommendNextMoveDirection(this));
			if (canMove(nextMoveDirection)) {
				setMoveDirection(nextMoveDirection);
			}
		} else if (getState() == State.DEAD) {
			moveIntoGhosthouse();
			if (stateDurationSeconds() > 6) {
				setState(State.ATTACKING);
			}
		} else if (getState() == State.FRIGHTENED) {
			moveRandomly();
			if (stateDurationSeconds() > 3) {
				setState(State.ATTACKING);
			}
		}
	}

	private void moveIntoGhosthouse() {
		moveRandomly(); // TODO
	}

	private void moveRandomly() {
		move();
		nextMoveDirection = StreamUtils.randomElement(Maze.TOPOLOGY.dirs()).getAsInt();
		if (isExactlyOverTile() && nextMoveDirection != Maze.TOPOLOGY.inv(moveDirection) && canMove(nextMoveDirection)) {
			moveDirection = nextMoveDirection;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		// TODO hack
		if (game.maze.getContent(col(), row()) == Tile.GHOSTHOUSE) {
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
		if (getState() == State.ATTACKING) {
			return spriteNormal[moveDirection];
		} else if (getState() == State.FRIGHTENED) {
			return spriteFrightened;
		} else if (getState() == State.DEAD) {
			return spriteDead[moveDirection];
		}
		throw new IllegalStateException("Illegal ghost state: " + getState());
	}
}