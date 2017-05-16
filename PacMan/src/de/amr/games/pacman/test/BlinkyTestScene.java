package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.board.Board.NUM_COLS;
import static de.amr.games.pacman.core.board.Board.NUM_ROWS;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.PacManState;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.play.PlayScene;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * A scene for testing Blinky.
 * 
 * @author Armin Reichert
 */
public class BlinkyTestScene extends Scene<BlinkyTestApp> {

	private Board board;
	private PacMan pacMan;
	private Ghost blinky;

	public BlinkyTestScene(BlinkyTestApp app) {
		super(app);
	}

	@Override
	public void init() {
		app.settings.set("drawInternals", true);
		app.settings.title = "Blinky Test";

		board = new Board(app.assets.text("board.txt").split("\n"));

		pacMan = new PacMan(app, board, PlayScene.PACMAN_HOME);
		pacMan.setSpeed(8 * TILE_SIZE / app.settings.fps);

		blinky = new Ghost(app, board, PlayScene.BLINKY_HOME);
		blinky.control.state(Chasing).update = state -> blinky.followRoute(pacMan.currentTile());
		blinky.stateAfterFrightened = () -> GhostState.Chasing;
		blinky.setColor(Color.RED);
		blinky.setName("Blinky");
		blinky.setAnimated(true);
		blinky.setSpeed(pacMan.getSpeed() * .9f);

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
		PacManTheme theme = app.selectedTheme();
		drawSprite(g, 3, 0, theme.getBoardSprite());
		range(4, NUM_ROWS - 3).forEach(row -> range(0, NUM_COLS).forEach(col -> {
			if (board.contains(row, col, Pellet)) {
				drawSprite(g, row, col, theme.getPelletSprite());
			} else if (board.contains(row, col, Energizer)) {
				drawSprite(g, row, col, theme.getEnergizerSprite());
			}
		}));
		pacMan.draw(g);
		blinky.draw(g);
	}
}
