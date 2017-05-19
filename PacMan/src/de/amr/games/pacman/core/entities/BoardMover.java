package de.amr.games.pacman.core.entities;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Wormhole;
import static de.amr.games.pacman.theme.PacManTheme.SPRITE_SIZE;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.lang.Math.abs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.core.app.AbstractPacManApp;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;

/**
 * Base class for entities which move on the board..
 */
public abstract class BoardMover extends GameEntity {

	protected final AbstractPacManApp app;
	protected final Board board;
	protected final Tile home;
	protected List<Integer> route;
	protected int moveDir;
	protected int nextMoveDir;
	public Supplier<Float> speed;

	public BoardMover(AbstractPacManApp app, Board board, Tile home) {
		this.app = Objects.requireNonNull(app);
		this.board = Objects.requireNonNull(board);
		this.home = Objects.requireNonNull(home);
		route = new ArrayList<>();
		moveDir = nextMoveDir = E;
		speed = () -> 0f;
		placeAt(home);
	}

	public Board getBoard() {
		return board;
	}

	public Tile getHome() {
		return home;
	}

	public List<Integer> getRoute() {
		return route;
	}

	public void setRoute(List<Integer> route) {
		this.route = route;
	}

	public int getMoveDir() {
		return moveDir;
	}

	public void setMoveDir(int moveDir) {
		this.moveDir = moveDir;
	}

	public int getNextMoveDir() {
		return nextMoveDir;
	}

	public void setNextMoveDir(int nextMoveDir) {
		this.nextMoveDir = nextMoveDir;
	}

	@Override
	public int getWidth() {
		return TILE_SIZE;
	}

	@Override
	public int getHeight() {
		return TILE_SIZE;
	}

	public boolean isAtHome() {
		Rectangle2D homeArea = new Rectangle2D.Float(home.x * TILE_SIZE, home.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		return getCollisionBox().intersects(homeArea);
	}

	public int getCol() {
		return getCenter().roundedX() / TILE_SIZE;
	}

	public int getRow() {
		return getCenter().roundedY() / TILE_SIZE;
	}

	public Tile currentTile() {
		return new Tile(getRow(), getCol());
	}

	public void placeAt(Tile tile) {
		tr.moveTo(tile.x * TILE_SIZE, tile.y * TILE_SIZE);
	}

	public void adjustOnTile() {
		placeAt(currentTile());
	}

	public boolean isExactlyOverTile(Tile tile) {
		return isExactlyOver(tile.getRow(), tile.getCol());
	}

	private boolean isExactlyOverTile() {
		return isExactlyOver(getRow(), getCol());
	}

	private boolean isExactlyOver(int row, int col) {
		int tolerance = 1;
		return abs(tr.getX() - col * TILE_SIZE) <= tolerance && abs(tr.getY() - row * TILE_SIZE) <= tolerance;
	}

	public boolean canMoveTowards(int dir) {
		return canEnter(currentTile().neighbor(dir));
	}

	public boolean changeMoveDir(int dir) {
		nextMoveDir = dir;
		boolean turn90 = (dir == board.topology.left(moveDir) || dir == board.topology.right(moveDir));
		if (!canMoveTowards(dir) || turn90 && !isExactlyOverTile()) {
			return false;
		}
		moveDir = nextMoveDir;
		return true;
	}

	public abstract boolean canEnter(Tile tile);

	/**
	 * Moves entity in current move direction.
	 * 
	 * @return <code>true</code> iff entity can move
	 */
	public boolean move() {
		// simulate move
		Vector2 oldPosition = new Vector2(tr.getX(), tr.getY());
		tr.setVel(new Vector2(board.topology.dx(moveDir), board.topology.dy(moveDir)).times(speed.get()));
		tr.move();
		// check if move would touch disallowed tile
		Tile newTile = currentTile();
		if (!canEnter(newTile)) {
			tr.moveTo(oldPosition); // undo move
			return false;
		}
		// check if "worm hole"-tile has been entered
		if (board.contains(newTile, Wormhole)) {
			int col = newTile.getCol();
			if (col == 0 && moveDir == W) {
				// fall off left edge -> appear at right edge
				tr.setX((board.numCols - 1) * TILE_SIZE - getWidth());
			} else if (col == board.numCols - 1 && moveDir == Top4.E) {
				// fall off right edge -> appear at left edge
				tr.setX(0);
			}
			return true;
		}
		// adjust position if entity touches disallowed neighbor tile
		int row = newTile.getRow(), col = newTile.getCol();
		Tile neighborTile = newTile.neighbor(moveDir);
		boolean forbidden = !canEnter(neighborTile);
		switch (moveDir) {
		case E:
			if (forbidden && tr.getX() + TILE_SIZE >= (col + 1) * TILE_SIZE) {
				tr.setX(col * TILE_SIZE);
				return false;
			}
			break;
		case W:
			if (forbidden && tr.getX() < col * TILE_SIZE) {
				tr.setX(col * TILE_SIZE);
				return false;
			}
			break;
		case N:
			if (forbidden && tr.getY() < row * TILE_SIZE) {
				tr.setY(row * TILE_SIZE);
				return false;
			}
			break;
		case S:
			if (forbidden && tr.getY() + TILE_SIZE >= (row + 1) * TILE_SIZE) {
				tr.setY(row * TILE_SIZE);
				return false;
			}
			break;
		}
		return true;
	}

	public void moveRandomly() {
		move();
		if (!isExactlyOverTile()) {
			return;
		}
		List<Integer> dirsPermuted = board.topology.dirsPermuted().boxed().collect(Collectors.toList());
		for (int dir : dirsPermuted) {
			Tile targetTile = currentTile().neighbor(dir);
			if (!board.isTileValid(targetTile)) {
				continue; // TODO
			}
			if (board.contains(targetTile, Wormhole)) {
				moveDir = board.topology.inv(moveDir);
				break;
			}
			if (dir == board.topology.inv(moveDir)) {
				break;
			}
			if (canEnter(targetTile)) {
				changeMoveDir(dir);
				break;
			}
		}
	}

	public void moveAlongRoute() {
		if (!route.isEmpty()) {
			if (changeMoveDir(route.get(0))) {
				route.remove(0);
			}
		}
		move();
	}

	public void follow(Tile target) {
		moveAlongRoute();
		route = board.shortestRoute(currentTile(), target);
	}

	public void bounce() {
		if (!move()) {
			changeMoveDir(board.topology.inv(moveDir));
		}
	}

	// -- user interface

	@Override
	public void draw(Graphics2D g) {
		int margin = (SPRITE_SIZE - TILE_SIZE) / 2;
		g.translate(-margin, -margin);
		super.draw(g);
		g.translate(margin, margin);
		if (app.settings.getBool("drawGrid")) {
			drawCollisionBox(g, isExactlyOverTile() ? Color.GREEN : Color.LIGHT_GRAY);
		}
	}
}