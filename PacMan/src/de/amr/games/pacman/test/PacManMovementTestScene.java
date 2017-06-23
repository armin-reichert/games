package de.amr.games.pacman.test;

import static de.amr.easy.game.Application.Log;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.None;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.board.TileContent.Wall;
import static de.amr.games.pacman.core.entities.PacManEvent.GotDrugs;
import static de.amr.games.pacman.core.entities.PacManEvent.StartWalking;
import static de.amr.games.pacman.core.entities.PacManState.Aggressive;
import static de.amr.games.pacman.core.entities.PacManState.Dying;
import static de.amr.games.pacman.core.entities.PacManState.Initialized;
import static de.amr.games.pacman.core.entities.PacManState.Peaceful;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Waiting;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static java.util.stream.IntStream.range;

import java.awt.Graphics2D;

import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
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
		board = new Board(app.assets.text("board.txt"));
	}

	@Override
	public void init() {

		loadBoardContent();

		pacMan = new PacMan(app, board, () -> theme);

		// Initialized

		pacMan.control.changeOnInput(PacManEvent.StartWalking, Initialized, Peaceful);

		// Peaceful

		pacMan.control.state(Peaceful).entry = state -> pacMan.speed = this::normalSpeed;

		pacMan.control.state(Peaceful).update = state -> pacMan.walk();

		pacMan.control.changeOnInput(PacManEvent.GotDrugs, Peaceful, Aggressive);

		pacMan.control.changeOnInput(PacManEvent.Killed, Peaceful, Dying);

		// Aggressive

		pacMan.control.state(Aggressive).entry = state -> {
			state.setDuration(app.motor.secToTicks(3));
			pacMan.speed = this::fastSpeed;
		};

		pacMan.control.state(Aggressive).update = state -> pacMan.walk();

		pacMan.control.changeOnTimeout(Aggressive, Peaceful);

		pacMan.control.changeOnInput(GotDrugs, Aggressive, Aggressive, (oldState, newState) -> {
			newState.setDuration(app.motor.secToTicks(3));
		});

		// Dying

		pacMan.control.state(Dying).entry = state -> {
			app.assets.sound("sfx/die.mp3").play();
			theme.getPacManDyingSprite().setAnimated(true);
		};

		pacMan.control.state(Dying).exit = state -> {
			theme.getPacManDyingSprite().resetAnimation();
			start();
		};

		pacMan.control.change(Dying, Initialized, () -> !app.assets.sound("sfx/die.mp3").isRunning());

		// Event handlers

		pacMan.onContentFound = content -> {
			if (content == Energizer) {
				Log.info("Pac-Man hat Energizer gefressen auf Feld " + pacMan.currentTile());
				board.setContent(pacMan.currentTile(), None);
				pacMan.receiveEvent(GotDrugs);
			} else if (content == Pellet) {
				Log.info("Pac-Man hat Pille gefressen auf Feld " + pacMan.currentTile());
				board.setContent(pacMan.currentTile(), None);
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

		// Initialized

		ghost.control.changeOnTimeout(GhostState.Initialized, Chasing);

		// Chasing

		ghost.control.state(Chasing).entry = state -> {
			state.setDuration(app.motor.secToTicks(5));
			ghost.speed = this::normalSpeed;
		};

		ghost.control.state(Chasing).update = state -> ghost.follow(pacMan.currentTile());

		ghost.control.changeOnTimeout(Chasing, Scattering);

		ghost.control.changeOnInput(GhostEvent.Killed, Chasing, Dead);

		ghost.control.changeOnInput(GhostEvent.ScatteringStarts, Chasing, Scattering);

		// Scattering

		ghost.control.state(Scattering).update = state -> ghost.follow(GHOST_HOUSE);

		ghost.control.change(Scattering, Waiting, () -> ghost.currentTile().equals(GHOST_HOUSE));

		ghost.control.changeOnInput(GhostEvent.ChasingStarts, Scattering, Chasing);

		// Dead

		ghost.control.state(Dead).update = state -> ghost.follow(GHOST_HOUSE);

		ghost.control.change(Dead, Recovering, () -> ghost.currentTile().equals(GHOST_HOUSE));

		// Recovering

		ghost.control.state(Recovering).entry = state -> state.setDuration(app.motor.secToTicks(3));

		ghost.control.changeOnTimeout(Recovering, Chasing);

		// Waiting

		ghost.control.state(Waiting).entry = state -> state.setDuration(app.motor.secToTicks(4));

		ghost.control.changeOnTimeout(Waiting, Chasing);

		app.entities.add(ghost);

		pacMan.enemies().add(ghost);

		start();
	};

	private void loadBoardContent() {
		board.loadContent();
		board.setContent(new Tile(32, 12), Energizer);
		board.setContent(new Tile(32, 1), Energizer);
		board.setContent(new Tile(32, 26), Energizer);
	}

	private void start() {
		pacMan.init();
		pacMan.placeAt(PACMAN_HOME);
		pacMan.speed = this::normalSpeed;
		pacMan.receiveEvent(StartWalking);
		ghost.init();
		ghost.placeAt(GHOST_HOUSE);
		ghost.canEnterTile = tile -> board.getContent(tile) != Wall;
		ghost.control.state().setDuration(app.motor.secToTicks(3));
	}

	private float normalSpeed() {
		return 2f;
	}

	private float fastSpeed() {
		return normalSpeed() * 1.5f;
	}

	@Override
	public void update() {
		if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
			loadBoardContent();
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