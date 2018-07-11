package de.amr.games.pacman.entities;

import de.amr.easy.game.entity.GameEntity;
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

}