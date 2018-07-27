package de.amr.games.pacman.ui;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.easy.game.math.Vector2f.sum;
import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.model.Tile.WALL;
import static de.amr.games.pacman.model.Tile.WORMHOLE;
import static java.lang.Math.round;

import java.awt.Graphics2D;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.behavior.MoveBehavior;
import de.amr.games.pacman.behavior.impl.Behaviors;
import de.amr.games.pacman.controller.event.GameEventSupport;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

/**
 * @param <S>
 *          maze mover state type
 */
public abstract class MazeMover<S> extends GameEntity {

	public final GameEventSupport observers = new GameEventSupport();
	public final Maze maze;
	public final Tile homeTile;
	private final Map<S, MoveBehavior> behaviorMap;
	private Function<MazeMover<S>, Float> fnSpeed;
	private int direction;
	private int intendedDirection;
	private S state;
	private long stateEntryTime;

	protected MazeMover(Maze maze, Tile homeTile, Map<S, MoveBehavior> behavior) {
		Objects.requireNonNull(maze);
		Objects.requireNonNull(homeTile);
		Objects.requireNonNull(behavior);
		this.maze = maze;
		this.homeTile = homeTile;
		this.behaviorMap = behavior;
	}

	@Override
	public void draw(Graphics2D g) {
		// center sprite over collision box
		int dx = (getWidth() - currentSprite().getWidth()) / 2,
				dy = (getHeight() - currentSprite().getHeight()) / 2;
		g.translate(dx, dy);
		super.draw(g);
		g.translate(-dx, -dy);
	}

	@Override
	public int getWidth() {
		return TS;
	}

	@Override
	public int getHeight() {
		return TS;
	}

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

	// Movement

	public void setBehavior(S state, MoveBehavior behavior) {
		this.behaviorMap.put(state, behavior);
	}

	public MoveBehavior getBehavior() {
		return behaviorMap.getOrDefault(getState(), Behaviors.forward());
	}

	public float getSpeed() {
		return fnSpeed.apply(this);
	}

	public void setSpeed(Function<MazeMover<S>, Float> speed) {
		this.fnSpeed = speed;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int dir) {
		this.direction = dir;
	}

	public int getIntendedDirection() {
		return intendedDirection;
	}

	public void setIntendedDirection(int dir) {
		this.intendedDirection = dir;
	}

	public void placeAt(int col, int row) {
		tf.moveTo(col * TS, row * TS);
	}

	public void placeAt(Tile tile) {
		placeAt(tile.col, tile.row);
	}

	public Tile getTile() {
		return new Tile(col(), row());
	}

	public int row() {
		return round(tf.getY() + getHeight() / 2) / TS;
	}

	public int col() {
		return round(tf.getX() + getWidth() / 2) / TS;
	}

	public boolean isExactlyOverTile() {
		return round(tf.getX()) % TS == 0 && round(tf.getY()) % TS == 0;
	}

	public void move() {
		Tile tile = getTile();
		if (maze.getContent(tile) == WORMHOLE) {
			if (direction == Top4.E && tile.col == maze.numCols() - 1
					|| direction == Top4.W && tile.col == 0) {
				placeAt(maze.numCols() - 1 - tile.col, tile.row);
			}
		}
		intendedDirection = getBehavior().getRoute(this).getDirection();
		if (canMove(intendedDirection)) {
			direction = intendedDirection;
		}
		if (canMove(direction)) {
			tf.moveTo(computePosition(direction, fnSpeed.apply(this)));
		} else {
			// adjust exactly over tile
			placeAt(tile);
		}
	}

	public boolean canMove(int dir) {
		Tile tile = getTile(), touched = computeTouchedTile(tile, dir);
		if (tile.equals(touched)) {
			return true;
		}
		if (!maze.isValidTile(touched) || maze.getContent(touched) == WALL) {
			return false;
		}
		if (dir == TOPOLOGY.right(direction) || dir == TOPOLOGY.left(direction)) {
			placeAt(getTile()); // TODO
			return isExactlyOverTile();
		}
		return true;
	}

	public Tile computeTouchedTile(Tile from, int dir) {
		Vector2f to = computePosition(dir, fnSpeed.apply(this));
		float x = to.x, y = to.y;
		switch (dir) {
		case Top4.W:
			return new Tile(round(x) / TS, from.row);
		case Top4.E:
			return new Tile(round(x + getWidth()) / TS, from.row);
		case Top4.N:
			return new Tile(from.col, round(y) / TS);
		case Top4.S:
			return new Tile(from.col, round(y + getHeight()) / TS);
		default:
			throw new IllegalArgumentException("Illegal direction: " + dir);
		}
	}

	private Vector2f computePosition(int dir, float speed) {
		Vector2f v_dir = Vector2f.of(TOPOLOGY.dx(dir), TOPOLOGY.dy(dir));
		return sum(tf.getPosition(), smul(speed, v_dir));
	}
}