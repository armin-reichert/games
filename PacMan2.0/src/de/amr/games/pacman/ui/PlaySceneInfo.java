package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.PlayScene;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class PlaySceneInfo {

	public static final Logger LOG = Logger.getLogger(PlaySceneInfo.class.getSimpleName());

	static {
		LOG.setLevel(Level.OFF);
	}

	private static boolean show_grid;
	private static boolean show_ghost_route;
	private static boolean show_entity_state;

	public static void update(PlayScene scene) {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_L)) {
			LOG.setLevel(LOG.getLevel() == Level.OFF ? Level.INFO : Level.OFF);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_G)) {
			show_grid = !show_grid;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_S)) {
			show_entity_state = !show_entity_state;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_R)) {
			show_ghost_route = !show_ghost_route;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_K)) {
			killAllLivingGhosts(scene);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_E)) {
			eatAllPellets(scene);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_B)) {
			toggleGhost(scene, scene.blinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_P)) {
			toggleGhost(scene, scene.pinky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_I)) {
			toggleGhost(scene, scene.inky);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			toggleGhost(scene, scene.clyde);
		}
	}

	private static void killAllLivingGhosts(PlayScene scene) {
		scene.mazeUI.getGhosts().filter(ghost -> ghost.getState() == Ghost.State.AGGRESSIVE)
				.forEach(ghost -> ghost.setState(Ghost.State.DEAD));
	}

	private static void eatAllPellets(PlayScene scene) {
		Maze maze = scene.mazeUI.getMaze();
		maze.tiles().filter(tile -> maze.getContent(tile) == Tile.PELLET).forEach(tile -> {
			maze.clearTile(tile);
			scene.game.foodEaten += 1;
		});
	}

	public static void draw(Graphics2D g, PlayScene scene) {
		if (show_grid) {
			drawGrid(g, scene.mazeUI);
		}
		if (show_ghost_route) {
			scene.mazeUI.getGhosts().forEach(ghost -> drawGhostPath(g, ghost, scene.mazeUI));
		}
		if (show_entity_state) {
			drawPacManTilePosition(g, scene);
			drawEntityState(g, scene);
		}
	}

	private static void drawGrid(Graphics2D g, MazeUI mazeUI) {
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

	private static void drawEntityState(Graphics2D g, PlayScene scene) {
		MazeUI mazeUI = scene.mazeUI;
		g.translate(mazeUI.tf.getX(), mazeUI.tf.getY());
		PacMan pacMan = scene.pacMan;
		drawText(g, Color.YELLOW, pacMan.tf.getX(), pacMan.tf.getY(), pacMan.getState().toString());
		mazeUI.getGhosts().forEach(ghost -> drawText(g, color(ghost), ghost.tf.getX(), ghost.tf.getY(),
				ghost.getState().toString()));
		g.translate(-mazeUI.tf.getX(), -mazeUI.tf.getY());
	}

	private static void toggleGhost(PlayScene scene, Ghost ghost) {
		if (scene.mazeUI.containsGhost(ghost)) {
			scene.mazeUI.removeGhost(ghost);
		} else {
			scene.mazeUI.addGhost(ghost);
		}
	}

	private static Color color(Ghost ghost) {
		switch (ghost.getColor()) {
		case Spritesheet.TURQUOISE_GHOST:
			return new Color(64, 224, 208);
		case Spritesheet.ORANGE_GHOST:
			return Color.ORANGE;
		case Spritesheet.PINK_GHOST:
			return Color.PINK;
		case Spritesheet.RED_GHOST:
			return Color.RED;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static void drawText(Graphics2D g, Color color, float x, float y, String text) {
		g.translate(x, y);
		g.setColor(color);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TS / 2));
		g.drawString(text, 0, -TS / 2);
		g.translate(-x, -y);
	}

	private static void drawPacManTilePosition(Graphics2D g, PlayScene scene) {
		PacMan pacMan = scene.pacMan;
		if (pacMan.isExactlyOverTile()) {
			g.translate(scene.mazeUI.tf.getX(), scene.mazeUI.tf.getY());
			g.translate(pacMan.tf.getX(), pacMan.tf.getY());
			g.setColor(Color.GREEN);
			g.drawRect(0, 0, pacMan.getWidth(), pacMan.getHeight());
			g.translate(-pacMan.tf.getX(), -pacMan.tf.getY());
			g.translate(-scene.mazeUI.tf.getX(), -scene.mazeUI.tf.getY());
		}
	}

	private static void drawGhostPath(Graphics2D g, Ghost ghost, MazeUI mazeUI) {
		List<Tile> path = ghost.getNavigation().computeRoute(ghost).getPath();
		if (path.size() > 1) {
			g.setColor(color(ghost));
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
			g.fillRect(TS / 4, TS / 4, TS / 2, TS / 2);
			g.translate(-tile.col * TS, -tile.row * TS);
			g.translate(-mazeUI.tf.getX(), -mazeUI.tf.getY());
		}
	}
}