package de.amr.games.pacman.board;

import static de.amr.games.pacman.board.Tile.*;
import static de.amr.games.pacman.board.Tile.PELLET;
import static de.amr.games.pacman.board.Tile.WORMHOLE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;

public class PacMan extends BoardMover {

	private static boolean DEBUG = false;

	private Sprite[] walkingSprites = new Sprite[4];
	private Sprite standingSprite;

	public Consumer<FoodEvent> fnFoodFound;

	public PacMan(Board board) {
		super(board);
		readSprites();
		fnFoodFound = e -> {
			System.out.println(String.format("Eat %s at col=%d, row=%d", e.food, e.col, e.row));
			board.setTile(e.col, e.row, Tile.EMPTY);
		};
	}

	private void readSprites() {
		BufferedImage sheet = Assets.readImage("sprites.png");
		standingSprite = new Sprite(sheet.getSubimage(488, 0, 15, 15));
		walkingSprites[Top4.E] = new Sprite(sheet.getSubimage(456, 0, 15, 15), sheet.getSubimage(472, 0, 15, 15));
		walkingSprites[Top4.W] = new Sprite(sheet.getSubimage(456, 16, 15, 15), sheet.getSubimage(472, 16, 15, 15));
		walkingSprites[Top4.N] = new Sprite(sheet.getSubimage(456, 32, 15, 15), sheet.getSubimage(472, 32, 15, 15));
		walkingSprites[Top4.S] = new Sprite(sheet.getSubimage(456, 48, 15, 15), sheet.getSubimage(472, 48, 15, 15));
		for (Sprite s : walkingSprites) {
			s.makeAnimated(AnimationMode.CYCLIC, 100);
		}
	}

	@Override
	public void init() {
		setMazePosition(14, 23);
		setSpeed(Board.TILE_SIZE / 8f);
		setMoveDirection(Top4.E);
		setNextMoveDirection(Top4.E);
	}

	@Override
	public void draw(Graphics2D g) {
		if (DEBUG) {
			g.translate(tf.getX(), tf.getY());
			g.setColor(isExactlyOverTile() ? Color.GREEN : Color.YELLOW);
			g.fillRect(0, 0, Board.TILE_SIZE, Board.TILE_SIZE);
			g.translate(-tf.getX(), -tf.getY());
		} else {
			g.translate(-Board.TILE_SIZE / 2, -Board.TILE_SIZE / 2);
			super.draw(g);
			g.translate(Board.TILE_SIZE / 2, Board.TILE_SIZE / 2);
		}
	}

	@Override
	public Sprite currentSprite() {
		if (!canMove(moveDirection)) {
			return standingSprite;
		}
		return walkingSprites[moveDirection];
	}

	@Override
	public void update() {
		lookForFood();
		readNextMoveDirection();
		changeDirection();
		move();
	}

	private void readNextMoveDirection() {
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			setNextMoveDirection(Top4.W);
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			setNextMoveDirection(Top4.E);
		} else if (Keyboard.keyDown(KeyEvent.VK_DOWN)) {
			setNextMoveDirection(Top4.S);
		} else if (Keyboard.keyDown(KeyEvent.VK_UP)) {
			setNextMoveDirection(Top4.N);
		}
	}

	private void lookForFood() {
		int col = col(), row = row();
		char tile = board.getTile(col, row);
		switch (tile) {
		case PELLET:
		case ENERGIZER:
			fnFoodFound.accept(new FoodEvent(col, row, tile));
			break;
		case BONUS_APPLE:
		case BONUS_BELL:
		case BONUS_CHERRIES:
		case BONUS_GALAXIAN:
		case BONUS_GRAPES:
		case BONUS_KEY:
		case BONUS_PEACH:
		case BONUS_STRAWBERRY:
			fnFoodFound.accept(new FoodEvent(col, row, tile));
			break;
		default:
			break;
		}
	}

	private void move() {
		int col = col(), row = row();
		if (board.getTile(col, row) == WORMHOLE) {
			warp(col, row);
		} else if (canMove(moveDirection)) {
			tf.moveTo(getNewPosition(moveDirection));
		} else {
			// position exactly over tile
			setMazePosition(col, row);
		}
	}

	private void warp(int col, int row) {
		if (moveDirection == Top4.E && col == board.getNumCols() - 1) {
			setMazePosition(0, row);
		} else if (moveDirection == Top4.W && col == 0) {
			setMazePosition(board.getNumCols() - 1, row);
		}
		tf.moveTo(getNewPosition(moveDirection));
	}

	private Vector2f getNewPosition(int direction) {
		Vector2f velocity = Vector2f.smul(speed, Vector2f.of(top.dx(direction), top.dy(direction)));
		return Vector2f.sum(tf.getPosition(), velocity);
	}

	private void changeDirection() {
		if (nextMoveDirection == moveDirection) {
			return;
		}
		if (nextMoveDirection == top.inv(moveDirection) || isExactlyOverTile() && canMove(nextMoveDirection)) {
			moveDirection = nextMoveDirection;
		}
	}

	private boolean canMove(int direction) {
		int nextCol = col(), nextRow = row();
		Vector2f newPosition;
		switch (direction) {
		case Top4.W:
			newPosition = getNewPosition(direction);
			nextCol = Board.col(newPosition.x);
			break;
		case Top4.E:
			newPosition = getNewPosition(direction);
			nextCol = Board.col(newPosition.x + Board.TILE_SIZE);
			break;
		case Top4.N:
			newPosition = getNewPosition(direction);
			nextRow = Board.row(newPosition.y);
			break;
		case Top4.S:
			newPosition = getNewPosition(direction);
			nextRow = Board.row(newPosition.y + Board.TILE_SIZE);
			break;
		default:
			throw new IllegalArgumentException("Illegal direction value: " + direction);
		}
		if (col() == nextCol && row() == nextRow) {
			return true;
		}
		if (!board.getGrid().isValidCol(nextCol) || !board.getGrid().isValidRow(nextRow)) {
			return false;
		}
		return board.getTile(nextCol, nextRow) != Tile.WALL;
	}

}