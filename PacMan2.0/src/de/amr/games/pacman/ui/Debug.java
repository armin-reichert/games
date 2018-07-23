package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.controller.PlayScene;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class Debug {

	public static int DEBUG_LEVEL = 0;

	public static void log(Supplier<String> msg) {
		if (DEBUG_LEVEL >= 1) {
			Logger.getGlobal().info(msg.get());
		}
	}

	public static void update(PlayScene scene) {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_0)) {
			DEBUG_LEVEL = 0;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			DEBUG_LEVEL = DEBUG_LEVEL == 1 ? 0 : 1;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			DEBUG_LEVEL = DEBUG_LEVEL == 2 ? 0 : 2;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			scene.getFsm().setLogger(scene.getFsm().getLogger().isPresent() ? null : Logger.getGlobal());
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_K)) {
			scene.getGhosts().filter(ghost -> ghost.getState() == Ghost.State.ATTACKING)
					.forEach(ghost -> ghost.setState(Ghost.State.DEAD));
		}
	}

	public static void draw(Graphics2D g, PlayScene scene) {
		if (DEBUG_LEVEL == 2) {
			drawMaze(g, scene.getMazeUI());
		}
		if (DEBUG_LEVEL == 1) {
			drawGhostPaths(g, scene);
			drawPacManTilePosition(g, scene);
		}
	}

	private static void drawMaze(Graphics2D g, MazeUI mazeUI) {
		Maze maze = mazeUI.getMaze();
		g.translate(mazeUI.tf.getX(), mazeUI.tf.getY());
		g.setColor(Color.LIGHT_GRAY);
		for (int row = 0; row < maze.numRows() + 1; ++row) {
			g.drawLine(0, row * TS, mazeUI.getWidth(), row * TS);
		}
		for (int col = 1; col < maze.numCols(); ++col) {
			g.drawLine(col * TS, 0, col * TS, mazeUI.getHeight());
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
		g.translate(-mazeUI.tf.getX(), -mazeUI.tf.getY());
	}

	private static void drawPacManTilePosition(Graphics2D g, PlayScene scene) {
		PacMan pacMan = scene.getPacMan();
		if (pacMan.isExactlyOverTile()) {
			g.translate(scene.getMazeUI().tf.getX(), scene.getMazeUI().tf.getY());
			g.setColor(Color.GREEN);
			g.translate(pacMan.tf.getX(), pacMan.tf.getY());
			g.drawRect(0, 0, pacMan.getWidth(), pacMan.getHeight());
			g.translate(-pacMan.tf.getX(), -pacMan.tf.getY());
			g.translate(-scene.getMazeUI().tf.getX(), -scene.getMazeUI().tf.getY());
		}
	}

	private static void drawGhostPaths(Graphics2D g, PlayScene scene) {
		scene.getGhosts().forEach(ghost -> {
			Route route = ghost.currentMoveBehavior().apply(ghost);
			List<Tile> path = route.getPath();
			if (path.size() > 1) {
				switch (ghost.getColor()) {
				case Spritesheet.RED_GHOST:
					g.setColor(Color.RED);
					break;
				case Spritesheet.PINK_GHOST:
					g.setColor(Color.PINK);
					break;
				case Spritesheet.BLUE_GHOST:
					g.setColor(Color.BLUE);
					break;
				case Spritesheet.ORANGE_GHOST:
					g.setColor(Color.ORANGE);
					break;
				}
				g.translate(scene.getMazeUI().tf.getX(), scene.getMazeUI().tf.getY());
				for (int i = 0; i < path.size() - 1; ++i) {
					Tile u = path.get(i), v = path.get(i + 1);
					int u1 = u.col * TS + TS / 2;
					int u2 = u.row * TS + TS / 2;
					int v1 = v.col * TS + TS / 2;
					int v2 = v.row * TS + TS / 2;
					g.drawLine(u1, u2, v1, v2);
				}
				// Target tile
				Tile tile = path.get(path.size() - 1);
				g.translate(tile.col * TS, tile.row * TS);
				g.fillRect(TS / 4, TS / 4, TS / 2, TS / 2);
				g.translate(-tile.col * TS, -tile.row * TS);
				g.translate(-scene.getMazeUI().tf.getX(), -scene.getMazeUI().tf.getY());
			}
		});
	}
}