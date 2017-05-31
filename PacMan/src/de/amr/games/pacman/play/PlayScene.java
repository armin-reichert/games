package de.amr.games.pacman.play;

import static de.amr.easy.game.Application.Log;
import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static de.amr.games.pacman.core.board.TileContent.Bonus;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.GhostHouse;
import static de.amr.games.pacman.core.board.TileContent.None;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.board.TileContent.Tunnel;
import static de.amr.games.pacman.core.entities.PacManState.Frightening;
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
import de.amr.easy.grid.impl.Top4;
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
	public static final Tile PACMAN_HOME = new Tile(26, 13.5f);
	public static final Tile BLINKY_HOME = new Tile(14, 13.f);
	public static final Tile INKY_HOME = new Tile(17f, 11f);
	public static final Tile PINKY_HOME = new Tile(17f, 13f);
	public static final Tile CLYDE_HOME = new Tile(17f, 15f);
	public static final Tile GHOST_HOUSE_ENTRY = new Tile(14, 13);
	public static final Tile BONUS_TILE = new Tile(20f, 13f);

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
	private final GhostAttackTimer ghostAttackTimer;
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
				lives = 3;
				score = 0;
				bonusList.clear();
				createPacManAndGhosts();
				level = 1;
				resetLevel();
				app.getTheme().getEnergizerSprite().setAnimated(false);
				app.assets.sound("sfx/insert-coin.mp3").play();
			};

			state(Initializing).update = state -> {
				if (!app.assets.sound("sfx/insert-coin.mp3").isRunning()) {
					changeTo(Ready);
				}
			};

			// Ready

			state(Ready).entry = state -> {
				app.getTheme().getEnergizerSprite().setAnimated(true);
			};

			state(Ready).update = state -> {
				if (Keyboard.keyPressedOnce(VK_SPACE)) {
					changeTo(StartingLevel);
				}
			};

			// StartingLevel

			state(StartingLevel).entry = state -> {
				ghostAttackTimer.setLevel(level);
				app.assets.sound("sfx/ready.mp3").play();
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
				ghosts().forEach(ghost -> {
					ghost.init();
					ghost.placeAt(getGhostHomeTile(ghost));
					ghost.setAnimated(true);
				});
				ghostAttackTimer.setLevel(level);
				app.getTheme().getEnergizerSprite().setAnimated(true);
				pacMan.control.changeTo(PacManState.Eating);
				ghosts().forEach(ghost -> ghost.setWaitingTime(getGhostWaitingDuration(ghost)));
				ghostAttackTimer.start();
			};

			state(Playing).update = state -> {
				ghostAttackTimer.update();
				app.entities.all().forEach(GameEntity::update);
				if (isBonusEnabled() && bonusTimeRemaining-- == 0) {
					setBonusEnabled(false);
				}
				if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
					++level;
					resetLevel();
					ghostAttackTimer.setLevel(level);
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
				app.getTheme().getEnergizerSprite().setAnimated(false);
				setBonusEnabled(false);
				pacMan.control.changeTo(PacManState.Dying);
				Log.info("PacMan died, lives remaining: " + lives);
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
		ghostAttackTimer = new GhostAttackTimer(app);
		configureGhostAttackTimer(app);
		levels = new LevelData(8 * TILE_SIZE / app.motor.getFrequency());
		board = new Board(app.assets.text("board.txt").split("\n"));
		highscore = new Highscore("pacman-hiscore.txt");
		bonusList = new ArrayList<>();
	}

	private void configureGhostAttackTimer(PacManGame app) {
		ghostAttackTimer.trace = true;
		ghostAttackTimer.onPhaseStart = phase -> {
			if (phase == GhostAttackState.Initialized) {
				ghosts().forEach(ghost -> {
					ghost.control.changeTo(GhostState.Waiting);
				});
			} else if (phase == GhostAttackState.Scattering) {
				ghosts().forEach(Ghost::beginScattering);
				app.assets.sound("sfx/siren.mp3").loop();
			} else if (phase == GhostAttackState.Chasing) {
				app.assets.sound("sfx/siren.mp3").loop();
				ghosts().forEach(Ghost::beginChasing);
			}
		};
		ghostAttackTimer.onPhaseEnd = phase -> {
			if (phase == GhostAttackState.Scattering) {
				app.assets.sound("sfx/siren.mp3").stop();
			} else if (phase == GhostAttackState.Chasing) {
				app.assets.sound("sfx/siren.mp3").stop();
			}
		};
	}

	@Override
	public void init() {
		playControl.changeTo(PlayState.Initializing);
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
			app.selectNextTheme();
		} else if (keyPressedOnce(VK_ALT, VK_K)) {
			ghosts().forEach(Ghost::killed);
		}
	}

	private void resetLevel() {
		ghostsEatenAtLevel = 0;
		board.resetContent();
		setBonusEnabled(false);
		app.entities.all().forEach(GameEntity::init);
		ghostAttackTimer.setLevel(level);
		Log.info(format("Level %d initialized: %d pellets and %d energizers.", level, board.count(Pellet),
				board.count(Energizer)));
	}

	private void createPacManAndGhosts() {

		pacMan = new PacMan(app, board);
		pacMan.placeAt(PACMAN_HOME);

		pacMan.speed = () -> {
			switch (pacMan.control.stateID()) {
			case Frightening:
				return levels.getPacManFrighteningSpeed(level);
			default:
				return levels.getPacManSpeed(level);
			}
		};

		pacMan.onPelletFound = tile -> {
			app.assets.sound("sfx/eat-pill.mp3").play();
			score(POINTS_FOR_PELLET);
			long pelletCount = board.count(Pellet);
			if (pelletCount == BONUS1_PELLETS_LEFT || pelletCount == BONUS2_PELLETS_LEFT) {
				setBonusEnabled(true);
			}
			pacMan.freeze(WAIT_TICKS_AFTER_PELLET_EATEN);
			board.setContent(tile, None);
		};

		pacMan.onEnergizerFound = tile -> {
			app.assets.sound("sfx/eat-pill.mp3").play();
			score(POINTS_FOR_ENERGIZER);
			nextGhostPoints = POINTS_FOR_KILLING_FIRST_GHOST;
			pacMan.freeze(WAIT_TICKS_AFTER_ENERGIZER_EATEN);
			int seconds = levels.getGhostFrightenedDuration(level);
			pacMan.control.state(Frightening).setDuration(app.motor.toFrames(seconds));
			pacMan.control.changeTo(Frightening);
			board.setContent(tile, None);
		};

		pacMan.onBonusFound = tile -> {
			app.assets.sound("sfx/eat-fruit.mp3").play();
			int points = levels.getBonusValue(level);
			score(points);
			bonusList.add(levels.getBonusSymbol(level));
			showFlashText(points, tile.getCol() * TILE_SIZE, tile.getRow() * TILE_SIZE);
			setBonusEnabled(false);
			board.setContent(tile, None);
		};

		pacMan.onGhostMet = ghost -> {
			if (ghost.control.inState(GhostState.Dead, GhostState.Waiting, GhostState.Recovering)) {
				return;
			}
			if (pacMan.control.inState(PacManState.Frightening)) {
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

		// Create the ghosts:

		blinky = new Ghost(app, board, "Blinky");
		blinky.setColor(Color.RED);

		inky = new Ghost(app, board, "Inky");
		inky.setColor(new Color(64, 224, 208));

		pinky = new Ghost(app, board, "Pinky");
		pinky.setColor(Color.PINK);

		clyde = new Ghost(app, board, "Clyde");
		clyde.setColor(Color.ORANGE);

		// Define common ghost behavior:

		ghosts().forEach(ghost -> {

			ghost.speed = () -> getGhostSpeed(ghost);

			ghost.control.state(GhostState.Waiting).update = state -> {
				if (!state.isTerminated()) {
					return;
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

			// When "frightened" state ends, go into chasing or scattering state:
			ghost.stateToRestore = () -> {
				switch (ghostAttackTimer.state()) {
				case Scattering:
					return GhostState.Scattering;
				case Chasing:
					return GhostState.Chasing;
				default:
					return ghost.control.stateID();
				}
			};

			// When in "frightened" state, move randomly:
			ghost.control.state(GhostState.Frightened).update = state -> {
				ghost.moveRandomly();
			};

			// When "dead", return to home location. Then recover.
			ghost.control.state(GhostState.Dead).update = state -> {
				Tile homeTile = getGhostHomeTile(ghost);
				ghost.follow(homeTile);
				if (ghost.getRow() == homeTile.getRow() && ghost.getCol() == homeTile.getCol()) {
					ghost.adjustOnTile();
					ghost.control.changeTo(GhostState.Recovering);
				}
			};

			// After "recovering", start scattering or chasing:
			ghost.control.state(GhostState.Recovering).entry = state -> {
				state.setDuration(getGhostRecoveringDuration(ghost));
			};

			ghost.control.state(GhostState.Recovering).update = state -> {
				if (state.isTerminated()) {
					ghost.setAnimated(true);
					ghost.control.changeTo(ghost.stateToRestore.get());
				}
			};
		});

		/*
		 * "Blinky", the red ghost.
		 */

		// Blinky waits just before ghost house:
		blinky.control.state(GhostState.Waiting).entry = state -> {
			blinky.placeAt(BLINKY_HOME);
			blinky.setMoveDir(Top4.W);
			blinky.setAnimated(true);
		};

		// Blinky loops around the walls at the right upper corner of the maze:
		blinky.control.state(GhostState.Scattering, new LoopAroundWalls(blinky, 4, 26, Top4.S, true));

		// Blinky directly follows Pac-Man:
		blinky.control.state(GhostState.Chasing).update = state -> {
			blinky.follow(pacMan.currentTile());
		};

		/*
		 * "Inky", the blue ghost.
		 */

		// Inky waits inside the ghost house:
		inky.control.state(GhostState.Waiting).entry = state -> {
			inky.placeAt(INKY_HOME);
			inky.setMoveDir(Top4.N);
			inky.setAnimated(true);
		};

		// Inky bounces while waiting. When waiting is over, he starts scattering or chasing.
		inky.control.state(GhostState.Waiting).update = state -> {
			if (!state.isTerminated()) {
				inky.bounce();
				return;
			}
			switch (ghostAttackTimer.state()) {
			case Scattering:
				inky.beginScattering();
				break;
			case Chasing:
				inky.beginChasing();
				break;
			default:
				break;
			}
		};

		// Inky loops around the walls at the lower right corner of the maze:
		inky.control.state(GhostState.Scattering, new LoopAroundWalls(inky, 32, 26, Top4.W, true));

		// Inky chases together with Blinky.
		inky.control.state(GhostState.Chasing, new ChaseWithPartner(inky, blinky, pacMan));

		/*
		 * "Pinky", the pink ghost.
		 */

		// Pinky waits inside the ghost house:
		pinky.control.state(GhostState.Waiting).entry = state ->

		{
			pinky.placeAt(PINKY_HOME);
			pinky.setMoveDir(Top4.S);
			pinky.setAnimated(true);
		};

		// Pinky bounce while waiting. When waiting is over, he starts scattering or chasing.
		pinky.control.state(GhostState.Waiting).update = state -> {
			if (!state.isTerminated()) {
				pinky.bounce();
				return;
			}
			switch (ghostAttackTimer.state()) {
			case Scattering:
				pinky.beginScattering();
				break;
			case Chasing:
				pinky.beginChasing();
				break;
			default:
				break;
			}
		};

		// Pinky loops around the walls at the upper right corner of the maze:
		pinky.control.state(GhostState.Scattering, new LoopAroundWalls(pinky, 4, 1, Top4.S, false));

		// Pinky follows the position 4 tiles ahead of Pac-Man:
		pinky.control.state(GhostState.Chasing, new FollowTileAheadOfPacMan(pinky, pacMan, 4));

		/*
		 * "Clyde", the yellow ghost.
		 */

		// Clyde waits inside ghost house:
		clyde.control.state(GhostState.Waiting).entry = state -> {
			clyde.placeAt(CLYDE_HOME);
			clyde.setMoveDir(Top4.N);
			clyde.setAnimated(true);
		};

		// Clyde bounces while waiting. When waiting is over, he starts scattering or chasing.
		clyde.control.state(GhostState.Waiting).update = state -> {
			if (!state.isTerminated()) {
				clyde.bounce();
				return;
			}
			switch (ghostAttackTimer.state()) {
			case Scattering:
				clyde.beginScattering();
				break;
			case Chasing:
				clyde.beginChasing();
				break;
			default:
				break;
			}
		};

		// Clyde loops around the walls at the left lower corner of the maze:
		clyde.control.state(GhostState.Scattering, new LoopAroundWalls(clyde, 32, 1, Top4.E, false));

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

		pacMan.setEnemies(blinky, inky, pinky, clyde);

		// Update entity collection
		app.entities.removeAll(PacMan.class);
		app.entities.removeAll(Ghost.class);
		app.entities.add(pacMan);
		app.entities.add(blinky, inky, pinky, clyde);
	}

	private float getGhostSpeed(Ghost ghost) {
		TileContent content = board.getContent(ghost.currentTile());
		if (content == Tunnel) {
			return levels.getGhostSpeedInTunnel(level);
		} else if (content == GhostHouse) {
			return levels.getGhostSpeedInHouse();
		} else if (ghost.control.inState(GhostState.Frightened)) {
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
		FlashText.show(app, String.valueOf(object), app.getTheme().getTextFont().deriveFont(Font.PLAIN, SPRITE_SIZE),
				Color.YELLOW, app.motor.toFrames(1), new Vector2(x, y), new Vector2(0, -0.2f));
	}

	@Override
	public void draw(Graphics2D g) {
		final PacManTheme theme = app.getTheme();

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
				float x = homeTile.x * TILE_SIZE, y = homeTile.y * TILE_SIZE;
				g.fillRect((int) x, (int) y, TILE_SIZE, TILE_SIZE);
			});
		}

		// Flash texts
		app.entities.allOf(FlashText.class).forEach(text -> text.draw(g));
	}
}