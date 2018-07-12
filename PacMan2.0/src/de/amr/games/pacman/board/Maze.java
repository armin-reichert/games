package de.amr.games.pacman.board;

import static de.amr.games.pacman.board.Tile.BONUS_APPLE;
import static de.amr.games.pacman.board.Tile.BONUS_BELL;
import static de.amr.games.pacman.board.Tile.BONUS_CHERRIES;
import static de.amr.games.pacman.board.Tile.BONUS_GALAXIAN;
import static de.amr.games.pacman.board.Tile.BONUS_GRAPES;
import static de.amr.games.pacman.board.Tile.BONUS_KEY;
import static de.amr.games.pacman.board.Tile.BONUS_PEACH;
import static de.amr.games.pacman.board.Tile.BONUS_STRAWBERRY;
import static de.amr.games.pacman.board.Tile.ENERGIZER;
import static de.amr.games.pacman.board.Tile.PELLET;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;

public class Maze extends GameEntity {

	private Board board;
	private int width;
	private int height;

	public Maze(Board board, int width, int height) {
		this.board = board;
		this.width = width;
		this.height = height;
		board.setTile(13, 17, BONUS_PEACH);
	}

	public Board getBoard() {
		return board;
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(SpriteSheet.get().getMazeImage(), 0, 0, width, height, null);
		board.getGrid().vertices().forEach(tile -> drawTile(g, board.getGrid().row(tile), board.getGrid().col(tile)));
	}

	private void drawTile(Graphics2D g, int row, int col) {
		g.translate(col * Board.TILE_SIZE, row * Board.TILE_SIZE);
		char tile = board.getGrid().get(board.getGrid().cell(col, row));
		switch (tile) {
		case PELLET:
			drawPellet(g, row, col);
			break;
		case ENERGIZER:
			drawEnergizer(g, row, col);
			break;
		case BONUS_APPLE:
		case BONUS_BELL:
		case BONUS_CHERRIES:
		case BONUS_GALAXIAN:
		case BONUS_GRAPES:
		case BONUS_KEY:
		case BONUS_PEACH:
		case BONUS_STRAWBERRY:
			drawBonus(g, row, col, tile);
		default:
			break;
		}
		g.translate(-col * Board.TILE_SIZE, -row * Board.TILE_SIZE);
	}

	private void drawBonus(Graphics2D g, int row, int col, char bonus) {
		g.translate(0, -Board.TILE_SIZE / 2);
		g.drawImage(SpriteSheet.get().getBonusImage(bonus), 0, 0, Board.TILE_SIZE*2, Board.TILE_SIZE*2, null);
		g.translate(0, Board.TILE_SIZE / 2);
	}

	private void drawPellet(Graphics2D g, int row, int col) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.YELLOW);
		drawCenteredCircle(g, row, col, Board.TILE_SIZE / 8);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void drawEnergizer(Graphics2D g, int row, int col) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.YELLOW);
		drawCenteredCircle(g, row, col, Board.TILE_SIZE / 4);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void drawCenteredCircle(Graphics2D g, int row, int col, int r) {
		g.fillOval(Board.TILE_SIZE / 2 - r, Board.TILE_SIZE / 2 - r, 2 * r, 2 * r);
	}
}