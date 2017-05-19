package de.amr.games.pacman.test;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.entities.PacManState.Eating;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.play.PlayScene.BLINKY_HOME;
import static de.amr.games.pacman.play.PlayScene.PACMAN_HOME;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Tests Blinky's behavior.
 * 
 * @author Armin Reichert
 */
public class BlinkyTestScene extends Scene<BlinkyTestApp> {

	private Board board;
	private PacMan pacMan;
	private Ghost blinky;
	private Random rand = new Random();

	public BlinkyTestScene(BlinkyTestApp app) {
		super(app);
	}

	@Override
	public void init() {
		board = new Board(app.assets.text("board.txt").split("\n"));

		pacMan = new PacMan(app, board, PACMAN_HOME);
		pacMan.speed = () -> (float) Math.round(8f * TILE_SIZE / app.motor.getFrequency());
		pacMan.onGhostMet = ghost -> {
			pacMan.placeAt(PACMAN_HOME);
			int dir = rand.nextBoolean() ? E : W;
			pacMan.setMoveDir(dir);
			pacMan.setNextMoveDir(dir);
			ghost.placeAt(BLINKY_HOME);
			dir = rand.nextBoolean() ? W : E;
			ghost.setMoveDir(dir); // TODO without this, ghost might get stuck
			ghost.setNextMoveDir(dir);
		};

		blinky = new Ghost(app, board, "Blinky", BLINKY_HOME);
		blinky.control.state(Chasing).update = state -> blinky.follow(pacMan.currentTile());
		blinky.setColor(Color.RED);
		blinky.setAnimated(true);
		blinky.speed = () -> pacMan.speed.get() * .9f;

		app.entities.add(pacMan, blinky); // needed for onGhostMet event handler!

		pacMan.control.changeTo(Eating);
		blinky.control.changeTo(Chasing);
	};

	@Override
	public void update() {
		if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
			board.resetContent();
		}
		pacMan.update();
		blinky.update();
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
		blinky.draw(g);
	}
}