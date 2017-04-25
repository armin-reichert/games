package de.amr.games.pacman.entities;

import static de.amr.easy.game.Application.Settings;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.PacManGame.Data;
import static de.amr.games.pacman.data.Board.NUM_COLS;
import static de.amr.games.pacman.ui.PacManUI.SPRITE_SIZE;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.data.TileContent;
import de.amr.games.pacman.ui.PacManUI;

/**
 * Base class for Pac-Man and ghosts.
 */
public abstract class PacManGameEntity extends GameEntity {

	public final Tile home;
	public int moveDir;
	public int nextMoveDir;
	public float speed;
	public PacManUI theme;

	public PacManGameEntity(Tile home) {
		this.home = home;
		moveDir = nextMoveDir = Top4.E;
		speed = 0;
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
		Rectangle homeArea = new Rectangle(Math.round(home.x * TILE_SIZE), Math.round(home.y * TILE_SIZE), TILE_SIZE,
				TILE_SIZE);
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

	public boolean isOverTile(int row, int col) {
		return row == getRow() && col == getCol();
	}

	public boolean isExactlyOverTile(int row, int col) {
		int tolerance = 1;
		return Math.abs(tr.getX() - col * TILE_SIZE) <= tolerance && Math.abs(tr.getY() - row * TILE_SIZE) <= tolerance;
	}

	public boolean isExactlyOverTile() {
		return isExactlyOverTile(getRow(), getCol());
	}

	public boolean canMoveTowards(int dir) {
		final Topology top = Data.board.topology;
		return canEnter(currentTile().translate(top.dx(dir), top.dy(dir)));
	}

	public abstract boolean canEnter(Tile pos);

	/**
	 * Moves entity in current move direction.
	 * 
	 * @return <code>true</code> iff entity can move
	 */
	public boolean move() {
		final Topology top = Data.board.topology;
		// simulate move
		Vector2 oldPosition = new Vector2(tr.getX(), tr.getY());
		tr.setVel(new Vector2(top.dx(moveDir), top.dy(moveDir)).times(speed));
		tr.move();
		// check if move would touch disallowed tile
		Tile newTile = currentTile();
		if (!canEnter(newTile)) {
			tr.moveTo(oldPosition); // undo move
			return false;
		}
		// check if "worm hole"-tile has been entered
		if (Data.board.has(TileContent.Wormhole, newTile)) {
			int col = newTile.getCol();
			if (col == 0 && moveDir == Top4.W) {
				// fall off left edge -> appear at right edge
				tr.setX((NUM_COLS - 1) * TILE_SIZE - getWidth());
			} else if (col == NUM_COLS - 1 && moveDir == Top4.E) {
				// fall off right edge -> appear at left edge
				tr.setX(0);
			}
			return true;
		}
		// adjust position if entity touches disallowed neighbor tile
		int row = newTile.getRow(), col = newTile.getCol();
		Tile neighborTile = newTile.translate(top.dx(moveDir), top.dy(moveDir));
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

	public void changeMoveDir(int dir) {
		final Topology top = Data.board.topology;
		nextMoveDir = dir;
		boolean turn90 = (dir == top.left(moveDir) || dir == top.right(moveDir));
		if (!canMoveTowards(dir) || turn90 && !isExactlyOverTile()) {
			return;
		}
		moveDir = nextMoveDir;
	}

	// -- user interface

	public PacManUI getTheme() {
		return theme;
	}

	public void setTheme(PacManUI theme) {
		this.theme = theme;
	}

	@Override
	public void draw(Graphics2D g) {
		int margin = (SPRITE_SIZE - TILE_SIZE) / 2;
		g.translate(-margin, -margin);
		super.draw(g);
		g.translate(margin, margin);

		if (Settings.getBool("drawGrid")) {
			if (isExactlyOverTile(getRow(), getCol())) {
				drawCollisionBox(g, Color.GREEN);
			} else {
				drawCollisionBox(g, Color.RED);
			}
		}
	}
}