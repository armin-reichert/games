package de.amr.games.pacman.play;

import static de.amr.easy.game.Application.Log;
import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Bonus;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.None;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.misc.SceneHelper.drawText;
import static de.amr.games.pacman.misc.SceneHelper.drawTextCentered;
import static de.amr.games.pacman.play.PlaySceneModel.BONUS1_PELLETS_LEFT;
import static de.amr.games.pacman.play.PlaySceneModel.BONUS2_PELLETS_LEFT;
import static de.amr.games.pacman.play.PlaySceneModel.CHASING_TIMES;
import static de.amr.games.pacman.play.PlaySceneModel.POINTS_FOR_ENERGIZER;
import static de.amr.games.pacman.play.PlaySceneModel.POINTS_FOR_KILLING_FIRST_GHOST;
import static de.amr.games.pacman.play.PlaySceneModel.POINTS_FOR_PELLET;
import static de.amr.games.pacman.play.PlaySceneModel.SCATTERING_TIMES;
import static de.amr.games.pacman.play.PlaySceneModel.SCORE_FOR_EXTRALIFE;
import static de.amr.games.pacman.play.PlaySceneModel.WAIT_TICKS_AFTER_ENERGIZER_EATEN;
import static de.amr.games.pacman.play.PlaySceneModel.WAIT_TICKS_AFTER_PELLET_EATEN;
import static de.amr.games.pacman.play.PlayState.Crashing;
import static de.amr.games.pacman.play.PlayState.GameOver;
import static de.amr.games.pacman.play.PlayState.Initializing;
import static de.amr.games.pacman.play.PlayState.Playing;
import static de.amr.games.pacman.play.PlayState.Ready;
import static de.amr.games.pacman.play.PlayState.StartingLevel;
import static de.amr.games.pacman.theme.PacManTheme.SPRITE_SIZE;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_G;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_K;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_T;
import static java.lang.String.format;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.common.FlashText;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.BonusSymbol;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.PacManState;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.AmbushPacMan;
import de.amr.games.pacman.core.entities.ghost.behaviors.ChaseWithPartner;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.core.entities.ghost.behaviors.LoopAroundWalls;
import de.amr.games.pacman.core.statemachine.StateMachine;
import de.amr.games.pacman.misc.Highscore;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * The play scene of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<PacManGame> {

	// Prominent board locations
	public static final Tile PACMAN_HOME = new Tile(26, 13);
	public static final Tile BLINKY_HOME = new Tile(14, 13);
	public static final Tile INKY_HOME = new Tile(17, 11);
	public static final Tile PINKY_HOME = new Tile(17, 13);
	public static final Tile CLYDE_HOME = new Tile(17, 15);
	public static final Tile GHOST_HOUSE_ENTRY = new Tile(14, 13);
	public static final Tile BONUS_TILE = new Tile(20, 13);
	public static final Tile LEFT_UPPER_CORNER = new Tile(4, 1);
	public static final Tile RIGHT_UPPER_CORNER = new Tile(4, 26);
	public static final Tile LEFT_LOWER_CORNER = new Tile(32, 1);
	public static final Tile RIGHT_LOWER_CORNER = new Tile(32, 26);

	// Game control
	private final StateMachine<PlayState, PlaySceneInput> playControl;
	private GhostAttackTimer ghostAttackTimer;

	// Entities
	private PacMan pacMan;
	private Set<Ghost> ghosts;

	// Scene-specific data
	private final PlaySceneModel model;
	private final Board board;
	private final Highscore highscore;
	private final List<BonusSymbol> bonusList;
	private int level;
	private int lives;
	private int score;
	private int bonusTimeRemaining;
	private int nextGhostPoints;
	private int ghostsEatenAtLevel;

	/**
	 * State machine which controls the game play.
	 */
	private class PlayControl extends StateMachine<PlayState, PlaySceneInput> {

		private void configureTracing() {
			setLogger(Log, app.motor.getFrequency());
			// ghostAttackTimer.setLogger(Log);
			// pacMan.setLogger(Log);
			ghosts.forEach(ghost -> ghost.setLogger(Log));
		}

		public PlayControl() {

			super("Play control", new EnumMap<>(PlayState.class), Initializing);

			// Initializing

			state(Initializing).entry = state -> {
				level = 0;
				lives = 3;
				score = 0;
				bonusList.clear();
				createPacManAndGhosts();
				pacMan.placeAt(PACMAN_HOME);
				ghosts.forEach(ghost -> {
					ghost.init();
					ghost.speed = () -> 0f;
					ghost.placeAt(getGhostHomeTile(ghost));
				});
				nextLevel();
				app.getThemeManager().getTheme().getEnergizerSprite().setAnimated(false);
				app.assets.sound("sfx/insert-coin.mp3").play();

				configureTracing();
			};

			change(Initializing, Ready, () -> !app.assets.sound("sfx/insert-coin.mp3").isRunning());

			// Ready to rumble

			state(Ready).entry = state -> {
				app.getThemeManager().getTheme().getEnergizerSprite().setAnimated(true);
				ghosts.forEach(ghost -> {
					ghost.speed = () -> model.getGhostSpeed(ghost, level);
					ghost.beginWaiting();
				});
			};

			change(Ready, StartingLevel, () -> Keyboard.keyPressedOnce(VK_SPACE));

			// StartingLevel

			state(StartingLevel).entry = state -> {
				pacMan.init();
				pacMan.placeAt(PACMAN_HOME);
				ghosts.forEach(ghost -> {
					ghost.placeAt(getGhostHomeTile(ghost));
					ghost.beginWaiting();
				});
				app.assets.sound("sfx/ready.mp3").play();
			};

			state(StartingLevel).update = state -> app.entities.all().forEach(GameEntity::update);

			change(StartingLevel, Playing, () -> !app.assets.sound("sfx/ready.mp3").isRunning());

			// Playing

			state(Playing).entry = state -> {
				pacMan.init();
				pacMan.placeAt(PACMAN_HOME);
				pacMan.speed = () -> model.getPacManSpeed(pacMan, level);
				pacMan.beginWalking();

				ghosts.forEach(ghost -> {
					ghost.placeAt(getGhostHomeTile(ghost));
					ghost.beginWaiting();
				});

				app.getThemeManager().getTheme().getEnergizerSprite().setAnimated(true);
				ghostAttackTimer.init();
			};

			state(Playing).update = state -> {
				if (bonusTimeRemaining == 1) {
					removeBonus();
				}
				ghostAttackTimer.update();
				app.entities.all().forEach(GameEntity::update);
			};

			state(Playing).exit = state -> {
				app.entities.removeAll(FlashText.class);
			};

			changeOnInput(PlaySceneInput.PacManCrashed, Playing, Crashing);

			change(Playing, StartingLevel, () -> board.count(Pellet) == 0 && board.count(Energizer) == 0,
					state -> nextLevel());

			// Crashing

			state(Crashing).entry = state -> {
				app.assets.sounds().forEach(Sound::stop);
				app.assets.sound("sfx/die.mp3").play();
				app.getThemeManager().getTheme().getEnergizerSprite().setAnimated(false);
				removeBonus();
				pacMan.killed();
				Log.info("PacMan killed, lives remaining: " + lives);
			};

			state(Crashing).update = state -> pacMan.update();

			change(Crashing, Playing, () -> !app.assets.sound("sfx/die.mp3").isRunning() && lives > 0);

			change(Crashing, GameOver, () -> !app.assets.sound("sfx/die.mp3").isRunning() && lives == 0);

			// GameOver

			state(GameOver).entry = state -> {
				checkHighscore();
				app.entities.all().forEach(entity -> entity.setAnimated(false));
				Log.info("Game over.");
			};

			change(GameOver, Initializing, () -> Keyboard.keyPressedOnce(VK_SPACE), state -> app.entities.removeAll());
		}
	}

	public PlayScene(PacManGame app) {
		super(app);
		playControl = new PlayControl();
		board = new Board(app.assets.text("board.txt").split("\n"));
		model = new PlaySceneModel(board, app.motor, 8 * TILE_SIZE);
		highscore = new Highscore("pacman-hiscore.txt");
		bonusList = new ArrayList<>();
	}

	@Override
	public void init() {
		playControl.init();
	}

	@Override
	public void update() {
		handleInput();
		playControl.update();
	}

	private void handleInput() {
		if (keyPressedOnce(VK_ALT, VK_I)) {
			app.settings.set("drawInternals", !app.settings.getBool("drawInternals"));
		} else if (keyPressedOnce(VK_ALT, VK_G)) {
			app.settings.set("drawGrid", !app.settings.getBool("drawGrid"));
		} else if (keyPressedOnce(VK_ALT, VK_R)) {
			app.settings.set("drawRoute", !app.settings.getBool("drawRoute"));
		} else if (keyPressedOnce(VK_ALT, VK_L)) {
			lives += 1;
		} else if (keyPressedOnce(VK_ALT, VK_P)) {
			board.tilesWithContent(Pellet).forEach(tile -> board.setContent(tile, None));
		} else if (keyPressedOnce(VK_ALT, VK_E)) {
			board.tilesWithContent(Energizer).forEach(tile -> board.setContent(tile, None));
		} else if (keyPressedOnce(VK_ALT, VK_T)) {
			app.getThemeManager().selectNextTheme();
		} else if (keyPressedOnce(VK_ALT, VK_K)) {
			ghosts.forEach(Ghost::kill);
		}
	}

	private void nextLevel() {
		++level;
		ghostsEatenAtLevel = 0;
		board.resetContent();
		removeBonus();
		ghostAttackTimer.setLevel(level);
		ghostAttackTimer.init();
		Log.info(format("Level %d: %d pellets, %d energizers.", level, board.count(Pellet), board.count(Energizer)));
	}

	private void createPacManAndGhosts() {

		pacMan = new PacMan(app, board, () -> app.getThemeManager().getTheme());
		pacMan.init();

		pacMan.onContentFound = content -> {
			Tile tile = pacMan.currentTile();
			switch (content) {
			case Pellet:
				board.setContent(tile, None);
				score(POINTS_FOR_PELLET);
				pacMan.freeze(WAIT_TICKS_AFTER_PELLET_EATEN);
				app.assets.sound("sfx/eat-pill.mp3").play();
				break;
			case Energizer:
				board.setContent(tile, None);
				score(POINTS_FOR_ENERGIZER);
				nextGhostPoints = POINTS_FOR_KILLING_FIRST_GHOST;
				pacMan.freeze(WAIT_TICKS_AFTER_ENERGIZER_EATEN);
				pacMan.beginPowerWalking(model.getGhostFrightenedDuration(level));
				app.assets.sound("sfx/eat-pill.mp3").play();
				break;
			case Bonus:
				app.assets.sound("sfx/eat-fruit.mp3").play();
				int points = model.getBonusValue(level);
				score(points);
				bonusList.add(model.getBonusSymbol(level));
				showFlashText(points, tile.getCol() * TILE_SIZE, tile.getRow() * TILE_SIZE);
				removeBonus();
				break;
			default:
				break;
			}
		};

		pacMan.onEnemyContact = ghost -> {
			if (ghost.control.stateID() == GhostState.Dead || ghost.control.stateID() == GhostState.Recovering) {
				return;
			}
			if (pacMan.control.is(PacManState.PowerWalking)) {
				Log.info("Pac-Man kills " + ghost.getName());
				app.assets.sound("sfx/eat-ghost.mp3").play();
				score(nextGhostPoints);
				showFlashText(nextGhostPoints, ghost.tr.getX(), ghost.tr.getY());
				if (++ghostsEatenAtLevel == 16) {
					score(12000);
				}
				nextGhostPoints *= 2;
				ghost.kill();
			} else {
				Log.info(ghost.getName() + " kills Pac-Man.");
				--lives;
				playControl.feed(PlaySceneInput.PacManCrashed);
			}
		};

		pacMan.xOffset = () -> {
			int offset = 0;
			if (pacMan.control.is(PacManState.Initialized) && pacMan.currentTile().equals(PACMAN_HOME)) {
				offset = TILE_SIZE / 2;
			}
			return offset;
		};

		// Create the ghosts:

		ghosts = new HashSet<>();

		Ghost blinky = new Ghost(app, board, "Blinky", () -> app.getThemeManager().getTheme());
		blinky.setColor(Color.RED);
		ghosts.add(blinky);

		Ghost inky = new Ghost(app, board, "Inky", () -> app.getThemeManager().getTheme());
		inky.setColor(new Color(64, 224, 208));
		ghosts.add(inky);

		Ghost pinky = new Ghost(app, board, "Pinky", () -> app.getThemeManager().getTheme());
		pinky.setColor(Color.PINK);
		ghosts.add(pinky);

		Ghost clyde = new Ghost(app, board, "Clyde", () -> app.getThemeManager().getTheme());
		clyde.setColor(Color.ORANGE);
		ghosts.add(clyde);

		// Define common ghost properties and behavior:

		ghosts.forEach(ghost -> {

			ghost.setAnimated(false);

			// When in ghost house or at ghost house door, ghosts should appear half a tile to the right:
			ghost.xOffset = () -> {
				int offset = 0;
				if (ghost.insideGhostHouse()) {
					offset = TILE_SIZE / 2;
				} else if (ghost.currentTile().equals(GHOST_HOUSE_ENTRY)
						&& (ghost.control.stateID() == GhostState.Initialized || ghost.control.stateID() == GhostState.Waiting
								|| ghost.control.stateID() == GhostState.Recovering || ghost.control.stateID() == GhostState.Dead)) {
					offset = TILE_SIZE / 2;
				}
				return offset;
			};

			// Define which state the ghost should change to after recovering or frightened:
			ghost.restoreState = () -> {
				if (ghostAttackTimer.state() == GhostAttackState.Scattering) {
					ghost.beginScattering();
				} else if (ghostAttackTimer.state() == GhostAttackState.Chasing) {
					ghost.beginChasing();
				}
			};

			// Start waiting when notified:
			ghost.control.changeOnInput(GhostEvent.WaitingStarts, GhostState.Initialized, GhostState.Waiting, state -> {
				ghost.control.state(GhostState.Waiting).setDuration(model.getGhostWaitingDuration(ghost));
				ghost.setAnimated(true);
			});

			// While waiting, ghosts bounce. Afterwards, they return to the current attack state.
			ghost.control.state(GhostState.Waiting).update = state -> {
				if (state.isTerminated()) {
					ghost.restoreState.run();
				} else {
					ghost.bounce();
				}
			};

			// When Pac-Man gets empowered, become frightened for the same duration
			ghost.control.changeOnInput(GhostEvent.PacManAttackStarts, GhostState.Scattering, GhostState.Frightened,
					state -> ghost.control.state(GhostState.Frightened)
							.setDuration(app.motor.toFrames(model.getGhostFrightenedDuration(level))));

			// When in "frightened" state, ghosts move randomly:
			ghost.control.state(GhostState.Frightened).update = state -> {
				if (state.isTerminated()) {
					ghost.restoreState.run();
				} else {
					ghost.moveRandomly();
				}
			};

			// When "dead", ghosts return to their home tile, then they start recovering:
			ghost.control.state(GhostState.Dead).update = state -> {
				Tile homeTile = getGhostHomeTile(ghost);
				ghost.follow(homeTile);
				if (ghost.isExactlyOver(homeTile)) {
					ghost.beginRecovering();
				}
			};

			// After "recovering", ghosts switch to the current attack state:
			ghost.control.state(GhostState.Recovering).update = state -> {
				if (state.isTerminated()) {
					ghost.restoreState.run();
				}
			};
		});

		// --- "Blinky", the red ghost.

		// Blinky waits just before the ghost house looking to the west:
		blinky.control.state(GhostState.Waiting).entry = state -> {
			blinky.placeAt(BLINKY_HOME);
			blinky.setMoveDir(W);
			blinky.setAnimated(true);
		};

		// Blinky doesn't bounce while waiting:
		blinky.control.state(GhostState.Waiting).update = state -> {
			if (state.isTerminated()) {
				blinky.restoreState.run();
			}
		};

		// Blinky loops around the walls at the right upper corner of the maze:
		blinky.control.state(GhostState.Scattering, new LoopAroundWalls(blinky, RIGHT_UPPER_CORNER, S, true));

		// Blinky directly follows Pac-Man:
		blinky.control.state(GhostState.Chasing).update = state -> blinky.follow(pacMan.currentTile());

		/*
		 * "Inky", the blue ghost.
		 */

		// Inky waits inside the ghost house:
		inky.control.state(GhostState.Waiting).entry = state -> {
			inky.placeAt(INKY_HOME);
			inky.setMoveDir(N);
			inky.setAnimated(true);
		};

		// Inky loops around the walls at the right corner of the maze:
		inky.control.state(GhostState.Scattering, new LoopAroundWalls(inky, RIGHT_LOWER_CORNER, W, true));

		// Inky chases together with Blinky.
		inky.control.state(GhostState.Chasing, new ChaseWithPartner(inky, blinky, pacMan));

		/*
		 * "Pinky", the pink ghost.
		 */

		// Pinky waits inside the ghost house:
		pinky.control.state(GhostState.Waiting).entry = state -> {
			pinky.placeAt(PINKY_HOME);
			pinky.setMoveDir(S);
			pinky.setAnimated(true);
		};

		// Pinky loops around the walls at the left upper corner of the maze:
		pinky.control.state(GhostState.Scattering, new LoopAroundWalls(pinky, LEFT_UPPER_CORNER, S, false));

		// Pinky follows the position 4 tiles ahead of Pac-Man:
		pinky.control.state(GhostState.Chasing, new AmbushPacMan(pinky, pacMan, 4));

		/*
		 * "Clyde", the yellow ghost.
		 */

		// Clyde waits inside ghost house:
		clyde.control.state(GhostState.Waiting).entry = state -> {
			clyde.placeAt(CLYDE_HOME);
			clyde.setMoveDir(N);
			clyde.setAnimated(true);
		};

		// Clyde loops around the walls at the left lower corner of the maze:
		clyde.control.state(GhostState.Scattering, new LoopAroundWalls(clyde, LEFT_LOWER_CORNER, E, false));

		// Clyde follows Pac-Man's position if he is more than 8 tiles away, otherwise he moves
		// randomly:
		clyde.control.state(GhostState.Chasing).update = state -> {
			if (clyde.insideGhostHouse()) {
				clyde.follow(GHOST_HOUSE_ENTRY);
			} else if (clyde.currentTile().distance(pacMan.currentTile()) > 8) {
				clyde.follow(pacMan.currentTile());
			} else {
				clyde.moveRandomly();
			}
		};

		app.entities.removeAll(PacMan.class);
		app.entities.removeAll(Ghost.class);
		app.entities.add(pacMan, blinky, inky, pinky, clyde);

		pacMan.enemies().clear();
		ghosts.forEach(ghost -> pacMan.enemies().add(ghost));

		ghostAttackTimer = new GhostAttackTimer(app, ghosts, SCATTERING_TIMES, CHASING_TIMES);
	}

	private Tile getGhostHomeTile(Ghost ghost) {
		switch (ghost.getName()) {
		case "Blinky":
			return BLINKY_HOME;
		case "Pinky":
			return PINKY_HOME;
		case "Inky":
			return INKY_HOME;
		case "Clyde":
			return CLYDE_HOME;
		default:
			return GHOST_HOUSE_ENTRY;
		}
	}

	private void score(int points) {
		int newScore = score + points;
		// check for extra life
		if (score < SCORE_FOR_EXTRALIFE && newScore >= SCORE_FOR_EXTRALIFE) {
			app.assets.sound("sfx/extra-life.mp3").play();
			++lives;
		}
		score = newScore;
		// check for bonus
		long pelletsLeft = board.count(Pellet);
		if (pelletsLeft == BONUS1_PELLETS_LEFT || pelletsLeft == BONUS2_PELLETS_LEFT) {
			board.setContent(BONUS_TILE, Bonus);
			bonusTimeRemaining = app.motor.toFrames(9);
		}
	}

	private void checkHighscore() {
		if (score > highscore.getPoints()) {
			highscore.save(score, level);
			highscore.load();
		}
	}

	private void removeBonus() {
		board.setContent(BONUS_TILE, None);
		bonusTimeRemaining = 0;
	}

	private void showFlashText(Object object, float x, float y) {
		if (x > getWidth() - 3 * TILE_SIZE) {
			x -= 3 * TILE_SIZE;
		}
		FlashText.show(app, String.valueOf(object),
				app.getThemeManager().getTheme().getTextFont().deriveFont(Font.PLAIN, SPRITE_SIZE), Color.YELLOW,
				app.motor.toFrames(1), new Vector2(x, y), new Vector2(0, -0.2f));
	}

	@Override
	public void draw(Graphics2D pen) {

		final PacManTheme theme = app.getThemeManager().getTheme();

		// Board & content
		drawSprite(pen, 3, 0, theme.getBoardSprite());
		int firstMazeRow = 4;
		int lastMazeRow = board.numRows - 3;
		range(firstMazeRow, lastMazeRow).forEach(row -> range(0, board.numCols).forEach(col -> {
			TileContent content = board.getContent(row, col);
			switch (content) {
			case Pellet:
				drawSprite(pen, row, col, theme.getPelletSprite());
				break;
			case Energizer:
				drawSprite(pen, row, col, theme.getEnergizerSprite());
				break;
			case Bonus:
				BonusSymbol symbol = model.getBonusSymbol(level);
				drawSprite(pen, row - .5f, col, theme.getBonusSprite(symbol));
				break;
			default:
				break;
			}
		}));

		// Entities
		pacMan.draw(pen);
		if (!playControl.is(PlayState.Crashing)) {
			ghosts.forEach(ghost -> ghost.draw(pen));
		}

		// HUD
		pen.setColor(Color.LIGHT_GRAY);
		pen.setFont(theme.getTextFont());
		drawText(pen, 1, 1, "SCORE");
		drawText(pen, 1, 8, "HIGH");
		drawText(pen, 1, 12, "SCORE");
		pen.setColor(theme.getHUDColor());
		drawText(pen, 2, 1, format("%02d", score));
		drawText(pen, 2, 8, format("%02d   L%d", highscore.getPoints(), highscore.getLevel()));
		drawText(pen, 2, 20, "Level " + level);

		// Status messages
		switch (playControl.stateID()) {
		case Ready:
			pen.setColor(Color.RED);
			drawTextCentered(pen, getWidth(), 9.5f, "Press SPACE to start");
			pen.setColor(theme.getHUDColor());
			drawTextCentered(pen, getWidth(), 21f, "Ready!");
			break;
		case StartingLevel:
			pen.setColor(theme.getHUDColor());
			drawTextCentered(pen, getWidth(), 21f, "Level " + level);
			break;
		case GameOver:
			pen.setColor(Color.RED);
			drawTextCentered(pen, getWidth(), 9.5f, "Press SPACE for new game");
			pen.setColor(theme.getHUDColor());
			drawTextCentered(pen, getWidth(), 21f, "Game Over!");
			break;
		default:
			break;
		}

		// Lives
		range(0, lives).forEach(i -> drawSprite(pen, board.numRows - 2, 2 * (i + 1), theme.getLifeSprite()));

		// Boni
		int col = board.numCols - 2;
		for (BonusSymbol bonus : bonusList) {
			drawSprite(pen, board.numRows - 2, col, theme.getBonusSprite(bonus));
			col -= 2;
		}

		// Grid lines
		if (app.settings.getBool("drawGrid")) {
			drawGridLines(pen, getWidth(), getHeight());
		}

		// Internals
		if (app.settings.getBool("drawInternals")) {
			// play state and ghost attack state
			drawTextCentered(pen, getWidth(), 33, playControl.stateID() + "  " + ghostAttackTimer.state());
			// mark home positions of ghosts
			ghosts.forEach(ghost -> {
				pen.setColor(ghost.getColor());
				Tile homeTile = getGhostHomeTile(ghost);
				pen.fillRect(homeTile.getCol() * TILE_SIZE + TILE_SIZE / 2, homeTile.getRow() * TILE_SIZE + TILE_SIZE / 2,
						TILE_SIZE, TILE_SIZE);
			});
		}

		// Flash texts
		app.entities.allOf(FlashText.class).forEach(text -> text.draw(pen));
	}
}