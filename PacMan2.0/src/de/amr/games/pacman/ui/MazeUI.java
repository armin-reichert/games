package de.amr.games.pacman.ui;

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
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.GameController;

public class MazeUI extends GameEntity {

	private final GameController controller;
	private final Sprite sprite;
	private boolean debug;

	public MazeUI(GameController controller, int width, int height) {
		this.controller = controller;
		sprite = new Sprite(Spritesheet.getMaze()).scale(width, height);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_D)) {
			debug = !debug;
		}
	}

	@Override
	public Sprite currentSprite() {
		return sprite;
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		g.translate(tf.getX(), tf.getY());
		controller.getGame().maze.tiles().forEach(pos -> drawTile(g, pos.x, pos.y));
		controller.getPacMan().draw(g);
		Arrays.stream(controller.getGhosts()).forEach(ghost -> ghost.draw(g));
		if (debug) {
			drawDebugInfo(g);
		}
		g.translate(-tf.getX(), -tf.getY());
	}

	private void drawTile(Graphics2D g, int col, int row) {
		g.translate(col * PacManApp.TS, row * PacManApp.TS);
		char tile = controller.getGame().maze.getContent(col, row);
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
		g.translate(-col * PacManApp.TS, -row * PacManApp.TS);
	}

	private void drawBonus(Graphics2D g, int row, int col, char bonus) {
		g.drawImage(Spritesheet.getBonus(bonus), 0, -PacManApp.TS / 2, PacManApp.TS * 2, PacManApp.TS * 2, null);
	}

	private void drawPellet(Graphics2D g, int row, int col) {
		drawCircle(g, Color.PINK, row, col, PacManApp.TS / 8);
	}

	private void drawEnergizer(Graphics2D g, int row, int col) {
		drawCircle(g, Color.PINK, row, col, PacManApp.TS / 2);
	}

	private void drawCircle(Graphics2D g, Color color, int row, int col, int r) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.fillOval(PacManApp.TS / 2 - r, PacManApp.TS / 2 - r, 2 * r, 2 * r);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void drawDebugInfo(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		for (int row = 0; row < controller.getGame().maze.numRows() + 1; ++row) {
			g.drawLine(0, row * PacManApp.TS, getWidth(), row * PacManApp.TS);
		}
		for (int col = 1; col < controller.getGame().maze.numCols(); ++col) {
			g.drawLine(col * PacManApp.TS, 0, col * PacManApp.TS, getHeight());
		}
		g.setFont(new Font("Arial Narrow", Font.PLAIN, PacManApp.TS * 40 / 100));
		for (int row = 0; row < controller.getGame().maze.numRows(); ++row) {
			for (int col = 0; col < controller.getGame().maze.numCols(); ++col) {
				g.translate(col * PacManApp.TS, row * PacManApp.TS);
				g.drawString(String.format("%d,%d", col, row), PacManApp.TS / 8, PacManApp.TS / 2);
				g.translate(-col * PacManApp.TS, -row * PacManApp.TS);
			}
		}
	}
}