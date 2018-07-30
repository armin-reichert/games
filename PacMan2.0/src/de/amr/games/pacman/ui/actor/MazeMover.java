package de.amr.games.pacman.ui.actor;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.easy.game.math.Vector2f.sum;
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
import de.amr.games.pacman.behavior.RoutePlanner;
import de.amr.games.pacman.behavior.impl.NavigationSystem;
import de.amr.games.pacman.controller.event.GameEventSupport;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeUI;
import de.amr.games.pacman.ui.PlaySceneInfo;

/**
 * @param <S>
 *          maze mover state type
 */
public abstract class MazeMover<S> extends GameEntity {

	public final GameEventSupport eventing = new GameEventSupport();
	public final Maze maze;
	public final Tile homeTile;
	private final String name;
	private final Map<S, RoutePlanner> navigation;
	private Function<MazeMover<S>, Float> fnSpeed;
	private int dir;
	private int nextDir;
	private S state;
	private long stateEntryTime;

	protected MazeMover(Maze maze, String name, Tile homeTile, Map<S, RoutePlanner> navigation) {
		Objects.requireNonNull(maze);
		Objects.requireNonNull(name);
		Objects.requireNonNull(homeTile);
		Objects.requireNonNull(navigation);
		this.maze = maze;
		this.name = name;
		this.homeTile = homeTile;
		this.navigation = navigation;
	}

	public String getName() {
		return name;
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
		return MazeUI.TS;
	}

	@Override
	public int getHeight() {
		return MazeUI.TS;
	}

	// State support

	public void setState(S state) {
		S oldState = this.state;
		this.state = state;
		stateEntryTime = System.currentTimeMillis();
		if (oldState != state) {
			PlaySceneInfo.LOG
					.info(() -> String.format("%s changed from %s to %s", getName(), oldState, state));
		}
	}

	public S getState() {
		return state;
	}

	public int stateSec() {
		return (int) (System.currentTimeMillis() - stateEntryTime) / 1000;
	}

	// Movement

	public void setNavigation(S state, RoutePlanner navigation) {
		this.navigation.put(state, navigation);
	}

	public RoutePlanner getNavigation() {
		return navigation.getOrDefault(getState(), NavigationSystem.forward());
	}

	public float getSpeed() {
		return fnSpeed.apply(this);
	}

	public void setSpeed(Function<MazeMover<S>, Float> speed) {
		this.fnSpeed = speed;
	}

	public int getDir() {
		return dir;
	}

	public void setDir(int dir) {
		this.dir = dir;
	}

	public int getNextDir() {
		return nextDir;
	}

	public void setNextDir(int dir) {
		this.nextDir = dir;
	}

	public void placeAt(int col, int row) {
		tf.moveTo(col * MazeUI.TS, row * MazeUI.TS);
	}

	public void placeAt(Tile tile) {
		placeAt(tile.col, tile.row);
	}

	public Tile getTile() {
		return new Tile(col(), row());
	}

	public int row() {
		return round(tf.getY() + getHeight() / 2) / MazeUI.TS;
	}

	public int col() {
		return round(tf.getX() + getWidth() / 2) / MazeUI.TS;
	}

	public boolean isExactlyOverTile() {
		return round(tf.getX()) % MazeUI.TS == 0 && round(tf.getY()) % MazeUI.TS == 0;
	}

	public void move() {
		Tile tile = getTile();
		if (maze.getContent(tile) == WORMHOLE) {
			if (dir == Top4.E && tile.col == maze.numCols() - 1 || dir == Top4.W && tile.col == 0) {
				placeAt(maze.numCols() - 1 - tile.col, tile.row);
			}
		}
		nextDir = getNavigation().computeRoute(this).getDirection();
		if (canMove(nextDir)) {
			dir = nextDir;
		}
		if (canMove(dir)) {
			tf.moveTo(computePosition(dir, fnSpeed.apply(this)));
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
		if (dir == TOPOLOGY.right(this.dir) || dir == TOPOLOGY.left(this.dir)) {
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
			return new Tile(round(x) / MazeUI.TS, from.row);
		case Top4.E:
			return new Tile(round(x + getWidth()) / MazeUI.TS, from.row);
		case Top4.N:
			return new Tile(from.col, round(y) / MazeUI.TS);
		case Top4.S:
			return new Tile(from.col, round(y + getHeight()) / MazeUI.TS);
		default:
			throw new IllegalArgumentException("Illegal direction: " + dir);
		}
	}

	private Vector2f computePosition(int dir, float speed) {
		Vector2f v_dir = Vector2f.of(TOPOLOGY.dx(dir), TOPOLOGY.dy(dir));
		return sum(tf.getPosition(), smul(speed, v_dir));
	}
}