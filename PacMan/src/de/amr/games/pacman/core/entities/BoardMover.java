package de.amr.games.pacman.core.entities;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.Top4;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Wormhole;
import static de.amr.games.pacman.theme.PacManTheme.SPRITE_SIZE;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.lang.Math.abs;
import static java.lang.String.format;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;

/**
 * Base class for entities which can move on the tiled board.
 * 
 * @author Armin Reichert
 */
public abstract class BoardMover extends GameEntity {

	protected final Board board;

	public Supplier<Float> speed;
	public Function<Tile, Boolean> canEnterTile;
	public IntSupplier xOffset;

	protected List<Integer> route;
	protected int moveDir;
	protected int nextMoveDir;
	protected boolean stuck;

	public BoardMover(Board board) {
		this.board = Objects.requireNonNull(board);
		canEnterTile = tile -> board.isBoardTile(tile);
		xOffset = () -> 0;
		route = new ArrayList<>();
		moveDir = nextMoveDir = E;
		stuck = true;
		speed = () -> 0f;
	}

	@Override
	public String toString() {
		return format("%s[name=%s,row=%d, col=%d]", getClass().getSimpleName(), getName(), getRow(), getCol());
	}

	@Override
	public void init() {
		route = new ArrayList<>();
		moveDir = nextMoveDir = E;
		stuck = true;
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

	public boolean isStuck() {
		return stuck;
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
		tf.moveTo(tile.col * TILE_SIZE, tile.row * TILE_SIZE);
	}

	public void placeAt(int row, int col) {
		tf.moveTo(col * TILE_SIZE, row * TILE_SIZE);
	}

	public void adjust() {
		placeAt(currentTile());
	}

	public boolean isExactlyOver(Tile tile) {
		return isExactlyOver(tile.row, tile.col);
	}

	protected boolean isAdjusted() {
		return isExactlyOver(getRow(), getCol());
	}

	protected boolean isExactlyOver(int row, int col) {
		int tolerance = 1;
		return abs(tf.getX() - col * TILE_SIZE) <= tolerance && abs(tf.getY() - row * TILE_SIZE) <= tolerance;
	}

	public boolean canEnterTileTowards(int dir) {
		return canEnterTile.apply(currentTile().neighbor(dir));
	}

	public boolean turnTo(int dir) {
		nextMoveDir = dir;
		if (!canEnterTileTowards(dir)) {
			return false;
		}
		boolean turnLeftOrRight = (dir == Top4.left(moveDir) || dir == Top4.right(moveDir));
		if (turnLeftOrRight && !isAdjusted()) {
			return false;
		}
		moveDir = nextMoveDir;
		return true;
	}

	/**
	 * Moves entity in current move direction. Store if entity could be moved.
	 */
	public void move() {
		Vector2 velocity = new Vector2(Top4.dx(moveDir), Top4.dy(moveDir)).times(speed.get());
		tf.setVelocity(velocity);
		tf.move();

		Tile tile = currentTile();
		int row = tile.row, col = tile.col;

		// handle "worm hole"
		if (board.contains(tile, Wormhole)) {
			if (col == 0 && moveDir == W) {
				placeAt(row, board.numCols - 1);
			} else if (col == board.numCols - 1 && moveDir == E) {
				placeAt(row, 0);
			}
			stuck = false;
			return;
		}

		stuck = !canEnterTile.apply(tile.neighbor(moveDir));
		// adjust position if stuck and reaching into inaccessible neighbor tile
		if (stuck) {
			/*@formatter:off*/
			if (moveDir == E && tf.getX() >= col * TILE_SIZE
			 || moveDir == W && tf.getX() <  col * TILE_SIZE
			 || moveDir == N && tf.getY() <  row * TILE_SIZE
			 || moveDir == S && tf.getY() >= row * TILE_SIZE) {
				placeAt(row, col);
			}
			/*@formatter:on*/
		}
	}

	public void moveRandomly() {
		move();
		if (isAdjusted()) {
			for (int dir : Top4.dirsPermuted().toArray()) {
				if (dir != Top4.inv(moveDir) && canEnterTileTowards(dir)) {
					turnTo(dir);
					break;
				}
			}
		}
	}

	public void moveAlongRoute() {
		move();
		if (!route.isEmpty()) {
			if (turnTo(route.get(0))) {
				route.remove(0);
			}
		}
	}

	public void follow(Tile target) {
		moveAlongRoute();
		route = board.shortestRoute(currentTile(), target);
	}

	public void bounce() {
		move();
		if (stuck) {
			turnTo(Top4.inv(moveDir));
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
		Tile current = currentTile();
		for (int dir : route) {
			Tile next = current.neighbor(dir);
			g.drawLine(current.col * TILE_SIZE + offset, current.row * TILE_SIZE + offset, next.col * TILE_SIZE + offset,
					next.row * TILE_SIZE + offset);
			current = next;
		}
		int size = TILE_SIZE / 2;
		g.fillRect(current.col * TILE_SIZE + size / 2, current.row * TILE_SIZE + size / 2, size, size);
	}
}