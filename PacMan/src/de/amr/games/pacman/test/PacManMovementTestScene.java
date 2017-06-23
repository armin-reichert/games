package de.amr.games.pacman.test;

import static de.amr.easy.game.Application.Log;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.board.TileContent.Wall;
import static de.amr.games.pacman.core.entities.PacManEvent.GotDrugs;
import static de.amr.games.pacman.core.entities.PacManEvent.StartWalking;
import static de.amr.games.pacman.core.entities.PacManState.Aggressive;
import static de.amr.games.pacman.core.entities.PacManState.Dying;
import static de.amr.games.pacman.core.entities.PacManState.Initialized;
import static de.amr.games.pacman.core.entities.PacManState.Peaceful;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static java.util.stream.IntStream.range;

import java.awt.Graphics2D;

import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.PacManEvent;
import de.amr.games.pacman.core.entities.PacManState;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.core.statemachine.State;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * A scene for testing Pac-Man's movement and eating behaviour.
 * 
 * @author Armin Reichert
 */
public class PacManMovementTestScene extends Scene<PacManMovementTestApp> {

	static final Tile PACMAN_HOME = new Tile(26, 13);
	static final Tile GHOST_HOUSE = new Tile(17, 15);

	private final PacManTheme theme;
	private final Board board;
	private PacMan pacMan;
	private Ghost ghost;

	public PacManMovementTestScene(PacManMovementTestApp app) {
		super(app);
		theme = new ClassicTheme(app.assets);
		board = new Board(app.assets.text("board.txt").split("\n"));
	}

	private void resetBoard() {
		board.resetContent();
		board.setContent(new Tile(32, 12), TileContent.Energizer);
		board.setContent(new Tile(32, 1), TileContent.Energizer);
		board.setContent(new Tile(32, 26), TileContent.Energizer);
	}

	@Override
	public void init() {

		resetBoard();

		pacMan = new PacMan(app, board, () -> theme);

		pacMan.control.changeOnInput(StartWalking, Initialized, Peaceful);

		pacMan.control.state(Peaceful).entry = state -> pacMan.speed = this::normalSpeed;

		pacMan.control.state(Peaceful).update = state -> pacMan.walk();

		pacMan.control.changeOnInput(GotDrugs, Peaceful, Aggressive);

		pacMan.control.changeOnInput(PacManEvent.Killed, Peaceful, Dying);

		pacMan.control.state(Aggressive).entry = state -> {
			pacMan.speed = this::fastSpeed;
			state.setDuration(3 * app.motor.getFrequency());
		};

		pacMan.control.changeOnTimeout(Aggressive, Peaceful);

		pacMan.control.state(Aggressive).update = state -> pacMan.walk();

		pacMan.control.changeOnInput(GotDrugs, Aggressive, Aggressive, (oldState, newState) -> {
			newState.setDuration(3 * app.motor.getFrequency());
		});

		pacMan.control.state(Dying).entry = state -> {
			app.assets.sound("sfx/die.mp3").play();
			theme.getPacManDyingSprite().setAnimated(true);
		};

		pacMan.control.state(Dying).exit = state -> {
			theme.getPacManDyingSprite().resetAnimation();
			start();
		};

		pacMan.control.change(Dying, Initialized, () -> !app.assets.sound("sfx/die.mp3").isRunning());

		pacMan.onContentFound = content -> {
			if (content == Energizer) {
				Log.info("Pac-Man hat Energizer gefressen auf Feld " + pacMan.currentTile());
				board.setContent(pacMan.currentTile(), TileContent.None);
				pacMan.receiveEvent(GotDrugs);
			} else if (content == Pellet) {
				Log.info("Pac-Man hat Pille gefressen auf Feld " + pacMan.currentTile());
				board.setContent(pacMan.currentTile(), TileContent.None);
			}
		};

		pacMan.onEnemyContact = enemy -> {
			if (pacMan.control.is(Aggressive)) {
				Log.info("Pac-Man tötet" + enemy.getName());
				enemy.receiveEvent(GhostEvent.Killed);
			} else if (pacMan.control.is(Dying)) {
				// nothing happens
			} else {
				Log.info(enemy.getName() + " tötet Pac-Man");
				pacMan.receiveEvent(PacManEvent.Killed);
				ghost.init();
				ghost.placeAt(GHOST_HOUSE);
				ghost.control.state().setDuration(State.FOREVER);
			}
		};

		app.entities.add(pacMan);

		ghost = new Ghost(app, board, "Pinky", () -> theme);

		ghost.control.changeOnTimeout(GhostState.Initialized, GhostState.Chasing);

		ghost.control.state(GhostState.Chasing).entry = state -> {
			ghost.speed = this::normalSpeed;
			state.setDuration(app.motor.toFrames(5));
		};

		ghost.control.state(GhostState.Chasing).update = state -> {
			ghost.follow(pacMan.currentTile());
		};

		ghost.control.changeOnTimeout(GhostState.Chasing, GhostState.Scattering);
		ghost.control.changeOnInput(GhostEvent.Killed, GhostState.Chasing, GhostState.Dead);
		ghost.control.changeOnInput(GhostEvent.ScatteringStarts, GhostState.Chasing, GhostState.Scattering);

		ghost.control.state(GhostState.Scattering).update = state -> {
			ghost.follow(GHOST_HOUSE);
		};

		ghost.control.change(GhostState.Scattering, GhostState.Waiting, () -> ghost.currentTile().equals(GHOST_HOUSE));
		ghost.control.changeOnInput(GhostEvent.ChasingStarts, GhostState.Scattering, GhostState.Chasing);

		ghost.control.state(GhostState.Dead).update = state -> ghost.follow(GHOST_HOUSE);

		ghost.control.change(GhostState.Dead, GhostState.Recovering, () -> ghost.currentTile().equals(GHOST_HOUSE));

		ghost.control.state(GhostState.Recovering).entry = state -> state.setDuration(app.motor.toFrames(3));

		ghost.control.changeOnTimeout(GhostState.Recovering, GhostState.Chasing);

		ghost.control.state(GhostState.Waiting).entry = state -> state.setDuration(app.motor.toFrames(4));

		ghost.control.changeOnTimeout(GhostState.Waiting, GhostState.Chasing);

		app.entities.add(ghost);

		pacMan.enemies().add(ghost);

		start();
	};

	private void start() {
		pacMan.init();
		pacMan.placeAt(PACMAN_HOME);
		pacMan.speed = this::normalSpeed;
		pacMan.receiveEvent(StartWalking);
		ghost.init();
		ghost.placeAt(GHOST_HOUSE);
		ghost.canEnterTile = tile -> board.getContent(tile) != Wall;
		ghost.control.state().setDuration(app.motor.toFrames(3));
	}

	private float normalSpeed() {
		return 2f;
	}

	private float fastSpeed() {
		return normalSpeed() * 2;
	}

	@Override
	public void update() {
		if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
			resetBoard();
		}
		super.update();
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
		drawGridLines(g, getWidth(), getHeight());
		pacMan.draw(g);
		if (!pacMan.control.is(PacManState.Dying)) {
			ghost.draw(g);
		}
	}
}