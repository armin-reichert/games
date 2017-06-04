package de.amr.games.pacman.play;

import static de.amr.easy.game.Application.Log;
import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Bonus;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.GhostHouse;
import static de.amr.games.pacman.core.board.TileContent.None;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.board.TileContent.Tunnel;
import static de.amr.games.pacman.misc.SceneHelper.drawGridLines;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.misc.SceneHelper.drawText;
import static de.amr.games.pacman.misc.SceneHelper.drawTextCentered;
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
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

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
import de.amr.games.pacman.core.entities.ghost.behaviors.ChaseWithPartner;
import de.amr.games.pacman.core.entities.ghost.behaviors.FollowTileAheadOfPacMan;
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

	// Game parameters
	public static final int POINTS_FOR_PELLET = 10;
	public static final int POINTS_FOR_ENERGIZER = 50;
	public static final int BONUS1_PELLETS_LEFT = 170;
	public static final int BONUS2_PELLETS_LEFT = 70;
	public static final int SCORE_FOR_EXTRALIFE = 10000;
	public static final int POINTS_FOR_KILLING_FIRST_GHOST = 200;
	public static final int WAIT_TICKS_AFTER_PELLET_EATEN = 1;
	public static final int WAIT_TICKS_AFTER_ENERGIZER_EATEN = 3;

	// Game control
	private final StateMachine<PlayState> playControl;
	private GhostAttackTimer ghostAttackTimer;
	private final Random rand = new Random();

	// Entities
	private PacMan pacMan;
	private Ghost blinky, inky, pinky, clyde;

	private Stream<Ghost> ghosts() {
		return Stream.of(blinky, inky, pinky, clyde);
	}

	// Scene-specific data
	private final LevelData levels;
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
	private class PlayControl extends StateMachine<PlayState> {

		public PlayControl() {

			super("Play control", new EnumMap<>(PlayState.class));

			// Initializing

			state(Initializing).entry = state -> {
				level = 0;
				lives = 3;
				score = 0;
				bonusList.clear();
				createPacManAndGhosts();
				nextLevel();
				app.getThemeManager().getTheme().getEnergizerSprite().setAnimated(false);
				app.assets.sound("sfx/insert-coin.mp3").play();

				// Tracing
				setLogger(Log, app.motor.getFrequency());
				ghostAttackTimer.setLogger(Log);
				pacMan.setLogger(Log);
				ghosts().forEach(ghost -> ghost.setLogger(Log));
			};

			state(Initializing).update = state -> {
				if (!app.assets.sound("sfx/insert-coin.mp3").isRunning()) {
					changeTo(Ready);
				}
			};

			// Ready

			state(Ready).entry = state -> {
				app.getThemeManager().getTheme().getEnergizerSprite().setAnimated(true);
				ghosts().forEach(ghost -> {
					ghost.setAnimated(true);
					ghost.speed = () -> getGhostSpeed(ghost);
				});
			};

			state(Ready).update = state -> {
				if (Keyboard.keyPressedOnce(VK_SPACE)) {
					changeTo(StartingLevel);
				}
			};

			// StartingLevel

			state(StartingLevel).entry = state -> {
				app.assets.sound("sfx/ready.mp3").play();
				ghosts().forEach(Ghost::beginWaiting);
			};

			state(StartingLevel).update = state -> {
				app.entities.all().forEach(GameEntity::update);
				if (!app.assets.sound("sfx/ready.mp3").isRunning()) {
					changeTo(Playing);
				}
			};

			// Playing

			state(Playing).entry = state -> {
				pacMan.init();
				pacMan.placeAt(PACMAN_HOME);
				pacMan.speed = () -> pacMan.state() == PacManState.PowerWalking ? levels.getPacManPowerWalkingSpeed(level)
						: levels.getPacManSpeed(level);

				ghosts().forEach(ghost -> {
					ghost.init();
					ghost.speed = () -> getGhostSpeed(ghost);
					ghost.placeAt(getGhostHomeTile(ghost));
					ghost.setWaitingTime(getGhostWaitingDuration(ghost));
					ghost.beginWaiting();
				});

				app.getThemeManager().getTheme().getEnergizerSprite().setAnimated(true);
				pacMan.beginWalking();
				ghostAttackTimer.start();
			};

			state(Playing).update = state -> {
				ghostAttackTimer.update();
				app.entities.all().forEach(GameEntity::update);
				if (isBonusEnabled() && bonusTimeRemaining-- == 0) {
					setBonusEnabled(false);
				}
				if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
					nextLevel();
					changeTo(StartingLevel);
				}
			};

			state(Playing).exit = state -> {
				app.entities.removeAll(FlashText.class);
			};

			// Crashing

			state(Crashing).entry = state -> {
				app.assets.sounds().forEach(Sound::stop);
				app.assets.sound("sfx/die.mp3").play();
				app.getThemeManager().getTheme().getEnergizerSprite().setAnimated(false);
				setBonusEnabled(false);
				pacMan.killed();
				Log.info("PacMan killed, lives remaining: " + lives);
			};

			state(Crashing).update = state -> {
				if (!app.assets.sound("sfx/die.mp3").isRunning()) {
					changeTo(lives > 0 ? Playing : GameOver);
				}
			};

			// GameOver

			state(GameOver).entry = state -> {
				if (score > highscore.getPoints()) {
					highscore.save(score, level);
					highscore.load();
				}
				app.entities.all().forEach(entity -> entity.setAnimated(false));
				Log.info("Game over.");
			};

			state(GameOver).update = state -> {
				if (Keyboard.keyPressedOnce(VK_SPACE)) {
					changeTo(Initializing);
				}
			};

			state(GameOver).exit = state -> {
				app.entities.removeAll(GameEntity.class);
			};
		}
	}

	public PlayScene(PacManGame app) {
		super(app);
		playControl = new PlayControl();
		levels = new LevelData(8 * TILE_SIZE / app.motor.getFrequency());
		board = new Board(app.assets.text("board.txt").split("\n"));
		highscore = new Highscore("pacman-hiscore.txt");
		bonusList = new ArrayList<>();
	}

	@Override
	public void init() {
		playControl.changeTo(Initializing);
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
			ghosts().forEach(Ghost::killed);
		}
	}

	private void nextLevel() {
		++level;
		ghostsEatenAtLevel = 0;
		board.resetContent();
		setBonusEnabled(false);
		ghostAttackTimer.setLevel(level);
		ghostAttackTimer.init();
		app.entities.all().forEach(GameEntity::init);
		pacMan.placeAt(PACMAN_HOME);
		Log.info(format("Level %d initialized: %d pellets and %d energizers.", level, board.count(Pellet),
				board.count(Energizer)));
	}

	private void createPacManAndGhosts() {

		pacMan = new PacMan(app, board);
		pacMan.theme = () -> app.getThemeManager().getTheme();

		pacMan.onContentFound = content -> {
			Tile tile = pacMan.currentTile();
			switch (content) {
			case Pellet:
				app.assets.sound("sfx/eat-pill.mp3").play();
				score(POINTS_FOR_PELLET);
				board.setContent(tile, None);
				if (board.count(Pellet) == BONUS1_PELLETS_LEFT || board.count(Pellet) == BONUS2_PELLETS_LEFT) {
					setBonusEnabled(true);
				}
				pacMan.freeze(WAIT_TICKS_AFTER_PELLET_EATEN);
				break;
			case Energizer:
				app.assets.sound("sfx/eat-pill.mp3").play();
				nextGhostPoints = POINTS_FOR_KILLING_FIRST_GHOST;
				score(POINTS_FOR_ENERGIZER);
				board.setContent(tile, None);
				if (board.count(Pellet) == BONUS1_PELLETS_LEFT || board.count(Pellet) == BONUS2_PELLETS_LEFT) {
					setBonusEnabled(true);
				}
				pacMan.freeze(WAIT_TICKS_AFTER_ENERGIZER_EATEN);
				pacMan.beginPowerWalking(levels.getGhostFrightenedDuration(level));
				break;
			case Bonus:
				app.assets.sound("sfx/eat-fruit.mp3").play();
				int points = levels.getBonusValue(level);
				score(points);
				bonusList.add(levels.getBonusSymbol(level));
				showFlashText(points, tile.getCol() * TILE_SIZE, tile.getRow() * TILE_SIZE);
				setBonusEnabled(false);
				break;
			default:
				break;
			}
		};

		pacMan.onEnemyContact = ghost -> {
			if (ghost.state() == GhostState.Dead || ghost.state() == GhostState.Recovering) {
				return;
			}
			if (pacMan.state() == PacManState.PowerWalking) {
				Log.info("Pac-Man kills " + ghost.getName());
				app.assets.sound("sfx/eat-ghost.mp3").play();
				score(nextGhostPoints);
				showFlashText(nextGhostPoints, ghost.tr.getX(), ghost.tr.getY());
				if (++ghostsEatenAtLevel == 16) {
					score(12000);
				}
				nextGhostPoints *= 2;
				ghost.killed();
			} else {
				Log.info(ghost.getName() + " kills Pac-Man.");
				--lives;
				playControl.changeTo(PlayState.Crashing);
			}
		};

		pacMan.xOffset = () -> {
			int offset = 0;
			if (pacMan.state() == PacManState.Initialized && pacMan.currentTile().equals(PACMAN_HOME)) {
				offset = TILE_SIZE / 2;
			}
			return offset;
		};

		// Create the ghosts:

		blinky = new Ghost(app, board, "Blinky");
		blinky.theme = () -> app.getThemeManager().getTheme();
		blinky.setColor(Color.RED);

		inky = new Ghost(app, board, "Inky");
		inky.theme = () -> app.getThemeManager().getTheme();
		inky.setColor(new Color(64, 224, 208));

		pinky = new Ghost(app, board, "Pinky");
		pinky.theme = () -> app.getThemeManager().getTheme();
		pinky.setColor(Color.PINK);

		clyde = new Ghost(app, board, "Clyde");
		clyde.theme = () -> app.getThemeManager().getTheme();
		clyde.setColor(Color.ORANGE);

		// Define common ghost behavior:

		ghosts().forEach(ghost -> {

			// When in ghost house or at ghost house door, render half a tile to the right:
			ghost.xOffset = () -> {
				int offset = 0;
				Tile current = ghost.currentTile();
				if (board.contains(current, GhostHouse)) {
					offset = TILE_SIZE / 2;
				}
				if (current.equals(GHOST_HOUSE_ENTRY)
						&& (ghost.state() == GhostState.Initialized || ghost.state() == GhostState.Waiting
								|| ghost.state() == GhostState.Recovering || ghost.state() == GhostState.Dead)) {
					offset = TILE_SIZE / 2;
				}
				return offset;
			};

			// While waiting, bounce. After waiting, return to state given by attack control.
			ghost.state(GhostState.Waiting).update = state -> {
				if (!state.isTerminated()) {
					ghost.bounce();
					return;
				}
				if (pacMan.state() == PacManState.PowerWalking) {
					ghost.beginBeingFrightened(pacMan.getRemainingTime());
				}
				switch (ghostAttackTimer.state()) {
				case Scattering:
					ghost.beginScattering();
					break;
				case Chasing:
					ghost.beginChasing();
					break;
				default:
					break;
				}
			};

			// What state to return to after frightening ends:
			ghost.stateToRestore = () -> {
				switch (ghostAttackTimer.state()) {
				case Scattering:
					return GhostState.Scattering;
				case Chasing:
					return GhostState.Chasing;
				default:
					return ghost.state();
				}
			};

			// When in "frightened" state, move randomly:
			ghost.state(GhostState.Frightened).update = state -> {
				ghost.moveRandomly();
			};

			// When "dead", return to home location. Then recover:
			ghost.state(GhostState.Dead).update = state -> {
				Tile homeTile = getGhostHomeTile(ghost);
				ghost.follow(homeTile);
				if (ghost.getRow() == homeTile.getRow() && ghost.getCol() == homeTile.getCol()) {
					ghost.adjust();
					ghost.beginRecovering();
				}
			};

			// After "recovering", start scattering or chasing:
			ghost.state(GhostState.Recovering).entry = state -> {
				state.setDuration(getGhostRecoveringDuration(ghost));
			};

			ghost.state(GhostState.Recovering).update = state -> {
				if (state.isTerminated()) {
					ghost.setAnimated(true);
					ghost.restoreState();
				}
			};
		});

		/*
		 * "Blinky", the red ghost.
		 */

		// Blinky waits just before ghost house:
		blinky.state(GhostState.Waiting).entry = state -> {
			blinky.placeAt(BLINKY_HOME);
			blinky.setMoveDir(W);
			blinky.setAnimated(true);
		};

		// Blinky doesn't bounce while waiting. Then he acts as given by attack control.
		blinky.state(GhostState.Waiting).update = state -> {
			if (!state.isTerminated()) {
				return;
			}
			switch (ghostAttackTimer.state()) {
			case Scattering:
				blinky.beginScattering();
				break;
			case Chasing:
				blinky.beginChasing();
				break;
			default:
				break;
			}
		};

		// Blinky loops around the walls at the right upper corner of the maze:
		blinky.state(GhostState.Scattering, new LoopAroundWalls(blinky, 4, 26, S, true));

		// Blinky directly follows Pac-Man:
		blinky.state(GhostState.Chasing).update = state -> {
			blinky.follow(pacMan.currentTile());
		};

		/*
		 * "Inky", the blue ghost.
		 */

		// Inky waits inside the ghost house:
		inky.state(GhostState.Waiting).entry = state -> {
			inky.placeAt(INKY_HOME);
			inky.setMoveDir(N);
			inky.setAnimated(true);
		};

		// Inky loops around the walls at the lower right corner of the maze:
		inky.state(GhostState.Scattering, new LoopAroundWalls(inky, 32, 26, W, true));

		// Inky chases together with Blinky.
		inky.state(GhostState.Chasing, new ChaseWithPartner(inky, blinky, pacMan));

		/*
		 * "Pinky", the pink ghost.
		 */

		// Pinky waits inside the ghost house:
		pinky.state(GhostState.Waiting).entry = state -> {
			pinky.placeAt(PINKY_HOME);
			pinky.setMoveDir(S);
			pinky.setAnimated(true);
		};

		// Pinky loops around the walls at the upper right corner of the maze:
		pinky.state(GhostState.Scattering, new LoopAroundWalls(pinky, 4, 1, S, false));

		// Pinky follows the position 4 tiles ahead of Pac-Man:
		pinky.state(GhostState.Chasing, new FollowTileAheadOfPacMan(pinky, pacMan, 4));

		/*
		 * "Clyde", the yellow ghost.
		 */

		// Clyde waits inside ghost house:
		clyde.state(GhostState.Waiting).entry = state -> {
			clyde.placeAt(CLYDE_HOME);
			clyde.setMoveDir(N);
			clyde.setAnimated(true);
		};

		// Clyde loops around the walls at the left lower corner of the maze:
		clyde.state(GhostState.Scattering, new LoopAroundWalls(clyde, 32, 1, E, false));

		// Clyde follows Pac-Man's position if he is more than 8 tiles away, otherwise he moves
		// randomly:
		clyde.state(GhostState.Chasing).update = state -> {
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
		ghosts().forEach(ghost -> pacMan.enemies().add(ghost));

		ghostAttackTimer = new GhostAttackTimer(app, blinky, inky, pinky, clyde);
	}

	private float getGhostSpeed(Ghost ghost) {
		TileContent content = board.getContent(ghost.currentTile());
		if (content == Tunnel) {
			return levels.getGhostSpeedInTunnel(level);
		} else if (content == GhostHouse) {
			return levels.getGhostSpeedInHouse();
		} else if (ghost.state() == GhostState.Frightened) {
			return levels.getGhostSpeedWhenFrightened(level);
		} else {
			return levels.getGhostSpeedNormal(level);
		}
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

	private int getGhostWaitingDuration(Ghost ghost) {
		if ("Blinky".equals(ghost.getName())) {
			return 0;
		}
		return app.motor.toFrames(2 + rand.nextInt(3));
	}

	private int getGhostRecoveringDuration(Ghost ghost) {
		return app.motor.toFrames(1 + rand.nextInt(2));
	}

	private void score(int points) {
		int newScore = score + points;
		if (score < SCORE_FOR_EXTRALIFE && newScore >= SCORE_FOR_EXTRALIFE) {
			app.assets.sound("sfx/extra-life.mp3").play();
			++lives;
		}
		score = newScore;
	}

	private boolean isBonusEnabled() {
		return bonusTimeRemaining > 0;
	}

	private void setBonusEnabled(boolean enabled) {
		if (enabled) {
			board.setContent(BONUS_TILE, Bonus);
			bonusTimeRemaining = app.motor.toFrames(9);
		} else {
			board.setContent(BONUS_TILE, None);
			bonusTimeRemaining = 0;
		}
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
	public void draw(Graphics2D g) {

		final PacManTheme theme = app.getThemeManager().getTheme();

		// Board & content
		drawSprite(g, 3, 0, theme.getBoardSprite());
		range(4, board.numRows - 3).forEach(row -> range(0, board.numCols).forEach(col -> {
			if (board.contains(row, col, Pellet)) {
				drawSprite(g, row, col, theme.getPelletSprite());
			} else if (board.contains(row, col, Energizer)) {
				drawSprite(g, row, col, theme.getEnergizerSprite());
			} else if (board.contains(row, col, Bonus)) {
				BonusSymbol symbol = levels.getBonusSymbol(level);
				drawSprite(g, row - .5f, col, theme.getBonusSprite(symbol));
			}
		}));

		// Entities
		pacMan.draw(g);
		if (!playControl.inState(PlayState.Crashing)) {
			ghosts().forEach(ghost -> ghost.draw(g));
		}

		// HUD
		g.setColor(Color.LIGHT_GRAY);
		g.setFont(theme.getTextFont());
		drawText(g, 1, 1, "SCORE");
		drawText(g, 1, 8, "HIGH");
		drawText(g, 1, 12, "SCORE");
		g.setColor(theme.getHUDColor());
		drawText(g, 2, 1, format("%02d", score));
		drawText(g, 2, 8, format("%02d   L%d", highscore.getPoints(), highscore.getLevel()));
		drawText(g, 2, 20, "Level " + level);

		// Status messages
		switch (playControl.stateID()) {
		case Ready:
			g.setColor(Color.RED);
			drawTextCentered(g, getWidth(), 9.5f, "Press SPACE to start");
			g.setColor(theme.getHUDColor());
			drawTextCentered(g, getWidth(), 21f, "Ready!");
			break;
		case StartingLevel:
			g.setColor(theme.getHUDColor());
			drawTextCentered(g, getWidth(), 21f, "Level " + level);
			break;
		case GameOver:
			g.setColor(Color.RED);
			drawTextCentered(g, getWidth(), 9.5f, "Press SPACE for new game");
			g.setColor(theme.getHUDColor());
			drawTextCentered(g, getWidth(), 21f, "Game Over!");
			break;
		default:
			break;
		}

		// Lives
		range(0, lives).forEach(i -> drawSprite(g, board.numRows - 2, 2 * (i + 1), theme.getLifeSprite()));

		// Boni
		int col = board.numCols - 2;
		for (BonusSymbol bonus : bonusList) {
			drawSprite(g, board.numRows - 2, col, theme.getBonusSprite(bonus));
			col -= 2;
		}

		// Grid lines
		if (app.settings.getBool("drawGrid")) {
			drawGridLines(g, getWidth(), getHeight());
		}

		// Internals
		if (app.settings.getBool("drawInternals")) {
			// play state
			drawTextCentered(g, getWidth(), 33, playControl.stateID() + "  " + ghostAttackTimer.state());
			// home positions of ghosts
			ghosts().forEach(ghost -> {
				g.setColor(ghost.getColor());
				Tile homeTile = getGhostHomeTile(ghost);
				g.fillRect(homeTile.getCol() * TILE_SIZE + TILE_SIZE / 2, homeTile.getRow() * TILE_SIZE + TILE_SIZE / 2,
						TILE_SIZE, TILE_SIZE);
			});
		}

		// Flash texts
		app.entities.allOf(FlashText.class).forEach(text -> text.draw(g));
	}
}