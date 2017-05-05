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
public class TestScene extends Scene<PacManGame> {

	private Ghost ghost;
	private Tile startTile;
	private Tile targetTile;
	private List<Integer> route;
	private boolean ghostRunning;
	private MouseListener clickHandler;
	
	public TestScene() {
		super(Game);
		clickHandler = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (ghostRunning) {
					return;
				}
				Tile clickedTile = new Tile(e.getY() / TILE_SIZE, e.getX() / TILE_SIZE);
				route = Game.board.shortestRoute(targetTile, clickedTile);
				if (!route.isEmpty()) {
					Log.info("New target tile: " + clickedTile);
					startTile = targetTile;
					targetTile = clickedTile;
				}
			}
		};
	}

	@Override
	public void init() {
		Game.getShell().getCanvas().addMouseListener(clickHandler);
		ghost = new Ghost("Pinky", 4, 1);
		ghost.setTheme(Game.selectedTheme());
		Game.entities.add(ghost);
		reset();
	};

	private void exit() {
		Game.getShell().getCanvas().removeMouseListener(clickHandler);
		Game.entities.remove(ghost);
		Game.settings.set("testMode", false);
		Game.views.show(PlayScene.class);
	}

	private void reset() {
		startTile = new Tile(4,1);
		targetTile = new Tile(Board.NUM_ROWS - 4, Board.NUM_COLS - 2);
		route = Game.board.shortestRoute(startTile, targetTile);
		ghost.placeAt(startTile);
		ghostRunning = false;
		ghost.setAnimated(false);
	}
	
	@Override
	public void update() {

		if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_X)) {
			exit();
			return;
		}
		
		if (Keyboard.pressedOnce(KeyEvent.VK_SPACE)) {
			reset();
		}
		
		if (!ghost.currentTile().equals(targetTile)) {
			ghost.followRoute(targetTile);
			ghostRunning = true;
			ghost.setAnimated(true);
		} else {
			ghostRunning = false;
			ghost.setAnimated(false);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		final PacManUI theme = Game.selectedTheme();
		drawSprite(g, 3, 0, theme.getBoard());
		ghost.draw(g);
		g.setColor(Color.GREEN);
		g.fillRect(startTile.getCol() * TILE_SIZE, startTile.getRow() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		g.setColor(Color.YELLOW);
		g.fillRect(targetTile.getCol() * TILE_SIZE, targetTile.getRow() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		drawGridLines(g, Game.getWidth(), Game.getHeight());
		drawRouteMap(g, Game.board);
		g.setColor(Color.RED);
		drawRoute(g, Game.board, startTile, route);
	}
}
