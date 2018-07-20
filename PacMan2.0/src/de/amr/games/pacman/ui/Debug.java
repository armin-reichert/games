package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.function.Supplier;

import de.amr.games.pacman.model.Maze;

public class Debug {

	public static boolean DEBUG = false;

	public static void run(Runnable code) {
		if (!DEBUG) {
			return;
		}
		code.run();
	}

	public static void log(Supplier<String> msg) {
		if (!DEBUG) {
			return;
		}
		System.out.println(msg.get());
	}

	public static void drawMazeDebugInfo(Graphics2D g, MazeUI mazeUI) {
		if (!DEBUG) {
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
	
	public static void drawGhostPath(Graphics2D g, Ghost ghost) {
		if (!DEBUG) {
			return;
		}
		
	}

}
