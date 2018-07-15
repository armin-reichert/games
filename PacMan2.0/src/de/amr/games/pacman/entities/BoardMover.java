package de.amr.games.pacman.entities;

import static de.amr.games.pacman.board.Tile.WALL;
import static de.amr.games.pacman.board.Tile.WORMHOLE;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashSet;
import java.util.Set;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.control.GameEvent;
import de.amr.games.pacman.control.GameEventListener;

public class BoardMover<State> extends GameEntity {

	private final Set<GameEventListener> observers = new LinkedHashSet<>();
	protected final Board board;
	protected final Topology top = new Top4();
	protected int moveDirection;
	protected int nextMoveDirection;
	protected float speed;
	protected State state;
	protected long stateChangeTime;

	public BoardMover(Board board) {
		this.board = board;
	}

	public void addObserver(GameEventListener observer) {
		observers.add(observer);
	}

	public void removeObserver(GameEventListener observer) {
		observers.remove(observer);
	}

	protected void fireGameEvent(GameEvent event) {
		observers.forEach(observer -> observer.dispatch(event));
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(-Board.TS / 2, -Board.TS / 2);
		super.draw(g);
		g.translate(Board.TS / 2, Board.TS / 2);
	}

	public void setMoveDirection(int moveDirection) {
		this.moveDirection = moveDirection;
	}

	public void setNextMoveDirection(int nextMoveDirection) {
		this.nextMoveDirection = nextMoveDirection;
	}

	public void setMazePosition(int col, int row) {
		tf.moveTo(col * Board.TS, row * Board.TS);
	}

	public void setMazePosition(Point pos) {
		setMazePosition(pos.x, pos.y);
	}

	public Point getMazePosition() {
		return Board.position(tf.getX(), tf.getY());
	}

	public boolean collidesWith(BoardMover<?> other) {
		Rectangle2D box = new Rectangle2D.Float(tf.getX(), tf.getY(), Board.TS, Board.TS);
		Rectangle2D otherBox = new Rectangle2D.Float(other.tf.getX(), other.tf.getY(), Board.TS, Board.TS);
		return box.intersects(otherBox);
	}

	@Override
	public int getWidth() {
		return Board.TS;
	}

	@Override
	public int getHeight() {
		return Board.TS;
	}

	public int row() {
		return Board.row(tf.getY() + Board.TS / 2);
	}

	public int col() {
		return Board.col(tf.getX() + Board.TS / 2);
	}

	public boolean isExactlyOverTile() {
		return Math.round(tf.getX()) % Board.TS == 0 && Math.round(tf.getY()) % Board.TS == 0;
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
			newCol = Board.col(getNewPosition(direction).x + Board.TS);
			break;
		case Top4.N:
			newRow = Board.row(getNewPosition(direction).y);
			break;
		case Top4.S:
			newRow = Board.row(getNewPosition(direction).y + Board.TS);
			break;
		default:
			throw new IllegalArgumentException("Illegal direction: " + direction);
		}
		if (col() == newCol && row() == newRow) {
			return true;
		}
		if (!board.grid.isValidCol(newCol) || !board.grid.isValidRow(newRow)) {
			return false;
		}
		return board.getContent(newCol, newRow) != WALL;
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
		if (board.getContent(col, row) == WORMHOLE) {
			warp(col, row);
		} else if (canMove(moveDirection)) {
			tf.moveTo(getNewPosition(moveDirection));
		} else { // position exactly over tile
			setMazePosition(col, row);
		}
	}

	public void warp(int col, int row) {
		if (moveDirection == Top4.E && col == board.numCols() - 1) {
			setMazePosition(1, row);
		} else if (moveDirection == Top4.W && col == 0) {
			setMazePosition(board.numCols() - 2, row);
		}
	}

	public Vector2f getNewPosition(int direction) {
		Vector2f velocity = Vector2f.smul(speed, Vector2f.of(top.dx(direction), top.dy(direction)));
		return Vector2f.sum(tf.getPosition(), velocity);
	}

	public void setState(State state) {
		State oldState = this.state;
		this.state = state;
		stateChangeTime = System.currentTimeMillis();
		if (oldState != state) {
			System.out.println(String.format("%s changed from %s to %s", this, oldState, state));
		}
	}

	public State getState() {
		return state;
	}

	public int secondsInState() {
		return (int) (System.currentTimeMillis() - stateChangeTime) / 1000;
	}

}