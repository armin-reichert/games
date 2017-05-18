package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.entities.PacManState.Eating;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.play.PlayScene.PACMAN_HOME;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.util.stream.IntStream.range;

import java.awt.Graphics2D;

import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * A scene for testing Pac-Man's movement and eating behaviour.
 * 
 * @author Armin Reichert
 */
public class PacManMovementTestScene extends Scene<PacManMovementTestApp> {

	private Board board;
	private PacMan pacMan;

	public PacManMovementTestScene(PacManMovementTestApp app) {
		super(app);
	}

	@Override
	public void init() {
		board = new Board(app.assets.text("board.txt").split("\n"));
		pacMan = new PacMan(app, board, PACMAN_HOME);
		pacMan.setSpeed(8 * TILE_SIZE / app.settings.fps);
		pacMan.control.changeTo(Eating);
	};

	@Override
	public void update() {
		pacMan.update();
		if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
			board.resetContent();
		}
	}

	@Override
	public void draw(Graphics2D g) {
		PacManTheme theme = app.getTheme();
		drawSprite(g, 3, 0, theme.getBoardSprite());
		range(4, board.numRows - 3).forEach(row -> range(0, board.numCols).forEach(col -> {
			if (board.contains(row, col, Pellet)) {
				drawSprite(g, row, col, theme.getPelletSprite());
			} else if (board.contains(row, col, Energizer)) {
				drawSprite(g, row, col, theme.getEnergizerSprite());
			}
		}));
		drawGridLines(g, getWidth(), getHeight());
		pacMan.draw(g);
	}
}