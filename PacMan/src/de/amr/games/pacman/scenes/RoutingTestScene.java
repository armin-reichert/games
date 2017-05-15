package de.amr.games.pacman.scenes;

import static de.amr.easy.game.Application.Log;
import static de.amr.games.pacman.PacManGame.Game;
import static de.amr.games.pacman.scenes.DrawUtil.drawGridLines;
import static de.amr.games.pacman.scenes.DrawUtil.drawRoute;
import static de.amr.games.pacman.scenes.DrawUtil.drawRouteMap;
import static de.amr.games.pacman.scenes.DrawUtil.drawSprite;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.ui.PacManUI;

/**
 * A scene for interactive testing of ghost routing through the maze.
 * 
 * @author Armin Reichert
 *
 */
public class RoutingTestScene extends Scene<PacManGame> {

	private PacManUI theme;
	private Board board;
	private Ghost ghost;
	private Tile startTile;
	private Tile targetTile;
	private List<Integer> route;
	private boolean ghostRunning;
	private MouseListener clickHandler;

	public RoutingTestScene() {
		super(Game);
	}

	@Override
	public void init() {
		theme = Game.selectedTheme();
		board = new Board(Game.assets.text("board.txt").split("\n"));
		clickHandler = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Tile clickedTile = new Tile(e.getY() / TILE_SIZE, e.getX() / TILE_SIZE);
				if (ghostRunning) {
					route = board.shortestRoute(startTile, clickedTile);
					if (!route.isEmpty()) {
						ghost.placeAt(startTile);
						targetTile = clickedTile;
						Log.info("New target tile: " + targetTile);
					}
				} else {
					route = board.shortestRoute(targetTile, clickedTile);
					if (!route.isEmpty()) {
						startTile = targetTile;
						targetTile = clickedTile;
						Log.info("New target tile: " + targetTile);
					}
				}
			}
		};
		Game.getShell().getCanvas().addMouseListener(clickHandler);
		ghost = new Ghost(board, new Tile(4, 1));
		ghost.setName("Pinky");
		ghost.setTheme(theme);
		ghost.setSpeed(Game.getGhostSpeedNormal(1));
		reset();
	};

	private void reset() {
		startTile = new Tile(4, 1);
		targetTile = new Tile(Board.NUM_ROWS - 4, Board.NUM_COLS - 2);
		route = board.shortestRoute(startTile, targetTile);
		ghost.placeAt(startTile);
		ghostRunning = false;
		ghost.setAnimated(false);
	}

	@Override
	public void update() {
		if (Keyboard.pressedOnce(KeyEvent.VK_SPACE)) {
			reset();
		} else if (!ghost.currentTile().equals(targetTile)) {
			ghostRunning = true;
			ghost.setAnimated(true);
			ghost.followRoute(targetTile);
		} else {
			ghostRunning = false;
			ghost.setAnimated(false);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, theme.getBoard());
		ghost.draw(g);
		g.setColor(Color.GREEN);
		g.fillRect(startTile.getCol() * TILE_SIZE, startTile.getRow() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		g.setColor(Color.YELLOW);
		g.fillRect(targetTile.getCol() * TILE_SIZE, targetTile.getRow() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		drawGridLines(g, Game.getWidth(), Game.getHeight());
		drawRouteMap(g, board);
		g.setColor(Color.RED);
		drawRoute(g, board, startTile, route);
	}
}
