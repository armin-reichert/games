package de.amr.games.pacman.board;

import static de.amr.games.pacman.board.Tile.ENERGIZER;
import static de.amr.games.pacman.board.Tile.PELLET;
import static de.amr.games.pacman.board.Tile.WALL;
import static de.amr.games.pacman.board.Tile.WORMHOLE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;

public class Maze extends GameEntity {

	private Board board;
	private Image mazeImage;

	public Maze(Board board, int width, int height) {
		this.board = board;
		BufferedImage sprites = Assets.readImage("sprites.png");
		mazeImage = sprites.getSubimage(228, 0, 224, 248).getScaledInstance(width, height, BufferedImage.SCALE_DEFAULT);
	}

	public Board getBoard() {
		return board;
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(mazeImage, 0, 0, null);
		for (int row = 0; row < board.getGrid().numRows(); ++row) {
			for (int col = 0; col < board.getGrid().numCols(); ++col) {
				drawTile(g, row, col);
			}
		}
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
		case WALL:
			// drawWall(g, row, col);
			break;
		case WORMHOLE:
			drawWormhole(g, row, col);
			break;
		default:
			break;
		}
		g.translate(-col * Board.TILE_SIZE, -row * Board.TILE_SIZE);
	}

	private void drawWormhole(Graphics2D g, int row, int col) {
		g.setColor(Color.ORANGE);
		g.fillRect(0, 0, Board.TILE_SIZE, Board.TILE_SIZE);
	}

	private void drawWall(Graphics2D g, int row, int col) {
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, Board.TILE_SIZE, Board.TILE_SIZE);
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