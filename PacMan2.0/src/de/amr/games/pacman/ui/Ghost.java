package de.amr.games.pacman.ui;

import static de.amr.easy.util.StreamUtils.randomElement;

import java.awt.Graphics2D;
import java.awt.Point;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.MazeContent;
import de.amr.games.pacman.model.Tile;

public class Ghost extends BoardMover<Ghost.State> {

	private static final int SIZE = MazeContent.TS * 2;

	public enum State {
		ATTACKING, FRIGHTENED, DEAD
	};

	private final int color;
	private final Sprite[] spriteNormal = new Sprite[4];
	private final Sprite spriteFrightened;
	private final Sprite[] spriteDead = new Sprite[4];

	public Ghost(MazeContent board, int color) {
		super(board);
		this.color = color;
		top.dirs().forEach(dir -> {
			spriteNormal[dir] = new Sprite(Spritesheet.getNormalGhostImages(color, dir)).scale(SIZE, SIZE);
			spriteNormal[dir].makeAnimated(AnimationMode.BACK_AND_FORTH, 300);
		});
		spriteFrightened = new Sprite(Spritesheet.getFrightenedGhostImages()).scale(SIZE, SIZE);
		spriteFrightened.makeAnimated(AnimationMode.CYCLIC, 200);
		top.dirs().forEach(dir -> {
			spriteDead[dir] = new Sprite(Spritesheet.getDeadGhostImage(dir)).scale(SIZE, SIZE);
		});
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
	public void init() {
		setMoveDirection(Top4.E);
		setNextMoveDirection(Top4.E);
		setSpeed(MazeContent.TS / 16f);
		setState(State.ATTACKING);
	}

	@Override
	public void update() {
		if (state == State.ATTACKING) {
			moveRandomly();
		} else if (state == State.DEAD) {
			moveRandomly();
			if (secondsInState() > 6) {
				setState(State.ATTACKING);
			}
		} else if (state == State.FRIGHTENED) {
			moveRandomly();
			if (secondsInState() > 3) {
				setState(State.ATTACKING);
			}
		}
	}
	
	private void moveRandomly() {
		if (canMove(moveDirection)) {
			move();
		} else {
			int direction = moveDirection;
			do {
				direction = randomElement(top.dirs()).getAsInt();
			} while (!canMove(direction));
			setMoveDirection(direction);
		}
	}

	@Override
	public boolean canMove(int direction) {
		boolean canMove = super.canMove(direction);
		if (!canMove) {
			return false;
		}
		Vector2f newPosition = getNewPosition(direction);
		Point newBoardPosition = MazeContent.position(newPosition.x, newPosition.y);
		if (board.getContent(newBoardPosition) == Tile.GHOSTHOUSE && getState() == State.DEAD) {
			return true;
		}
		return true;
	}

	@Override
	public void draw(Graphics2D g) {
		if (board.getContent(col(), row()) == Tile.GHOSTHOUSE) {
			g.translate(MazeContent.TS / 2, 0);
			super.draw(g);
			g.translate(-MazeContent.TS / 2, 0);
		} else {
			super.draw(g);
		}
	}

	@Override
	public Sprite currentSprite() {
		if (state == State.ATTACKING) {
			return spriteNormal[moveDirection];
		} else if (state == State.FRIGHTENED) {
			return spriteFrightened;
		} else if (state == State.DEAD) {
			return spriteDead[moveDirection];
		}
		throw new IllegalStateException("Illegal ghost state: " + state);
	}
}