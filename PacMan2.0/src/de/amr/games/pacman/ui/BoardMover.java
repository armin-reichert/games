package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Tile.WALL;
import static de.amr.games.pacman.model.Tile.WORMHOLE;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashSet;
import java.util.Set;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.GameEvent;
import de.amr.games.pacman.controller.GameEventListener;
import de.amr.games.pacman.model.GameState;
import de.amr.games.pacman.model.Maze;

public abstract class BoardMover<State> extends GameEntity {

	protected static int row(float y) {
		return Math.round(y) / PacManApp.TS;
	}

	protected static int col(float x) {
		return Math.round(x) / PacManApp.TS;
	}

	protected static Point getMazePosition(float x, float y) {
		return new Point(col(x), row(y));
	}

	protected final GameState gameState;
	private State state;
	protected int moveDirection;
	protected int nextMoveDirection;
	protected float speed;
	protected long stateChangeTime;

	protected BoardMover(GameState gameState) {
		this.gameState = gameState;
	}

	@Override
	public int getWidth() {
		return PacManApp.TS;
	}

	@Override
	public int getHeight() {
		return PacManApp.TS;
	}

	private final Set<GameEventListener> observers = new LinkedHashSet<>();

	public void addObserver(GameEventListener observer) {
		observers.add(observer);
	}

	public void removeObserver(GameEventListener observer) {
		observers.remove(observer);
	}

	protected void fireGameEvent(GameEvent event) {
		observers.forEach(observer -> observer.dispatch(event));
	}

	public void setMoveDirection(int moveDirection) {
		this.moveDirection = moveDirection;
	}

	public void setNextMoveDirection(int nextMoveDirection) {
		this.nextMoveDirection = nextMoveDirection;
	}

	public void setMazePosition(int col, int row) {
		tf.moveTo(col * TS, row * TS);
	}

	public void setMazePosition(Point pos) {
		setMazePosition(pos.x, pos.y);
	}

	public Point getMazePosition() {
		return getMazePosition(tf.getX(), tf.getY());
	}

	public boolean collidesWith(BoardMover<?> other) {
		Rectangle2D box = new Rectangle2D.Float(tf.getX(), tf.getY(), TS, TS);
		Rectangle2D otherBox = new Rectangle2D.Float(other.tf.getX(), other.tf.getY(), TS, TS);
		return box.intersects(otherBox);
	}

	public int row() {
		return row(tf.getY() + TS / 2);
	}

	public int col() {
		return col(tf.getX() + TS / 2);
	}

	public boolean isExactlyOverTile() {
		return Math.round(tf.getX()) % TS == 0 && Math.round(tf.getY()) % TS == 0;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public boolean canMove(int direction) {
		int newCol = col(), newRow = row();
		switch (direction) {
		case Top4.W:
			newCol = col(getNewPosition(direction).x);
			break;
		case Top4.E:
			newCol = col(getNewPosition(direction).x + TS);
			break;
		case Top4.N:
			newRow = row(getNewPosition(direction).y);
			break;
		case Top4.S:
			newRow = row(getNewPosition(direction).y + TS);
			break;
		default:
			throw new IllegalArgumentException("Illegal direction: " + direction);
		}
		if (col() == newCol && row() == newRow) {
			return true;
		}
		return gameState.maze.getContent(newCol, newRow) != WALL;
	}

	public void changeDirection() {
		if (nextMoveDirection == moveDirection) {
			return;
		}
		if (nextMoveDirection == Maze.TOPOLOGY.inv(moveDirection) || isExactlyOverTile() && canMove(nextMoveDirection)) {
			moveDirection = nextMoveDirection;
		}
	}

	public void move() {
		int col = col(), row = row();
		if (gameState.maze.getContent(col, row) == WORMHOLE) {
			warp(col, row);
		} else if (canMove(moveDirection)) {
			tf.moveTo(getNewPosition(moveDirection));
		} else { // position exactly over tile
			setMazePosition(col, row);
		}
	}

	public void warp(int col, int row) {
		if (moveDirection == Top4.E && col == gameState.maze.numCols() - 1) {
			setMazePosition(1, row);
		} else if (moveDirection == Top4.W && col == 0) {
			setMazePosition(gameState.maze.numCols() - 2, row);
		}
	}

	public Vector2f getNewPosition(int direction) {
		Vector2f velocity = Vector2f.smul(speed, Vector2f.of(Maze.TOPOLOGY.dx(direction), Maze.TOPOLOGY.dy(direction)));
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