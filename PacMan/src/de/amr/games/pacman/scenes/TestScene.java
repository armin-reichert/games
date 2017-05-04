package de.amr.games.pacman.scenes;

import static de.amr.easy.game.Application.Log;
import static de.amr.games.pacman.PacManGame.Game;
import static de.amr.games.pacman.scenes.DrawUtil.drawGridLines;
import static de.amr.games.pacman.scenes.DrawUtil.drawRouteMap;
import static de.amr.games.pacman.scenes.DrawUtil.drawSprite;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.ui.PacManUI;

public class TestScene extends Scene<PacManGame> {

	private Tile targetTile;
	private Tile newTargetTile;

	public TestScene() {
		super(Game);
		Game.getShell().getCanvas().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Tile tile = new Tile(e.getY() / TILE_SIZE, e.getX() / TILE_SIZE);
				if (!Game.board.shortestRoute(targetTile, tile).isEmpty()) {
					Log.info("New target tile: " + tile);
					newTargetTile = tile;
				}
			}
		});
	}

	@Override
	public void init() {
		Game.entities.remove(Game.inky);
		Game.entities.remove(Game.pinky);
		Game.entities.remove(Game.clyde);
		Game.entities.remove(Game.pacMan);
		reset();
	};

	private void reset() {
		Game.blinky.placeAt(new Tile(4, 1));
		Game.blinky.setAnimated(true);
		targetTile = new Tile(Board.NUM_ROWS - 4, Board.NUM_COLS - 2);
	}

	@Override
	public void update() {
		if (!Game.blinky.currentTile().equals(targetTile)) {
			Game.blinky.followRoute(targetTile);
		} else {
			Game.blinky.setAnimated(false);
			if (newTargetTile != null) {
				targetTile = newTargetTile;
				newTargetTile = null;
			}
		}
		if (Keyboard.pressedOnce(KeyEvent.VK_SPACE)) {
			reset();
		}
	}

	@Override
	public void draw(Graphics2D g) {
		final PacManUI theme = Game.selectedTheme();
		drawSprite(g, 3, 0, theme.getBoard());
		Game.entities.all().forEach(entity -> entity.draw(g));
		g.setColor(Color.GREEN);
		g.fillRect(targetTile.getCol() * TILE_SIZE, targetTile.getRow() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		g.setColor(Color.YELLOW);
		if (newTargetTile != null) {
			g.fillRect(newTargetTile.getCol() * TILE_SIZE, newTargetTile.getRow() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
		}
		drawGridLines(g, Game.getWidth(), Game.getHeight());
		drawRouteMap(g, Game.board);
	}
}
