package de.amr.games.pacman.test;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
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
import de.amr.games.pacman.core.entities.ghost.behaviors.AmbushPacMan;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * A scene for testing Pinky.
 * 
 * @author Armin Reichert
 */
public class PinkyTestScene extends Scene<PinkyTestApp> {

	private final PacManTheme theme;
	private final Random rand = new Random();
	private Board board;
	private PacMan pacMan;
	private Ghost pinky;
	private int pauseTimer;

	public PinkyTestScene(PinkyTestApp app) {
		super(app);
		theme = new ClassicTheme(app.assets);
	}

	@Override
	public void init() {
		board = new Board(app.assets.text("board.txt").split("\n"));

		pacMan = new PacMan(app, board, () -> theme);
		pacMan.init();
		pacMan.speed = () -> (float) Math.round(8f * TILE_SIZE / app.motor.getFrequency());
		pacMan.onEnemyContact = ghost -> {
			app.assets.sound("sfx/die.mp3").play();
			pause(2);
		};

		pinky = new Ghost(app, board, "Pinky", () -> theme);
		pinky.init();
		pinky.state(Chasing, new AmbushPacMan(pinky, pacMan, 4));
		pinky.stateToRestore = () -> Chasing;
		pinky.setColor(Color.PINK);
		pinky.setAnimated(true);
		pinky.speed = () -> .9f * pacMan.speed.get();

		pacMan.enemies().add(pinky);
		reset();

		pacMan.beginWalking();
		pinky.beginChasing();
	};

	private void reset() {
		pacMan.placeAt(PACMAN_HOME);
		int dir = rand.nextBoolean() ? E : W;
		pacMan.setMoveDir(dir);
		pacMan.setNextMoveDir(dir);

		pinky.placeAt(GHOST_HOUSE_ENTRY);
		dir = rand.nextBoolean() ? W : E;
		pinky.setMoveDir(dir); // TODO without this, ghost might get stuck
		pinky.setNextMoveDir(dir);
	}

	private void pause(int seconds) {
		pauseTimer = app.motor.toFrames(seconds);
	}

	@Override
	public void update() {
		if (pauseTimer > 0) {
			--pauseTimer;
			if (pauseTimer == 0) {
				reset();
			}
			return;
		}
		pacMan.update();
		pinky.update();
	}

	@Override
	public void draw(Graphics2D g) {
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