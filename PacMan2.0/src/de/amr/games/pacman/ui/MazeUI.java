package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Tile.BONUS_APPLE;
import static de.amr.games.pacman.model.Tile.BONUS_BELL;
import static de.amr.games.pacman.model.Tile.BONUS_CHERRIES;
import static de.amr.games.pacman.model.Tile.BONUS_GALAXIAN;
import static de.amr.games.pacman.model.Tile.BONUS_GRAPES;
import static de.amr.games.pacman.model.Tile.BONUS_KEY;
import static de.amr.games.pacman.model.Tile.BONUS_PEACH;
import static de.amr.games.pacman.model.Tile.BONUS_STRAWBERRY;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Arrays;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class MazeUI extends GameEntity {

	private final Maze maze;
	private final PacMan pacMan;
	private final Ghost[] ghosts;
	private final Sprite spriteMaze;
	private final Sprite spriteEnergizer;

	public MazeUI(int width, int height, Maze maze, PacMan pacMan, Ghost[] ghosts) {
		this.maze = maze;
		this.pacMan = pacMan;
		this.ghosts = ghosts;
		spriteMaze = new Sprite(Spritesheet.getMaze()).scale(width, height);
		spriteEnergizer = new Sprite(Spritesheet.getEnergizerImages()).scale(TS, TS);
		spriteEnergizer.makeAnimated(AnimationMode.BACK_AND_FORTH, 500);
	}

	@Override
	public Sprite currentSprite() {
		return spriteMaze;
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		g.translate(tf.getX(), tf.getY());
		maze.tiles().forEach(tile -> drawTile(g, tile));
		Arrays.stream(ghosts).forEach(ghost -> ghost.draw(g));
		pacMan.draw(g);
		PacManApp.debug(() -> drawDebugInfo(g));
		g.translate(-tf.getX(), -tf.getY());
	}

	private void drawTile(Graphics2D g, Tile tile) {
		g.translate(tile.col * TS, tile.row * TS);
		char content = maze.getContent(tile);
		switch (content) {
		case PELLET:
			drawPellet(g);
			break;
		case ENERGIZER:
			spriteEnergizer.draw(g);
			break;
		case BONUS_APPLE:
		case BONUS_BELL:
		case BONUS_CHERRIES:
		case BONUS_GALAXIAN:
		case BONUS_GRAPES:
		case BONUS_KEY:
		case BONUS_PEACH:
		case BONUS_STRAWBERRY:
			drawBonus(g, content);
		default:
			break;
		}
		g.translate(-tile.col * TS, -tile.row * TS);
	}

	private void drawBonus(Graphics2D g, char bonus) {
		g.drawImage(Spritesheet.getBonus(bonus), 0, -TS / 2, TS * 2, TS * 2, null);
	}

	private void drawPellet(Graphics2D g) {
		int size = TS / 4;
		g.setColor(Color.PINK);
		g.fillRect((TS - size) / 2, (TS - size) / 2, size, size);
	}

	private void drawDebugInfo(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		for (int row = 0; row < maze.numRows() + 1; ++row) {
			g.drawLine(0, row * TS, getWidth(), row * TS);
		}
		for (int col = 1; col < maze.numCols(); ++col) {
			g.drawLine(col * TS, 0, col * TS, getHeight());
		}
		int fontSize = TS * 4 / 10;
		if (fontSize > 4) {
			g.setFont(new Font("Arial Narrow", Font.PLAIN, TS * 40 / 100));
			for (int row = 0; row < maze.numRows(); ++row) {
				for (int col = 0; col < maze.numCols(); ++col) {
					g.translate(col * TS, row * TS);
					g.drawString(String.format("%d,%d", col, row), TS / 8, TS / 2);
					g.translate(-col * TS, -row * TS);
				}
			}
		}
	}
}