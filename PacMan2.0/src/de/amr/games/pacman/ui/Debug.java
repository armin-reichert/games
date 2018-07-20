package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Supplier;

import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class Debug {

	public static int DEBUG_LEVEL = 0;

	public static void readDebugLevel() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_0)) {
			DEBUG_LEVEL = 0;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_1)) {
			DEBUG_LEVEL = 1;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_2)) {
			DEBUG_LEVEL = 2;
		}
	}

	public static void run(Runnable code) {
		if (DEBUG_LEVEL == 0) {
			return;
		}
		code.run();
	}

	public static void log(Supplier<String> msg) {
		if (DEBUG_LEVEL < 1) {
			return;
		}
		System.out.println(msg.get());
	}

	public static void drawMazeDebugInfo(Graphics2D g, MazeUI mazeUI) {
		if (DEBUG_LEVEL < 2) {
			return;
		}
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

	public static void drawGhostPath(Graphics2D g, MazeUI mazeUI, Ghost ghost) {
		if (DEBUG_LEVEL < 1) {
			return;
		}
		List<Tile> path = ghost.currentMoveBehavior().getTargetPath();
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
			g.translate(mazeUI.tf.getX(), mazeUI.tf.getY());
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
			g.fillRect(0, 0, TS, TS);
			g.translate(-tile.col * TS, -tile.row * TS);
			g.translate(-mazeUI.tf.getX(), -mazeUI.tf.getY());
		}
	}
}