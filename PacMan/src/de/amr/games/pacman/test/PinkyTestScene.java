package de.amr.games.pacman.test;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Initialized;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.play.PlayScene.GHOST_HOUSE_ENTRY;
import static de.amr.games.pacman.play.PlayScene.PACMAN_HOME;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.Application;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.PacManEvent;
import de.amr.games.pacman.core.entities.PacManState;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.AmbushPacMan;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent;
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
	private final Board board;
	private PacMan pacMan;
	private Ghost pinky;

	public PinkyTestScene(PinkyTestApp app) {
		super(app);
		theme = new ClassicTheme(app.assets);
		board = new Board(app.assets.text("board.txt").split("\n"));
	}

	@Override
	public void init() {
		pacMan = new PacMan(app, board, () -> theme);
		pacMan.init();
		pacMan.speed = () -> (float) Math.round(8f * TILE_SIZE / app.motor.getFrequency());
		pacMan.onEnemyContact = ghost -> pacMan.handleEvent(PacManEvent.Killed);
		pacMan.control.state(PacManState.Dying).entry = state -> {
			state.setDuration(app.motor.toFrames(2));
		};
		pacMan.control.changeOnTimeout(PacManState.Dying, PacManState.Peaceful, state -> start());
		pacMan.setLogger(Application.Log);

		pinky = new Ghost(app, board, "Pinky", () -> theme);
		pinky.init();
		pinky.control.state(Chasing, new AmbushPacMan(pinky, pacMan, 4));
		pinky.control.changeOnInput(GhostEvent.ChasingStarts, Initialized, Chasing);
		pinky.restoreState = () -> pinky.handleEvent(GhostEvent.ChasingStarts);
		pinky.setColor(Color.PINK);
		pinky.setAnimated(true);
		pinky.speed = () -> .9f * pacMan.speed.get();

		pacMan.enemies().add(pinky);
		start();
	};

	private void start() {
		pacMan.placeAt(PACMAN_HOME);
		int dir = rand.nextBoolean() ? E : W;
		pacMan.setMoveDir(dir);
		pacMan.setNextMoveDir(dir);
		pacMan.handleEvent(PacManEvent.StartWalking);

		pinky.placeAt(GHOST_HOUSE_ENTRY);
		dir = rand.nextBoolean() ? W : E;
		pinky.setMoveDir(dir); // TODO without this, ghost might get stuck
		pinky.setNextMoveDir(dir);
		pinky.handleEvent(GhostEvent.ChasingStarts);
	}

	@Override
	public void update() {
		if (!pacMan.control.is(PacManState.Dying)) {
			pinky.update();
		}
		pacMan.update();
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
		if (!pacMan.control.is(PacManState.Dying)) {
			pinky.draw(g);
		}
		pacMan.draw(g);
	}
}