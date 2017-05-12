package de.amr.games.pacman.scenes;

import static de.amr.games.pacman.PacManGame.Game;
import static de.amr.games.pacman.data.Board.NUM_COLS;
import static de.amr.games.pacman.data.Board.NUM_ROWS;
import static de.amr.games.pacman.data.TileContent.Energizer;
import static de.amr.games.pacman.data.TileContent.Pellet;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.scenes.DrawUtil.drawSprite;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.entities.PacMan;
import de.amr.games.pacman.entities.PacManState;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.ui.PacManUI;

/**
 * A scene for testing Blinky.
 * 
 * @author Armin Reichert
 */
public class BlinkyTestScene extends Scene<PacManGame> {

	private PacManUI theme;
	private Board board;
	private PacMan pacMan;
	private Ghost blinky;

	public BlinkyTestScene() {
		super(Game);
	}

	@Override
	public void init() {
		Game.settings.set("drawInternals", true);
		Game.settings.title = "Blinky Test";

		theme = Game.selectedTheme();
		board = new Board(Game.assets.text("board.txt").split("\n"));

		pacMan = new PacMan(board, PlayScene.PACMAN_HOME);
		pacMan.speed = Game.getPacManSpeed(1);
		pacMan.setTheme(Game.selectedTheme());

		blinky = new Ghost("Blinky", board, PlayScene.BLINKY_HOME);
		blinky.control.state(Chasing).update = state -> blinky.enterRoute(pacMan.currentTile());
		blinky.stateAfterFrightened = () -> GhostState.Chasing;
		blinky.color = Color.RED;
		blinky.setTheme(theme);
		blinky.setAnimated(true);
		blinky.speed = Game.getGhostSpeedNormal(1);

		pacMan.control.changeTo(PacManState.Eating);
		blinky.control.changeTo(GhostState.Chasing);
	};

	@Override
	public void update() {
		pacMan.update();
		blinky.update();
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, theme.getBoard());
		range(4, NUM_ROWS - 3).forEach(row -> range(0, NUM_COLS).forEach(col -> {
			if (board.contains(row, col, Pellet)) {
				drawSprite(g, row, col, theme.getPellet());
			} else if (board.contains(row, col, Energizer)) {
				drawSprite(g, row, col, theme.getEnergizer());
			}
		}));
		pacMan.draw(g);
		blinky.draw(g);
	}
}
