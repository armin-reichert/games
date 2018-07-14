package de.amr.games.pacman.entities;

import java.awt.Graphics2D;
import java.awt.Point;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.easy.util.StreamUtils;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.board.SpriteSheet;
import de.amr.games.pacman.board.Tile;

public class Ghost extends BoardMover<Ghost.State> {

	public enum State {
		WALKING, FRIGHTENED, DEAD
	};

	private int color;
	private Sprite[] spriteWalking = new Sprite[4];
	private Sprite spriteFrightened;
	private Sprite[] spriteDead = new Sprite[4];

	public Ghost(Board board, int color) {
		super(board);
		this.color = color;
		top.dirs().forEach(dir -> {
			spriteWalking[dir] = new Sprite(SpriteSheet.getGhostImagesByDirection(color, dir));
			spriteWalking[dir].scale(Board.TS * 2, Board.TS * 2);
			spriteWalking[dir].makeAnimated(AnimationMode.BACK_AND_FORTH, 300);
		});
		spriteFrightened = new Sprite(SpriteSheet.getFrightenedGhostImages());
		spriteFrightened.scale(Board.TS * 2, Board.TS * 2);
		spriteFrightened.makeAnimated(AnimationMode.CYCLIC, 200);
		top.dirs().forEach(dir -> {
			spriteDead[dir] = new Sprite(SpriteSheet.getDeadGhostImage(dir));
			spriteDead[dir].scale(Board.TS * 2, Board.TS * 2);
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
		setSpeed(Board.TS / 16f);
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
	public void draw(Graphics2D g) {
		if (board.getContent(col(), row()) == Tile.GHOSTHOUSE) {
			g.translate(Board.TS / 2, 0);
			super.draw(g);
			g.translate(-Board.TS / 2, 0);
		} else {
			super.draw(g);
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

	@Override
	public boolean canMove(int direction) {
		boolean canMove = super.canMove(direction);
		if (!canMove) {
			return false;
		}
		Vector2f newPosition = getNewPosition(direction);
		Point newBoardPosition = Board.position(newPosition.x, newPosition.y);
		if (board.getContent(newBoardPosition) == Tile.GHOSTHOUSE && getState() == State.DEAD) {
			return true;
		}
		return true;
	}

	public int getColor() {
		return color;
	}
}