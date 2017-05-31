package de.amr.games.pacman.core.entities;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Wormhole;
import static de.amr.games.pacman.theme.PacManTheme.SPRITE_SIZE;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.lang.Math.abs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;

/**
 * Base class for entities which move on the board.
 */
public abstract class BoardMover extends GameEntity {

	protected final Board board;
	protected List<Integer> route;
	protected int moveDir;
	protected int nextMoveDir;
	protected boolean couldMove;
	public Supplier<Float> speed;
	public Function<Tile, Boolean> canEnterTile;

	public BoardMover(Board board) {
		this.board = Objects.requireNonNull(board);
		route = Collections.emptyList();
		canEnterTile = tile -> board.isTileValid(tile);
	}

	@Override
	public void init() {
		route = new ArrayList<>();
		moveDir = nextMoveDir = E;
		couldMove = false;
		speed = () -> 0f;
	}

	public Board getBoard() {
		return board;
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

	public boolean couldMove() {
		return couldMove;
	}

	@Override
	public int getWidth() {
		return TILE_SIZE;
	}

	@Override
	public int getHeight() {
		return TILE_SIZE;
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

	public void adjust() {
		placeAt(currentTile());
	}

	public boolean isExactlyOverTile(Tile tile) {
		return isExactlyOver(tile.getRow(), tile.getCol());
	}

	protected boolean isExactlyOverTile() {
		return isExactlyOver(getRow(), getCol());
	}

	protected boolean isExactlyOver(int row, int col) {
		int tolerance = 1;
		return abs(tr.getX() - col * TILE_SIZE) <= tolerance && abs(tr.getY() - row * TILE_SIZE) <= tolerance;
	}

	public boolean canEnterTileTowards(int dir) {
		return canEnterTile.apply(currentTile().neighbor(dir));
	}

	public boolean turnTo(int dir) {
		nextMoveDir = dir;
		if (!canEnterTileTowards(dir)) {
			return false;
		}
		boolean turnLeftOrRight = (dir == Top4.INSTANCE.left(moveDir) || dir == Top4.INSTANCE.right(moveDir));
		if (turnLeftOrRight && !isExactlyOverTile()) {
			return false;
		}
		moveDir = nextMoveDir;
		return true;
	}

	/**
	 * Try to move entity in current move direction. If entity can be moved, set
	 * <code>couldMove</code> to <code>true</code>.
	 */
	public void move() {

		// move pixel-wise
		Vector2 oldPosition = new Vector2(tr.getX(), tr.getY());
		Vector2 velocity = new Vector2(Top4.INSTANCE.dx(moveDir), Top4.INSTANCE.dy(moveDir)).times(speed.get());
		tr.setVel(velocity);
		tr.move();

		// check if new tile position is allowed
		Tile newTile = currentTile();
		if (!canEnterTile.apply(newTile)) {
			tr.moveTo(oldPosition); // undo move
			couldMove = false;
			return;
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
			couldMove = true;
			return;
		}

		// adjust position if entity touches disallowed neighbor tile
		Tile neighborTile = newTile.neighbor(moveDir);
		boolean forbidden = !canEnterTile.apply(neighborTile);

		if (!forbidden) {
			couldMove = true;
			return;
		}

		int row = newTile.getRow(), col = newTile.getCol();
		switch (moveDir) {
		case E:
			if (tr.getX() + TILE_SIZE >= (col + 1) * TILE_SIZE) {
				tr.setX(col * TILE_SIZE);
			}
			break;
		case W:
			if (tr.getX() < col * TILE_SIZE) {
				tr.setX(col * TILE_SIZE);
			}
			break;
		case N:
			if (tr.getY() < row * TILE_SIZE) {
				tr.setY(row * TILE_SIZE);
			}
			break;
		case S:
			if (tr.getY() + TILE_SIZE >= (row + 1) * TILE_SIZE) {
				tr.setY(row * TILE_SIZE);
			}
			break;
		}
		couldMove = false;
	}

	public void moveRandomly() {
		move();
		if (!isExactlyOverTile()) {
			return;
		}
		for (int dir : Top4.INSTANCE.dirsPermuted().toArray()) {
			Tile neighborTile = currentTile().neighbor(dir);
			if (!board.isTileValid(neighborTile)) {
				continue; // TODO
			}
			if (board.contains(neighborTile, Wormhole)) {
				moveDir = Top4.INSTANCE.inv(moveDir);
				break;
			}
			if (dir == Top4.INSTANCE.inv(moveDir)) {
				break;
			}
			if (canEnterTile.apply(neighborTile)) {
				turnTo(dir);
				break;
			}
		}
	}

	public void moveAlongRoute() {
		if (!route.isEmpty()) {
			if (turnTo(route.get(0))) {
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
		move();
		if (!couldMove) {
			turnTo(Top4.INSTANCE.inv(moveDir));
		}
	}

	// -- user interface

	@Override
	public void draw(Graphics2D g) {
		int margin = (SPRITE_SIZE - TILE_SIZE) / 2;
		g.translate(-margin, -margin);
		super.draw(g);
		g.translate(margin, margin);
	}

	public void drawRoute(Graphics2D g, Color color) {
		if (route.isEmpty()) {
			return;
		}
		g.setColor(color);
		g.setStroke(new BasicStroke(1f));
		int offset = TILE_SIZE / 2;
		Tile tile = currentTile();
		for (int dir : route) {
			Tile nextTile = tile.neighbor(dir);
			g.drawLine(tile.getCol() * TILE_SIZE + offset, tile.getRow() * TILE_SIZE + offset,
					nextTile.getCol() * TILE_SIZE + offset, nextTile.getRow() * TILE_SIZE + offset);
			tile = nextTile;
		}
		g.fillRect(tile.getCol() * TILE_SIZE + TILE_SIZE / 4, tile.getRow() * TILE_SIZE + TILE_SIZE / 4, TILE_SIZE / 2,
				TILE_SIZE / 2);
	}
}