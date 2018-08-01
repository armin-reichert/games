package de.amr.games.pacman.ui;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.pacman.model.TileContent.PELLET;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Level;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.actor.Ghost;
import de.amr.games.pacman.ui.actor.PacMan;

public class GameInfo implements ViewController {

	private final Game game;
	private final MazeUI mazeUI;
	private final Maze maze;
	private boolean show_grid;
	private boolean show_ghost_route;
	private boolean show_entity_state;

	public GameInfo(Game game, MazeUI mazeUI, Maze maze) {
		this.game = game;
		this.mazeUI = mazeUI;
		this.maze = maze;
	}

	@Override
	public int getWidth() {
		return mazeUI.getWidth();
	}

	@Override
	public int getHeight() {
		return mazeUI.getWidth();
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
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
			killAllLivingGhosts();
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_E)) {
			eatAllPellets();
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_B)) {
			toggleGhost(MazeUI.GhostName.BLINKY);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_P)) {
			toggleGhost(MazeUI.GhostName.PINKY);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_I)) {
			toggleGhost(MazeUI.GhostName.INKY);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			toggleGhost(MazeUI.GhostName.CLYDE);
		}
	}

	private void killAllLivingGhosts() {
		mazeUI.getActiveGhosts().forEach(ghost -> ghost.processEvent(new GhostKilledEvent(ghost)));
	}

	private void eatAllPellets() {
		maze.tiles().filter(tile -> maze.getContent(tile) == PELLET).forEach(tile -> {
			maze.clearTile(tile);
			game.foodEaten += 1;
		});
	}

	@Override
	public void draw(Graphics2D g) {
		if (show_grid) {
			drawGrid(g);
		}
		if (show_ghost_route) {
			mazeUI.getActiveGhosts().forEach(ghost -> drawGhostPath(g, ghost));
		}
		if (show_entity_state) {
			drawPacManTilePosition(g);
			drawEntityState(g);
		}
	}

	private void drawGrid(Graphics2D g) {
		g.translate(mazeUI.tf.getX(), mazeUI.tf.getY());
		g.setColor(Color.LIGHT_GRAY);
		for (int row = 0; row < maze.numRows() + 1; ++row) {
			g.drawLine(0, row * Spritesheet.TS, mazeUI.getWidth(), row * Spritesheet.TS);
		}
		for (int col = 1; col < maze.numCols(); ++col) {
			g.drawLine(col * Spritesheet.TS, 0, col * Spritesheet.TS, mazeUI.getHeight());
		}
		int fontSize = Spritesheet.TS * 4 / 10;
		if (fontSize > 4) {
			g.setFont(new Font("Arial Narrow", Font.PLAIN, Spritesheet.TS * 40 / 100));
			for (int row = 0; row < maze.numRows(); ++row) {
				for (int col = 0; col < maze.numCols(); ++col) {
					g.translate(col * Spritesheet.TS, row * Spritesheet.TS);
					g.drawString(String.format("%d,%d", col, row), Spritesheet.TS / 8, Spritesheet.TS / 2);
					g.translate(-col * Spritesheet.TS, -row * Spritesheet.TS);
				}
			}
		}
		g.translate(-mazeUI.tf.getX(), -mazeUI.tf.getY());
	}

	private void drawEntityState(Graphics2D g) {
		PacMan pacMan = mazeUI.getPacMan();
		g.translate(mazeUI.tf.getX(), mazeUI.tf.getY());
		drawText(g, Color.YELLOW, pacMan.tf.getX(), pacMan.tf.getY(), pacMan.getState().toString());
		mazeUI.getActiveGhosts().filter(Ghost::isVisible).forEach(ghost -> {
			String txt = String.format("%s(%s)", ghost.getState(), ghost.getName());
			drawText(g, color(ghost), ghost.tf.getX() - Spritesheet.TS, ghost.tf.getY(), txt);
		});
		g.translate(-mazeUI.tf.getX(), -mazeUI.tf.getY());
	}

	private void toggleGhost(MazeUI.GhostName ghostName) {
		mazeUI.setGhostActive(ghostName, !mazeUI.isGhostActive(ghostName));
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

	private void drawText(Graphics2D g, Color color, float x, float y, String text) {
		g.translate(x, y);
		g.setColor(color);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Spritesheet.TS / 2));
		g.drawString(text, 0, -Spritesheet.TS / 2);
		g.translate(-x, -y);
	}

	private void drawPacManTilePosition(Graphics2D g) {
		PacMan pacMan = mazeUI.getPacMan();
		if (pacMan.isExactlyOverTile()) {
			g.translate(mazeUI.tf.getX(), mazeUI.tf.getY());
			g.translate(pacMan.tf.getX(), pacMan.tf.getY());
			g.setColor(Color.GREEN);
			g.drawRect(0, 0, pacMan.getWidth(), pacMan.getHeight());
			g.translate(-pacMan.tf.getX(), -pacMan.tf.getY());
			g.translate(-mazeUI.tf.getX(), -mazeUI.tf.getY());
		}
	}

	private void drawGhostPath(Graphics2D g, Ghost ghost) {
		List<Tile> path = ghost.getNavigation().computeRoute(ghost).getPath();
		if (path.size() > 1) {
			g.setColor(color(ghost));
			g.translate(mazeUI.tf.getX(), mazeUI.tf.getY());
			for (int i = 0; i < path.size() - 1; ++i) {
				Tile u = path.get(i), v = path.get(i + 1);
				int u1 = u.col * Spritesheet.TS + Spritesheet.TS / 2;
				int u2 = u.row * Spritesheet.TS + Spritesheet.TS / 2;
				int v1 = v.col * Spritesheet.TS + Spritesheet.TS / 2;
				int v2 = v.row * Spritesheet.TS + Spritesheet.TS / 2;
				g.drawLine(u1, u2, v1, v2);
			}
			// Target tile
			Tile tile = path.get(path.size() - 1);
			g.translate(tile.col * Spritesheet.TS, tile.row * Spritesheet.TS);
			g.fillRect(Spritesheet.TS / 4, Spritesheet.TS / 4, Spritesheet.TS / 2, Spritesheet.TS / 2);
			g.translate(-tile.col * Spritesheet.TS, -tile.row * Spritesheet.TS);
			g.translate(-mazeUI.tf.getX(), -mazeUI.tf.getY());
		}
	}
}