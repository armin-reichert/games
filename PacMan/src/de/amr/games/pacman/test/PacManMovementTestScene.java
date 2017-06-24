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
import static de.amr.games.pacman.misc.SceneHelper.drawText;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.State;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.PacManEvent;
import de.amr.games.pacman.core.entities.PacManState;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * A scene for testing Pac-Man's movement and eating behaviour.
 * 
 * @author Armin Reichert
 */
public class PacManMovementTestScene extends Scene<PacManMovementTestApp> {

	private static final Tile PACMAN_HOME = new Tile(26, 13);
	private static final Tile GHOST_HOUSE_LEFT = new Tile(17, 12);
	private static final Tile GHOST_HOUSE_MIDDLE = new Tile(17, 13);
	private static final Tile GHOST_HOUSE_RIGHT = new Tile(17, 15);

	private final PacManTheme theme;
	private final Board board;
	private PacMan pacMan;
	private Ghost pinky;
	private Ghost inky;
	private Ghost clyde;
	private int points;

	public PacManMovementTestScene(PacManMovementTestApp app) {
		super(app);
		theme = new ClassicTheme(app.assets);
		board = new Board(app.assets.text("board.txt"));
	}

	@Override
	public void init() {
		loadBoardContent();

		pacMan = new PacMan(app, board, () -> theme);
		app.entities.add(pacMan);

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
				points += 50;
				pacMan.receiveEvent(GotDrugs);
			} else if (content == Pellet) {
				Log.info("Pac-Man hat Pille gefressen auf Feld " + pacMan.currentTile());
				board.setContent(pacMan.currentTile(), None);
				points += 10;
			}
		};

		pacMan.onEnemyContact = enemy -> {
			if (pacMan.control.is(Aggressive)) {
				if (!enemy.control.is(GhostState.Dead)) {
					Log.info("Pac-Man tötet" + enemy.getName());
					points += 100;
					enemy.receiveEvent(GhostEvent.Killed);
				}
			} else if (pacMan.control.is(Dying)) {
				// nothing happens
			} else {
				Log.info(enemy.getName() + " tötet Pac-Man");
				pacMan.receiveEvent(PacManEvent.Killed);
				pinky.init();
				pinky.placeAt(GHOST_HOUSE_LEFT);
				pinky.control.state().setDuration(State.FOREVER);
			}
		};

		pinky = new Ghost(app, board, "Pinky", () -> theme);
		app.entities.add(pinky);
		pacMan.enemies().add(pinky);

		inky = new Ghost(app, board, "Inky", () -> theme);
		app.entities.add(inky);
		pacMan.enemies().add(inky);

		clyde = new Ghost(app, board, "Clyde", () -> theme);
		app.entities.add(clyde);
		pacMan.enemies().add(clyde);

		Stream.of(pinky, inky, clyde).forEach(ghost -> {

			ghost.setLogger(Application.Log);

			// Initialized
			ghost.control.changeOnTimeout(GhostState.Initialized, Chasing);

			// Chasing
			ghost.control.changeOnTimeout(Chasing, Scattering);
			ghost.control.changeOnInput(GhostEvent.Killed, Chasing, Dead);
			ghost.control.changeOnInput(GhostEvent.ScatteringStarts, Chasing, Scattering);
			
			// Scattering
			ghost.control.state(Scattering).update = state -> ghost.follow(getGhostHomeTile(ghost));
			ghost.control.change(Scattering, Waiting, () -> ghost.currentTile().equals(getGhostHomeTile(ghost)));
			ghost.control.changeOnInput(GhostEvent.ChasingStarts, Scattering, Chasing);

			// Dead
			ghost.control.state(Dead).update = state -> ghost.follow(getGhostHomeTile(ghost));
			ghost.control.change(Dead, Recovering, () -> ghost.currentTile().equals(getGhostHomeTile(ghost)));

			// Recovering
			ghost.control.state(Recovering).entry = state -> {
				state.setDuration(app.motor.secToTicks(3));
				ghost.adjust();
			};
			ghost.control.changeOnTimeout(Recovering, Chasing);

			// Waiting
			ghost.control.state(Waiting).entry = state -> {
				state.setDuration(app.motor.secToTicks(2));
				ghost.adjust();
			};
			ghost.control.changeOnTimeout(Waiting, Chasing);
		});

		// Chasing for Pinky
		pinky.control.state(Chasing).entry = state -> {
			state.setDuration(app.motor.secToTicks(10));
			pinky.speed = () -> getGhostSpeed(pinky);
		};
		pinky.control.state(Chasing).update = state -> pinky.follow(pacMan.currentTile());

		// Chasing for Inky
		inky.control.defineState(Chasing, new InkyChasingState());

		// Chasing for Clyde
		clyde.control.defineState(Chasing, new ClydeChasingState());

		start();
	};

	private class InkyChasingState extends State {

		private final Tile[] CORNERS = { new Tile(4, 1), new Tile(32, 1), new Tile(4, 26), new Tile(32, 26) };
		private Tile currentTarget;

		public InkyChasingState() {
			entry = state -> {
				state.setDuration(app.motor.secToTicks(20));
				inky.speed = () -> getGhostSpeed(inky);
				currentTarget = randomCorner();
			};
			update = state -> {
				inky.follow(currentTarget);
				if (inky.currentTile().equals(currentTarget)) {
					nextTarget();
				}
			};
		}

		private Tile randomCorner() {
			return CORNERS[new Random().nextInt(CORNERS.length)];
		}

		private void nextTarget() {
			Tile corner;
			do {
				corner = randomCorner();
			} while (corner.equals(currentTarget));
			currentTarget = corner;
		}
	}

	private class ClydeChasingState extends State {

		public ClydeChasingState() {
			entry = state -> {
				state.setDuration(app.motor.secToTicks(20));
				clyde.speed = () -> getGhostSpeed(clyde);

			};
			update = state -> {
				clyde.moveRandomly();
			};
		}
	};

	private Tile getGhostHomeTile(Ghost ghost) {
		if (ghost == pinky) {
			return GHOST_HOUSE_LEFT;
		}
		if (ghost == inky) {
			return GHOST_HOUSE_RIGHT;
		}
		return GHOST_HOUSE_MIDDLE;
	}

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

		pinky.init();
		pinky.placeAt(getGhostHomeTile(pinky));
		pinky.canEnterTile = tile -> board.getContent(tile) != Wall;
		pinky.control.state().setDuration(app.motor.secToTicks(2));

		inky.init();
		inky.placeAt(getGhostHomeTile(inky));
		inky.canEnterTile = tile -> board.getContent(tile) != Wall;
		inky.control.state().setDuration(app.motor.secToTicks(0));

		clyde.init();
		clyde.placeAt(getGhostHomeTile(clyde));
		clyde.canEnterTile = tile -> board.getContent(tile) != Wall;
		clyde.control.state().setDuration(app.motor.secToTicks(1));
	}

	private float normalSpeed() {
		return 2f;
	}

	private float fastSpeed() {
		return normalSpeed() * 1.5f;
	}

	private float getGhostSpeed(Ghost ghost) {
		if (ghost == inky) {
			return normalSpeed();
		}
		if (ghost == pinky) {
			return normalSpeed() * 1.2f;
		}
		return normalSpeed();
	}

	@Override
	public void update() {
		if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
			loadBoardContent();
		}
		app.entities.filter(Ghost.class).forEach(ghost -> ghost.update());
		pacMan.update();
	}

	@Override
	public void draw(Graphics2D pen) {
		drawSprite(pen, 3, 0, theme.getBoardSprite());
		range(4, board.numRows - 3).forEach(row -> range(0, board.numCols).forEach(col -> {
			if (board.contains(row, col, Pellet)) {
				drawSprite(pen, row, col, theme.getPelletSprite());
			} else if (board.contains(row, col, Energizer)) {
				drawSprite(pen, row, col, theme.getEnergizerSprite());
			}
		}));
		if (app.settings.getBool("drawGrid")) {
			drawGridLines(pen, getWidth(), getHeight());
		}
		pen.setColor(Color.WHITE);
		pen.setFont(theme.getTextFont());
		drawText(pen, 2, 1, points + " Punkte");

		pacMan.draw(pen);
		if (!pacMan.control.is(PacManState.Dying)) {
			app.entities.filter(Ghost.class).forEach(ghost -> ghost.draw(pen));
		}
	}
}