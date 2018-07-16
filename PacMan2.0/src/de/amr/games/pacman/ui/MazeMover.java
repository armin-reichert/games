package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Tile.WALL;
import static de.amr.games.pacman.model.Tile.WORMHOLE;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.GameEvent;
import de.amr.games.pacman.controller.GameEventListener;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;

/**
 *
 * @param <State>
 *          type of state
 */
public abstract class MazeMover<State> extends GameEntity {

	protected final Game game;

	private State state;
	protected long stateEntryTime;
	
	protected float speed;
	protected int moveDirection;
	protected int nextMoveDirection;

	protected MazeMover(Game game) {
		Objects.requireNonNull(game);
		this.game = game;
	}

	@Override
	public void draw(Graphics2D g) {
		// draw sprite centered over tile
		int offsetX = (getSpriteSize() - getWidth()) / 2, offsetY = (getSpriteSize() - getHeight()) / 2;
		g.translate(-offsetX, -offsetY);
		super.draw(g);
		g.translate(offsetX, offsetY);
	}

	@Override
	public int getWidth() {
		return PacManApp.TS;
	}

	@Override
	public int getHeight() {
		return PacManApp.TS;
	}

	protected abstract int getSpriteSize();

	public void setState(State state) {
		State oldState = this.state;
		this.state = state;
		stateEntryTime = System.currentTimeMillis();
		if (oldState != state) {
			System.out.println(String.format("%s changed from %s to %s", this, oldState, state));
		}
	}

	public State getState() {
		return state;
	}

	public int stateDurationSeconds() {
		return (int) (System.currentTimeMillis() - stateEntryTime) / 1000;
	}

	private final Set<GameEventListener> observers = new LinkedHashSet<>();

	public void addObserver(GameEventListener observer) {
		observers.add(observer);
	}

	public void removeObserver(GameEventListener observer) {
		observers.remove(observer);
	}

	protected void fireGameEvent(GameEvent event) {
		observers.forEach(observer -> observer.processGameEvent(event));
	}

	public void setMoveDirection(int moveDirection) {
		this.moveDirection = moveDirection;
	}

	public void setNextMoveDirection(int nextMoveDirection) {
		this.nextMoveDirection = nextMoveDirection;
	}

	protected int row(float y) {
		return Math.round(y) / PacManApp.TS;
	}

	protected int col(float x) {
		return Math.round(x) / PacManApp.TS;
	}

	protected Point getMazePosition(float x, float y) {
		return new Point(col(x), row(y));
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

	public boolean collidesWith(MazeMover<?> other) {
		return getCollisionBox().intersects(other.getCollisionBox());
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

	public boolean canMove(int dir) {
		Vector2f newPosition = computePosition(dir);
		int touchedCol, touchedRow;
		switch (dir) {
		case Top4.W:
			touchedCol = col(newPosition.x);
			touchedRow = row();
			break;
		case Top4.E:
			touchedCol = col(newPosition.x + getWidth());
			touchedRow = row();
			break;
		case Top4.N:
			touchedCol = col();
			touchedRow = row(newPosition.y);
			break;
		case Top4.S:
			touchedCol = col();
			touchedRow = row(newPosition.y + getHeight());
			break;
		default:
			throw new IllegalArgumentException("Illegal direction: " + dir);
		}
		if (col() == touchedCol && row() == touchedRow) {
			return true;
		}
		return game.maze.getContent(touchedCol, touchedRow) != WALL;
	}

	public void move() {
		int col = col(), row = row();
		if (game.maze.getContent(col, row) == WORMHOLE) {
			warp(col, row);
		} else if (canMove(moveDirection)) {
			tf.moveTo(computePosition(moveDirection));
		} else { // position exactly over tile
			setMazePosition(col, row);
		}
	}

	public void warp(int col, int row) {
		if (moveDirection == Top4.E && col == game.maze.numCols() - 1) {
			setMazePosition(1, row);
		} else if (moveDirection == Top4.W && col == 0) {
			setMazePosition(game.maze.numCols() - 2, row);
		}
	}

	public Vector2f computePosition(int dir) {
		Vector2f velocity = Vector2f.smul(speed, Vector2f.of(Maze.TOPOLOGY.dx(dir), Maze.TOPOLOGY.dy(dir)));
		return Vector2f.sum(tf.getPosition(), velocity);
	}
}