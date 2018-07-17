package de.amr.games.pacman.ui;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.easy.game.math.Vector2f.sum;
import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.controller.GameController.debug;
import static de.amr.games.pacman.model.Tile.WALL;
import static de.amr.games.pacman.model.Tile.WORMHOLE;
import static java.lang.Math.round;

import java.awt.Graphics2D;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.GameEvent;
import de.amr.games.pacman.controller.GameEventListener;
import de.amr.games.pacman.controller.MoveBehavior;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * @param <State>
 *          type of state, for example {@link PacMan.State}
 */
public abstract class MazeMover<State> extends GameEntity {

	protected final Maze maze;
	protected MoveBehavior moveBehavior;
	private State state;
	protected long stateEntryTime;
	protected float speed;
	protected int moveDirection;
	protected int nextMoveDirection;

	protected MazeMover(Maze maze) {
		Objects.requireNonNull(maze);
		this.maze = maze;
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
		return TS;
	}

	@Override
	public int getHeight() {
		return TS;
	}

	protected abstract int getSpriteSize();

	public void setState(State state) {
		State oldState = this.state;
		this.state = state;
		stateEntryTime = System.currentTimeMillis();
		if (oldState != state) {
			debug(() -> System.out.println(String.format("%s changed from %s to %s", this, oldState, state)));
		}
	}

	public State getState() {
		return state;
	}

	public void setMoveBehavior(MoveBehavior behavior) {
		Objects.nonNull(behavior);
		this.moveBehavior = behavior;
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

	public int getMoveDirection() {
		return moveDirection;
	}

	public int getNextMoveDirection() {
		return nextMoveDirection;
	}

	public void setMoveDirection(int moveDirection) {
		this.moveDirection = moveDirection;
	}

	public void setNextMoveDirection(int nextMoveDirection) {
		this.nextMoveDirection = nextMoveDirection;
	}

	protected Tile getMazePosition(float x, float y) {
		return new Tile(round(x) / TS, round(y) / TS);
	}

	public void setMazePosition(int col, int row) {
		tf.moveTo(col * TS, row * TS);
	}

	public void setMazePosition(Tile tile) {
		setMazePosition(tile.col, tile.row);
	}

	public Tile getMazePosition() {
		return getMazePosition(tf.getX() + getWidth() / 2, tf.getY() + getHeight() / 2);
	}

	public boolean collidesWith(MazeMover<?> other) {
		return getCollisionBox().intersects(other.getCollisionBox());
	}

	public int row() {
		float centerY = tf.getY() + getHeight() / 2;
		return round(centerY) / TS;
	}

	public int col() {
		float centerX = tf.getX() + getWidth() / 2;
		return round(centerX) / TS;
	}

	public boolean isExactlyOverTile() {
		return round(tf.getX()) % TS == 0 && round(tf.getY()) % TS == 0;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public void move() {
		Tile currentTile = getMazePosition();
		if (maze.getContent(currentTile) == WORMHOLE) {
			teleport(currentTile);
		}
		if (canMove(moveDirection)) {
			tf.moveTo(computeExactMovePosition(moveDirection));
		} else { // position exactly over tile
			setMazePosition(getMazePosition());
		}
	}

	public boolean canMove(int dir) {
		Tile currentTile = getMazePosition();
		Tile touchedTile = computeTouchedTile(currentTile, dir);
		if (currentTile.equals(touchedTile)) {
			return true;
		}
		if (!maze.isValidTile(touchedTile)) {
			return false;
		}
		if (maze.getContent(touchedTile) == WALL) {
			return false;
		}
		if (dir == Maze.TOPOLOGY.right(moveDirection) || dir == Maze.TOPOLOGY.left(moveDirection)) {
			return isExactlyOverTile();
		}
		return true;
	}

	private Tile computeTouchedTile(Tile currentTile, int dir) {
		Vector2f newPosition = computeExactMovePosition(dir);
		float x = newPosition.x, y = newPosition.y;
		switch (dir) {
		case Top4.W:
			return new Tile(round(x) / TS, currentTile.row);
		case Top4.E:
			return new Tile(round(x + getWidth()) / TS, currentTile.row);
		case Top4.N:
			return new Tile(currentTile.col, round(y) / TS);
		case Top4.S:
			return new Tile(currentTile.col, round(y + getHeight()) / TS);
		default:
			throw new IllegalArgumentException("Illegal direction: " + dir);
		}
	}

	private void teleport(Tile wormhole) {
		if (moveDirection == Top4.E && wormhole.col > 0) {
			setMazePosition(0, wormhole.row);
		} else if (moveDirection == Top4.W && wormhole.col == 0) {
			setMazePosition(maze.numCols() - 1, wormhole.row);
		}
	}

	private Vector2f computeExactMovePosition(int dir) {
		return sum(tf.getPosition(), smul(speed, Vector2f.of(Maze.TOPOLOGY.dx(dir), Maze.TOPOLOGY.dy(dir))));
	}
}