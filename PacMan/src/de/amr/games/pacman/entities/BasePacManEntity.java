package de.amr.games.pacman.entities;

import static de.amr.easy.game.Application.Settings;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.PacManGame.Data;
import static de.amr.games.pacman.data.Board.Wormhole;
import static de.amr.games.pacman.ui.PacManUI.SpriteSize;
import static de.amr.games.pacman.ui.PacManUI.TileSize;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.grid.api.Topology;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.ui.PacManUI;

/**
 * Base class for Pac-Man and ghosts.
 */
public abstract class BasePacManEntity extends GameEntity {

	protected final Topology top = new Top4();
	public final Tile home;
	public int moveDir;
	public int nextMoveDir;
	public float speed;
	public PacManUI theme;

	public BasePacManEntity(Tile home) {
		this.home = home;
		moveDir = nextMoveDir = Top4.E;
		speed = 0;
	}

	@Override
	public int getWidth() {
		return TileSize;
	}

	@Override
	public int getHeight() {
		return TileSize;
	}

	public boolean isAtHome() {
		Rectangle homeArea = new Rectangle(Math.round(home.x * TileSize), Math.round(home.y * TileSize), TileSize,
				TileSize);
		return getCollisionBox().intersects(homeArea);
	}

	public int getCol() {
		return getCenter().roundedX() / TileSize;
	}

	public int getRow() {
		return getCenter().roundedY() / TileSize;
	}

	public Tile currentTile() {
		return new Tile(getRow(), getCol());
	}

	public void placeAt(Tile tile) {
		tr.moveTo(tile.x * TileSize, tile.y * TileSize);
	}

	public void adjustOnTile() {
		placeAt(currentTile());
	}

	public boolean isOverTile(int row, int col) {
		return row == getRow() && col == getCol();
	}

	public boolean isExactlyOverTile(int row, int col) {
		int tolerance = 1;
		return Math.abs(tr.getX() - col * TileSize) <= tolerance && Math.abs(tr.getY() - row * TileSize) <= tolerance;
	}

	public boolean isExactlyOverTile() {
		return isExactlyOverTile(getRow(), getCol());
	}

	public boolean canMoveTowards(int dir) {
		return canEnter(currentTile().translate(top.dx(dir), top.dy(dir)));
	}

	public abstract boolean canEnter(Tile pos);

	/**
	 * Moves entity in current move direction.
	 * 
	 * @return <code>true</code> iff entity can move
	 */
	public boolean move() {
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
		if (Data.board.has(Wormhole, newTile)) {
			int col = newTile.getCol();
			if (col == 0 && moveDir == Top4.W) {
				// fall off left edge -> appear at right edge
				tr.setX((Board.Cols - 1) * TileSize - getWidth());
			} else if (col == Board.Cols - 1 && moveDir == Top4.E) {
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
			if (forbidden && tr.getX() + TileSize >= (col + 1) * TileSize) {
				tr.setX(col * TileSize);
				return false;
			}
			break;
		case W:
			if (forbidden && tr.getX() < col * TileSize) {
				tr.setX(col * TileSize);
				return false;
			}
			break;
		case N:
			if (forbidden && tr.getY() < row * TileSize) {
				tr.setY(row * TileSize);
				return false;
			}
			break;
		case S:
			if (forbidden && tr.getY() + TileSize >= (row + 1) * TileSize) {
				tr.setY(row * TileSize);
				return false;
			}
			break;
		}
		return true;
	}

	public void changeMoveDir(int dir) {
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
		int margin = (SpriteSize - TileSize) / 2;
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