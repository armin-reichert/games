package de.amr.games.pacman.board;

import static de.amr.games.pacman.board.Tile.EMPTY;
import static de.amr.games.pacman.board.Tile.ENERGIZER;
import static de.amr.games.pacman.board.Tile.PELLET;
import static de.amr.games.pacman.board.Tile.WORMHOLE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

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

	public PacMan(Board board) {
		super(board);
		readSprites();
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
		setMazePosition(5, 1);
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

	private void readInput() {
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			nextMoveDirection = Top4.W;
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			nextMoveDirection = Top4.E;
		} else if (Keyboard.keyDown(KeyEvent.VK_DOWN)) {
			nextMoveDirection = Top4.S;
		} else if (Keyboard.keyDown(KeyEvent.VK_UP)) {
			nextMoveDirection = Top4.N;
		}
	}

	@Override
	public void update() {
		readInput();
		eat();
		moveIfPossible();
	}

	private void eat() {
		int col = col(), row = row();
		switch (board.getTile(col, row)) {
		case PELLET:
			board.setTile(col, row, EMPTY);
			break;
		case ENERGIZER:
			board.setTile(col, row, EMPTY);
			break;
		case Tile.BONUS:
			board.setTile(col, row, EMPTY);
		default:
			break;
		}
	}

	private void moveIfPossible() {
		if (nextMoveDirection != moveDirection) {
			changeDirectionIfPossible();
		}
		int col = col(), row = row();
		if (canMove(moveDirection)) {
			tf.moveTo(getNewPosition(moveDirection));
			if (board.getTile(col, row) == WORMHOLE) {
				if (moveDirection == Top4.E && col == board.getNumCols() - 1) {
					setMazePosition(0, row);
				} else if (moveDirection == Top4.W && col == 0) {
					setMazePosition(board.getNumCols() - 1, row);
				}
			}
		} else {
			// position exactly over tile
			setMazePosition(col, row);
		}
	}

	private Vector2f getNewPosition(int direction) {
		Vector2f velocity = Vector2f.smul(speed, Vector2f.of(top.dx(direction), top.dy(direction)));
		return Vector2f.sum(tf.getPosition(), velocity);
	}

	private void changeDirectionIfPossible() {
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