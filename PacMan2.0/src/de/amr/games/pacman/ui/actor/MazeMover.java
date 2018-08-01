package de.amr.games.pacman.ui.actor;

import static de.amr.easy.game.math.Vector2f.smul;
import static de.amr.easy.game.math.Vector2f.sum;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.model.TileContent.WALL;
import static de.amr.games.pacman.ui.MazeUI.TS;
import static java.lang.Math.round;

import java.awt.Graphics2D;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameEventManager;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.routing.RoutePlanner;
import de.amr.games.pacman.routing.impl.NavigationSystem;
import de.amr.games.pacman.ui.MazeUI;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateTransition;

/**
 * @param <S>
 *          maze mover state type
 */
public abstract class MazeMover<S> extends GameEntity {

	private static final int TELEPORT_LENGTH = 3;

	public final GameEventManager eventMgr = new GameEventManager();

	public final Game game;
	public final Maze maze;
	public final Tile homeTile;
	private final String name;
	protected final StateMachine<S, GameEvent> sm;
	private final Map<S, RoutePlanner> navigation;
	private Function<MazeMover<S>, Float> fnSpeed;
	private int dir;
	private int nextDir;

	protected MazeMover(Game game, Maze maze, String name, Tile homeTile,
			Map<S, RoutePlanner> navigation) {
		Objects.requireNonNull(game);
		Objects.requireNonNull(maze);
		Objects.requireNonNull(name);
		Objects.requireNonNull(homeTile);
		Objects.requireNonNull(navigation);
		this.game = game;
		this.maze = maze;
		this.name = name;
		this.homeTile = homeTile;
		this.navigation = navigation;
		this.sm = createStateMachine();
		sm.setLogger(Application.LOG);
	}

	public String getName() {
		return name;
	}

	// State machine

	protected abstract StateMachine<S, GameEvent> createStateMachine();

	public S getState() {
		return sm.currentStateLabel();
	}

	@Override
	public void init() {
		sm.init();
	}

	@Override
	public void update() {
		sm.update();
	}

	public void processEvent(GameEvent e) {
		sm.enqueue(e);
	}

	@SuppressWarnings("unchecked")
	protected <E extends GameEvent> E event(StateTransition<S, GameEvent> t) {
		return (E) t.event().get();
	}

	// Display

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

	public boolean isOutsideMaze() {
		Tile tile = getTile();
		return tile.row < 0 || tile.row >= maze.numRows() || tile.col < 0 || tile.col >= maze.numCols();
	}

	public void move() {
		Tile tile = getTile();
		if (isOutsideMaze()) {
			// teleport
			if (tile.col > (maze.numCols() - 1) + TELEPORT_LENGTH) {
				placeAt(0, tile.row);
			} else if (tile.col < -TELEPORT_LENGTH) {
				placeAt(maze.numCols() - 1, tile.row);
			} else {
				tf.moveTo(computePosition(dir, fnSpeed.apply(this)));
			}
			return;
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

	public boolean canMove(int direction) {
		Tile current = getTile();
		if (direction == Top4.W && current.col <= 0
				|| direction == Top4.E && current.col >= maze.numCols() - 1) {
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
			placeAt(getTile()); // TODO this is not 100% correct
			return isExactlyOverTile();
		}
		return true;
	}

	public Tile computeNextTile(Tile current, int dir) {
		Vector2f nextPosition = computePosition(dir, fnSpeed.apply(this));
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

	private Vector2f computePosition(int dir, float speed) {
		Vector2f v_dir = Vector2f.of(TOPOLOGY.dx(dir), TOPOLOGY.dy(dir));
		return sum(tf.getPosition(), smul(speed, v_dir));
	}
}