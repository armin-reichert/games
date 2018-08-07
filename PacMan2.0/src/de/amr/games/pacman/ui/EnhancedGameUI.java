package de.amr.games.pacman.ui;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.pacman.model.Content.PELLET;
import static de.amr.games.pacman.ui.Spritesheet.TS;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Level;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.GhostName;
import de.amr.games.pacman.actor.PacMan;
import de.amr.games.pacman.controller.event.game.GhostKilledEvent;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.StateObject;

/**
 * Decorates game UI by showing states, routes etc.
 * 
 * @author Armin Reichert
 */
public class EnhancedGameUI extends GameUI {

	private final GameUI gameUI;
	private boolean show_grid;
	private boolean show_ghost_route;
	private boolean show_entity_state;

	private Image gridImage;

	public EnhancedGameUI(GameUI gameUI) {
		super(gameUI.width, gameUI.height, gameUI.game, gameUI.actors);
		this.gameUI = gameUI;
		gridImage = createGridImage(game.maze.numRows(), game.maze.numCols());
	}

	@Override
	public View currentView() {
		return this;
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void update() {
		super.update();
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
			toggleGhost(GhostName.BLINKY);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_P)) {
			toggleGhost(GhostName.PINKY);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_I)) {
			toggleGhost(GhostName.INKY);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			toggleGhost(GhostName.CLYDE);
		}
	}

	private void killAllLivingGhosts() {
		actors.getActiveGhosts().forEach(ghost -> ghost.processEvent(new GhostKilledEvent(ghost)));
	}

	private void eatAllPellets() {
		game.maze.tiles().filter(tile -> game.maze.getContent(tile) == PELLET).forEach(tile -> {
			game.maze.clearTile(tile);
			game.foodEaten += 1;
		});
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		if (show_grid) {
			drawGrid(g);
			drawPacManTilePosition(g);
		}
		if (show_ghost_route) {
			actors.getActiveGhosts().forEach(ghost -> drawGhostPath(g, ghost));
		}
		if (show_entity_state) {
			drawEntityState(g);
		}
	}

	private static Image createGridImage(int numRows, int numCols) {
		GraphicsConfiguration conf = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();
		Image image = conf.createCompatibleImage(numCols * TS, numRows * TS + 1, Transparency.TRANSLUCENT);
		Graphics g = image.getGraphics();
		g.setColor(Color.LIGHT_GRAY);
		for (int row = 0; row <= numRows; ++row) {
			g.drawLine(0, row * TS, numCols * TS, row * TS);
		}
		for (int col = 1; col < numCols; ++col) {
			g.drawLine(col * TS, 0, col * TS, numRows * TS);
		}
		return image;
	}

	private void drawGrid(Graphics2D g) {
		g.translate(gameUI.mazeUI.tf.getX(), gameUI.mazeUI.tf.getY());
		g.drawImage(gridImage, 0, 0, null);
		g.translate(-gameUI.mazeUI.tf.getX(), -gameUI.mazeUI.tf.getY());
	}

	private void drawEntityState(Graphics2D g) {
		PacMan pacMan = actors.getPacMan();
		g.translate(gameUI.mazeUI.tf.getX(), gameUI.mazeUI.tf.getY());
		drawText(g, Color.YELLOW, pacMan.tf.getX(), pacMan.tf.getY(), pacManStateText(pacMan));
		actors.getActiveGhosts().filter(Ghost::isVisible).forEach(ghost -> {
			drawText(g, color(ghost), ghost.tf.getX() - TS, ghost.tf.getY(), ghostStateText(ghost));
		});
		g.translate(-gameUI.mazeUI.tf.getX(), -gameUI.mazeUI.tf.getY());
	}

	private String pacManStateText(PacMan pacMan) {
		StateObject<PacMan.State, ?> state = pacMan.getStateMachine().state(pacMan.getStateMachine().currentState());
		return state.getDuration() != StateObject.ENDLESS
				? String.format("%s(%d|%d)", state.id(), state.getRemaining(), state.getDuration())
				: String.format("(%s)", state.id());
	}

	private String ghostStateText(Ghost ghost) {
		StateObject<Ghost.State, ?> state = ghost.getStateMachine().state(ghost.getStateMachine().currentState());
		return state.getDuration() != StateObject.ENDLESS
				? String.format("%s(%s,%d|%d)", ghost.getName(), state.id(), state.getRemaining(), state.getDuration())
				: String.format("%s(%s)", ghost.getName(), state.id());
	}

	private void toggleGhost(GhostName ghostName) {
		actors.setGhostActive(ghostName, !actors.isGhostActive(ghostName));
	}

	private static Color color(Ghost ghost) {
		switch (ghost.getName()) {
		case INKY:
			return new Color(64, 224, 208);
		case CLYDE:
			return Color.ORANGE;
		case PINKY:
			return Color.PINK;
		case BLINKY:
			return Color.RED;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void drawText(Graphics2D g, Color color, float x, float y, String text) {
		g.translate(x, y);
		g.setColor(color);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TS / 2));
		g.drawString(text, 0, -TS / 2);
		g.translate(-x, -y);
	}

	private void drawPacManTilePosition(Graphics2D g) {
		PacMan pacMan = actors.getPacMan();
		if (pacMan.isExactlyOverTile()) {
			g.translate(gameUI.mazeUI.tf.getX(), gameUI.mazeUI.tf.getY());
			g.translate(pacMan.tf.getX(), pacMan.tf.getY());
			g.setColor(Color.GREEN);
			g.drawRect(0, 0, pacMan.getWidth(), pacMan.getHeight());
			g.translate(-pacMan.tf.getX(), -pacMan.tf.getY());
			g.translate(-gameUI.mazeUI.tf.getX(), -gameUI.mazeUI.tf.getY());
		}
	}

	private void drawGhostPath(Graphics2D g, Ghost ghost) {
		List<Tile> path = ghost.getNavigation().computeRoute(ghost).getPath();
		if (path.size() > 1) {
			g.setColor(color(ghost));
			g.translate(gameUI.mazeUI.tf.getX(), gameUI.mazeUI.tf.getY());
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
			g.translate(-gameUI.mazeUI.tf.getX(), -gameUI.mazeUI.tf.getY());
		}
	}
}