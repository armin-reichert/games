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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;

/**
 * Base class for entities which move on the board.
 */
public abstract class BoardMover extends GameEntity {

	public Supplier<Float> speed;

	public Function<Tile, Boolean> canEnterTile;

	protected final Board board;
	protected List<Integer> route;
	protected int moveDir;
	protected int nextMoveDir;
	protected boolean stuck;

	public BoardMover(Board board) {
		this.board = Objects.requireNonNull(board);
		canEnterTile = tile -> board.isTileValid(tile);
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
		tr.moveTo(tile.getCol() * TILE_SIZE, tile.getRow() * TILE_SIZE);
	}

	public void placeAt(int row, int col) {
		tr.moveTo(col * TILE_SIZE, row * TILE_SIZE);
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
		boolean turnLeftOrRight = (dir == Top4.left(moveDir) || dir == Top4.right(moveDir));
		if (turnLeftOrRight && !isExactlyOverTile()) {
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
		tr.setVel(velocity);
		tr.move();

		Tile tile = currentTile();
		int row = tile.getRow(), col = tile.getCol();

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
			if (moveDir == E && tr.getX() >= col * TILE_SIZE
			||  moveDir == W && tr.getX() <  col * TILE_SIZE
			||  moveDir == N && tr.getY() <  row * TILE_SIZE
			||  moveDir == S && tr.getY() >= row * TILE_SIZE)
			{
				placeAt(row, col);
			}
			/*@formatter:on*/
		}
	}

	public void moveRandomly() {
		move();
		if (isExactlyOverTile()) {
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