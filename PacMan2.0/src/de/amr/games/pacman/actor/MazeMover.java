package de.amr.games.pacman.actor;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.easy.game.math.Vector2f.sum;
import static de.amr.games.pacman.model.Content.WALL;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.TS;
import static java.lang.Math.round;

import java.awt.Graphics2D;
import java.util.Map;
import java.util.function.Function;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.game.GameEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.routing.Navigation;
import de.amr.games.pacman.routing.impl.NavigationSystem;
import de.amr.statemachine.StateMachine;

/**
 * @param <S> maze mover state type
 */
public abstract class MazeMover<S> extends GameEntity {

	private static final int TELEPORT_LENGTH = 3;

	public final Game game;
	public final Maze maze;
	public final Tile homeTile;
	private final Map<S, Navigation<MazeMover<?>>> navigation;
	private Function<MazeMover<S>, Float> fnSpeed;
	private int dir;
	private int nextDir;

	protected MazeMover(Game game, Tile homeTile, Map<S, Navigation<MazeMover<?>>> navigation) {
		this.game = game;
		this.maze = game.maze;
		this.homeTile = homeTile;
		this.navigation = navigation;
		this.fnSpeed = mover -> 0f;
	}

	// State machine

	protected abstract StateMachine<S, GameEvent> getStateMachine();

	public S getState() {
		return getStateMachine().currentState();
	}

	@Override
	public void init() {
		getStateMachine().init();
	}

	@Override
	public void update() {
		getStateMachine().update();
	}

	public void processEvent(GameEvent e) {
		getStateMachine().enqueue(e);
		getStateMachine().update();
	}

	// Display

	@Override
	public void draw(Graphics2D g) {
		// center sprite over collision box
		int dx = (getWidth() - currentSprite().getWidth()) / 2, dy = (getHeight() - currentSprite().getHeight()) / 2;
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

	// Movement

	public void setNavigation(S state, Navigation<MazeMover<?>> navigation) {
		this.navigation.put(state, navigation);
	}

	public Navigation<MazeMover<?>> getNavigation() {
		return navigation.getOrDefault(getState(), NavigationSystem.forward());
	}

	public float getSpeed() {
		return fnSpeed.apply(this);
	}

	public void setSpeed(Function<MazeMover<S>, Float> fnSpeed) {
		this.fnSpeed = fnSpeed;
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
		tf.moveTo(col * TS, row * TS);
	}

	public void setMazePosition(Tile tile) {
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

	public boolean isTeleporting() {
		Tile tile = getTile();
		return tile.row < 0 || tile.row >= maze.numRows() || tile.col < 0 || tile.col >= maze.numCols();
	}

	public void move() {
		Tile tile = getTile();
		if (isTeleporting()) {
			// teleport
			if (tile.col > (maze.numCols() - 1) + TELEPORT_LENGTH) {
				placeAt(0, tile.row);
			} else if (tile.col < -TELEPORT_LENGTH) {
				placeAt(maze.numCols() - 1, tile.row);
			} else {
				tf.moveTo(computePosition(dir));
			}
			return;
		}
		nextDir = getNavigation().computeRoute(this).getDirection();
		if (canMove(nextDir)) {
			dir = nextDir;
		}
		if (canMove(dir)) {
			tf.moveTo(computePosition(dir));
		} else {
			// adjust exactly over tile
			setMazePosition(tile);
		}
	}

	public boolean canMove(int direction) {
		Tile current = getTile();
		if (direction == Top4.W && current.col <= 0 || direction == Top4.E && current.col >= maze.numCols() - 1) {
			return true; // teleport
		}
		Tile next = computeNextTile(current, direction);
		if (next.equals(current)) {
			return true; // move stays inside tile
		}
		if (maze.getContent(next) == WALL) {
			return false;
		}
		if (direction == TOPOLOGY.right(dir) || direction == TOPOLOGY.left(dir)) {
			setMazePosition(getTile()); // TODO this is not 100% correct
			return isExactlyOverTile();
		}
		return true;
	}

	public Tile computeNextTile(Tile current, int dir) {
		Vector2f nextPosition = computePosition(dir);
		float x = nextPosition.x, y = nextPosition.y;
		switch (dir) {
		case Top4.W:
			return new Tile(round(x) / TS, current.row);
		case Top4.E:
			return new Tile(round(x + getWidth()) / TS, current.row);
		case Top4.N:
			return new Tile(current.col, round(y) / TS);
		case Top4.S:
			return new Tile(current.col, round(y + getHeight()) / TS);
		default:
			throw new IllegalArgumentException("Illegal direction: " + dir);
		}
	}

	private Vector2f computePosition(int dir) {
		Vector2f v_dir = Vector2f.of(TOPOLOGY.dx(dir), TOPOLOGY.dy(dir));
		return sum(tf.getPosition(), smul(getSpeed(), v_dir));
	}
}