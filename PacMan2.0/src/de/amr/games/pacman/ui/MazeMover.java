package de.amr.games.pacman.ui;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.easy.game.math.Vector2f.sum;
import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Tile.WALL;
import static de.amr.games.pacman.model.Tile.WORMHOLE;
import static java.lang.Math.round;

import java.awt.Graphics2D;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.behavior.DoNothing;
import de.amr.games.pacman.controller.behavior.MoveBehavior;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameEventListener;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public abstract class MazeMover<S> extends GameEntity {

	protected final Maze maze;
	protected final Map<S, MoveBehavior> moveBehavior;
	private final MoveBehavior defaultMoveBehavior;
	private float speed;
	private int moveDirection;
	private int nextMoveDirection;
	private S state;
	private long stateEntryTime;

	protected MazeMover(Maze maze, Map<S, MoveBehavior> moveBehavior) {
		Objects.requireNonNull(maze);
		this.maze = maze;
		this.moveBehavior = moveBehavior;
		defaultMoveBehavior = new DoNothing(this);
	}

	@Override
	public void draw(Graphics2D g) {
		// draw sprite centered over collision box
		int offsetX = (getWidth() - getSpriteSize()) / 2, offsetY = (getHeight() - getSpriteSize()) / 2;
		g.translate(offsetX, offsetY);
		super.draw(g);
		g.translate(-offsetX, -offsetY);
	}

	@Override
	public int getWidth() {
		return TS;
	}

	@Override
	public int getHeight() {
		return TS;
	}

	public abstract int getSpriteSize();

	// State support

	public void setState(S state) {
		S oldState = this.state;
		this.state = state;
		stateEntryTime = System.currentTimeMillis();
		if (oldState != state) {
			Debug.log(() -> String.format("%s changed from %s to %s", getName(), oldState, state));
		}
	}

	public S getState() {
		return state;
	}

	public int stateDurationSeconds() {
		return (int) (System.currentTimeMillis() - stateEntryTime) / 1000;
	}

	// GameEvent observer support

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

	// Movement

	public void setMoveBehavior(S state, MoveBehavior behavior) {
		moveBehavior.put(state, behavior);
	}

	public MoveBehavior currentMoveBehavior() {
		return moveBehavior.getOrDefault(getState(), defaultMoveBehavior);
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public int getMoveDirection() {
		return moveDirection;
	}

	public void setMoveDirection(int moveDirection) {
		this.moveDirection = moveDirection;
	}

	public int getNextMoveDirection() {
		return nextMoveDirection;
	}

	public void setNextMoveDirection(int nextMoveDirection) {
		this.nextMoveDirection = nextMoveDirection;
	}

	public void setTile(int col, int row) {
		tf.moveTo(col * TS, row * TS);
	}

	public void setTile(Tile tile) {
		setTile(tile.col, tile.row);
	}

	public Tile getTile() {
		return new Tile(col(), row());
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

	public void move() {
		nextMoveDirection = currentMoveBehavior().getNextMoveDirection();
		if (canMove(nextMoveDirection)) {
			moveDirection = nextMoveDirection;
		}
		Tile currentTile = getTile();
		if (maze.getContent(currentTile) == WORMHOLE) {
			if (moveDirection == Top4.E && currentTile.col == maze.numCols() - 1
					|| moveDirection == Top4.W && currentTile.col == 0) {
				setTile(maze.numCols() - 1 - currentTile.col, currentTile.row);
			}
		}
		if (canMove(moveDirection)) {
			tf.moveTo(computeMovePosition(moveDirection, speed));
		} else { // adjust exactly over tile
			setTile(currentTile);
		}
	}

	public boolean canMove(int dir) {
		Tile currentTile = getTile();
		Tile touchedTile = computeTouchedTile(currentTile, dir);
		if (currentTile.equals(touchedTile)) {
			return true;
		}
		if (!maze.isValidTile(touchedTile) || maze.getContent(touchedTile) == WALL) {
			return false;
		}
		if (dir == Maze.TOPOLOGY.right(moveDirection) || dir == Maze.TOPOLOGY.left(moveDirection)) {
			return isExactlyOverTile();
		}
		return true;
	}

	private Tile computeTouchedTile(Tile currentTile, int dir) {
		Vector2f newPosition = computeMovePosition(dir, speed);
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

	private Vector2f computeMovePosition(int dir, float speed) {
		Vector2f dirVector = Vector2f.of(Maze.TOPOLOGY.dx(dir), Maze.TOPOLOGY.dy(dir));
		Vector2f velocity = smul(speed, dirVector);
		// TODO insert micro-move such that taking turn is always possible
		return sum(tf.getPosition(), velocity);
	}
}