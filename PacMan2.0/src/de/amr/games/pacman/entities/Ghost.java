package de.amr.games.pacman.entities;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.easy.util.StreamUtils;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.board.SpriteSheet;

public class Ghost extends BoardMover {

	public enum State {
		WALKING, FRIGHTENED, DEAD
	};

	private int color;
	private Sprite[] spriteWalking = new Sprite[4];
	private Sprite spriteFrightened;
	private Sprite[] spriteDead = new Sprite[4];
	private State state;
	public Ghost(Board board, int color) {
		super(board);
		this.color = color;
		top.dirs().forEach(dir -> {
			spriteWalking[dir] = new Sprite(SpriteSheet.getGhostImagesByDirection(color, dir));
			spriteWalking[dir].scale(Board.TILE_SIZE * 2, Board.TILE_SIZE * 2);
			spriteWalking[dir].makeAnimated(AnimationMode.BACK_AND_FORTH, 300);
		});
		spriteFrightened = new Sprite(SpriteSheet.getFrightenedGhostImages());
		spriteFrightened.scale(Board.TILE_SIZE * 2, Board.TILE_SIZE * 2);
		spriteFrightened.makeAnimated(AnimationMode.CYCLIC, 200);
		top.dirs().forEach(dir -> {
			spriteDead[dir] = new Sprite(SpriteSheet.getDeadGhostImage(dir));
			spriteDead[dir].scale(Board.TILE_SIZE * 2, Board.TILE_SIZE * 2);
		});
	}

	@Override
	public String toString() {
		switch (color) {
		case SpriteSheet.BLUE_GHOST:
			return "Inky";
		case SpriteSheet.ORANGE_GHOST:
			return "Clyde";
		case SpriteSheet.PINK_GHOST:
			return "Pinky";
		case SpriteSheet.RED_GHOST:
			return "Blinky";
		}
		throw new IllegalArgumentException("Illegal ghost color: " + color);
	}

	@Override
	public void init() {
		setMoveDirection(Top4.E);
		setNextMoveDirection(Top4.E);
		setSpeed(1f);
		setState(State.WALKING);
	}

	@Override
	public void update() {
		if (canMove(moveDirection)) {
			move();
		} else {
			int direction = moveDirection;
			do {
				direction = StreamUtils.randomElement(top.dirs()).getAsInt();
			} while (!canMove(direction));
			setMoveDirection(direction);
		}
		if (state == State.DEAD) {
			if (secondsInState() > 6) {
				setState(State.WALKING);
			}
		} else if (state == State.FRIGHTENED) {
			if (secondsInState() > 3) {
				setState(State.WALKING);
			}
		}
	}

	@Override
	public Sprite currentSprite() {
		if (state == State.WALKING) {
			return spriteWalking[moveDirection];
		} else if (state == State.FRIGHTENED) {
			return spriteFrightened;
		} else if (state == State.DEAD) {
			return spriteDead[moveDirection];
		}
		throw new IllegalStateException("Illegal ghost state: " + state);
	}

	public int getColor() {
		return color;
	}

	public void setState(State state) {
		this.state = state;
		stateChangeAt = System.currentTimeMillis();
	}
	
	public State getState() {
		return state;
	}
}