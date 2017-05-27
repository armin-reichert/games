package de.amr.games.pacman.test;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.entities.PacManState.Eating;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.play.PlayScene.GHOST_HOUSE_ENTRY;
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
import de.amr.games.pacman.core.entities.ghost.behaviors.FollowTileAheadOfPacMan;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * A scene for testing Pinky.
 * 
 * @author Armin Reichert
 */
public class PinkyTestScene extends Scene<PinkyTestApp> {

	private Board board;
	private PacMan pacMan;
	private Ghost pinky;
	private Random rand = new Random();

	public PinkyTestScene(PinkyTestApp app) {
		super(app);
	}

	@Override
	public void init() {
		board = new Board(app.assets.text("board.txt").split("\n"));

		pacMan = new PacMan(app, board, PACMAN_HOME);
		pacMan.speed = () -> (float) Math.round(8f * TILE_SIZE / app.motor.getFrequency());

		pinky = new Ghost(app, board, "Pinky", GHOST_HOUSE_ENTRY);
		pinky.control.state(Chasing, new FollowTileAheadOfPacMan(pinky, pacMan, 4));
		pinky.stateToRestore = () -> Chasing;
		pinky.setColor(Color.PINK);
		pinky.setAnimated(true);
		pinky.speed = () -> .9f * pacMan.speed.get();

		pacMan.control.changeTo(Eating);
		pinky.control.changeTo(Chasing);
	};

	@Override
	public void update() {
		pacMan.update();
		pinky.update();
		if (pacMan.currentTile().equals(pinky.currentTile())) {
			pacMan.placeAt(PACMAN_HOME);
			int dir = rand.nextBoolean() ? E : W;
			pacMan.setMoveDir(dir);
			pacMan.setNextMoveDir(dir);
			pinky.placeAt(GHOST_HOUSE_ENTRY);
			dir = rand.nextBoolean() ? W : E;
			pinky.setMoveDir(dir); // TODO without this, ghost might get stuck
			pinky.setNextMoveDir(dir);
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
		if (app.settings.getBool("drawGrid")) {
			drawGridLines(g, getWidth(), getHeight());
		}
		pacMan.draw(g);
		pinky.draw(g);
	}
}