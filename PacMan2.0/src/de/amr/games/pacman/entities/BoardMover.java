package de.amr.games.pacman.entities;

import static de.amr.games.pacman.board.Tile.WALL;
import static de.amr.games.pacman.board.Tile.WORMHOLE;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.board.Board;

public class BoardMover extends GameEntity {

	protected final Board board;
	protected final Topology top = new Top4();
	protected int moveDirection;
	protected int nextMoveDirection;
	protected float speed;

	public BoardMover(Board board) {
		this.board = board;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(-Board.TILE_SIZE / 2, -Board.TILE_SIZE / 2);
		super.draw(g);
		g.translate(Board.TILE_SIZE / 2, Board.TILE_SIZE / 2);
	}

	public void setMoveDirection(int moveDirection) {
		this.moveDirection = moveDirection;
	}

	public void setNextMoveDirection(int nextMoveDirection) {
		this.nextMoveDirection = nextMoveDirection;
	}

	public void setMazePosition(int col, int row) {
		tf.moveTo(col * Board.TILE_SIZE, row * Board.TILE_SIZE);
	}

	@Override
	public int getWidth() {
		return Board.TILE_SIZE;
	}

	@Override
	public int getHeight() {
		return Board.TILE_SIZE;
	}

	public int row() {
		return Board.row(tf.getY() + Board.TILE_SIZE / 2);
	}

	public int col() {
		return Board.col(tf.getX() + Board.TILE_SIZE / 2);
	}

	public boolean isExactlyOverTile() {
		return Math.round(tf.getX()) % Board.TILE_SIZE == 0 && Math.round(tf.getY()) % Board.TILE_SIZE == 0;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public boolean canMove(int direction) {
		int newCol = col(), newRow = row();
		switch (direction) {
		case Top4.W:
			newCol = Board.col(getNewPosition(direction).x);
			break;
		case Top4.E:
			newCol = Board.col(getNewPosition(direction).x + Board.TILE_SIZE);
			break;
		case Top4.N:
			newRow = Board.row(getNewPosition(direction).y);
			break;
		case Top4.S:
			newRow = Board.row(getNewPosition(direction).y + Board.TILE_SIZE);
			break;
		default:
			throw new IllegalArgumentException("Illegal direction: " + direction);
		}
		if (col() == newCol && row() == newRow) {
			return true;
		}
		if (!board.getGrid().isValidCol(newCol) || !board.getGrid().isValidRow(newRow)) {
			return false;
		}
		return board.getTile(newCol, newRow) != WALL;
	}

	public void changeDirection() {
		if (nextMoveDirection == moveDirection) {
			return;
		}
		if (nextMoveDirection == top.inv(moveDirection) || isExactlyOverTile() && canMove(nextMoveDirection)) {
			moveDirection = nextMoveDirection;
		}
	}

	public void move() {
		int col = col(), row = row();
		if (board.getTile(col, row) == WORMHOLE) {
			warp(col, row);
		} else if (canMove(moveDirection)) {
			tf.moveTo(getNewPosition(moveDirection));
		} else { // position exactly over tile
			setMazePosition(col, row);
		}
	}

	public void warp(int col, int row) {
		if (moveDirection == Top4.E && col == board.getNumCols() - 1) {
			setMazePosition(1, row);
		} else if (moveDirection == Top4.W && col == 0) {
			setMazePosition(board.getNumCols() - 2, row);
		}
	}

	public Vector2f getNewPosition(int direction) {
		Vector2f velocity = Vector2f.smul(speed, Vector2f.of(top.dx(direction), top.dy(direction)));
		return Vector2f.sum(tf.getPosition(), velocity);
	}

}