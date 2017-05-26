package de.amr.games.pacman.play;

import static de.amr.easy.game.Application.Log;
import static de.amr.easy.grid.impl.Top4.W;
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
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.lang.String.format;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
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
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.core.entities.ghost.behaviors.LoopAroundWalls;
import de.amr.games.pacman.core.entities.ghost.behaviors.TargetAtTileAheadOfPacMan;
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
	public static final Tile BLINKY_HOME = new Tile(14, 13.5f);
	public static final Tile INKY_HOME = new Tile(17.5f, 11.5f);
	public static final Tile PINKY_HOME = new Tile(17.5f, 13.5f);
	public static final Tile CLYDE_HOME = new Tile(17.5f, 15.5f);
	public static final Tile GHOST_HOUSE_ENTRY = new Tile(14, 13);
	public static final Tile BONUS_TILE = new Tile(20f, 13f);

	// values
	public static final int POINTS_FOR_PELLET = 10;
	public static final int POINTS_FOR_ENERGIZER = 50;
	public static final int BONUS1_PELLETS_LEFT = 170;
	public static final int BONUS2_PELLETS_LEFT = 70;
	public static final int SCORE_FOR_EXTRALIFE = 10000;
	public static final int POINTS_FOR_KILLING_FIRST_GHOST = 200;
	public static final int WAIT_TICKS_AFTER_PELLET_EATEN = 1;
	public static final int WAIT_TICKS_AFTER_ENERGIZER_EATEN = 3;

	/**
	 * State machine which controls the game play.
	 */
	private class PlayControl extends StateMachine<PlayState> {

		public PlayControl() {

			super("Play control", new EnumMap<>(PlayState.class));

			// Initializing

			state(Initializing).entry = state -> {
				app.assets.sound("sfx/insert-coin.mp3").play();
				lives = 3;
				score = 0;
				bonusCollection.clear();
				nextGhostPoints = 0;
				app.entities.removeAll(GameEntity.class);
				createPacManAndGhosts();
				createAttackControl();
				level = 1;
				initLevel();
				app.getTheme().getEnergizerSprite().setAnimated(false);
			};

			state(Initializing).update = state -> {
				if (!app.assets.sound("sfx/insert-coin.mp3").isRunning()) {
					changeTo(Ready);
				}
			};

			// Ready

			state(Ready).entry = state -> {
				app.getTheme().getEnergizerSprite().setAnimated(true);
				attackControl.init(level);
			};

			state(Ready).update = state -> {
				if (Keyboard.pressedOnce(VK_SPACE)) {
					changeTo(StartingLevel);
				}
			};

			// StartingLevel

			state(StartingLevel).entry = state -> {
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
				app.getTheme().getEnergizerSprite().setAnimated(true);
				pacMan.control.changeTo(PacManState.Eating);
				ghosts.get().forEach(ghost -> ghost.control.state(GhostState.Waiting).setDuration(getGhostWaitingTime(ghost)));
				attackControl.start();
			};

			state(Playing).update = state -> {
				attackControl.update();
				app.entities.all().forEach(GameEntity::update);
				if (isBonusEnabled() && bonusTimeRemaining-- == 0) {
					setBonusEnabled(false);
				}
				if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
					++level;
					initLevel();
					attackControl.init(level);
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

			state(Crashing).exit = state -> {
				pacMan.init();
				ghosts.get().forEach(ghost -> {
					ghost.init();
					ghost.setAnimated(true);
				});
				attackControl.init(level);
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
				if (Keyboard.pressedOnce(VK_SPACE)) {
					changeTo(Initializing);
				}
			};
		}
	}

	// State machines
	private StateMachine<PlayState> playControl;
	private GhostAttackTimer attackControl;

	// Entities
	private PacMan pacMan;
	private Ghost blinky, inky, pinky, clyde;
	private Supplier<Stream<Ghost>> ghosts = () -> Stream.of(blinky, inky, pinky, clyde);

	// Scene-specific data
	private Board board;
	private PacManGameLevels levels;
	private int level;
	private int lives;
	private int score;
	private Highscore highscore;
	private List<BonusSymbol> bonusCollection;
	private int bonusTimeRemaining;
	private int nextGhostPoints;
	private int ghostsEatenAtLevel;

	private final Random rand = new Random();

	public PlayScene(PacManGame app) {
		super(app);
	}

	@Override
	public void init() {
		levels = new PacManGameLevels(8 * TILE_SIZE / app.motor.getFrequency());
		board = new Board(app.assets.text("board.txt").split("\n"));
		highscore = new Highscore("pacman-hiscore.txt");
		bonusCollection = new ArrayList<>();
		playControl = new PlayControl();
		playControl.changeTo(PlayState.Initializing);
	}

	@Override
	public void update() {
		// cheats and debug keys
		if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_I)) {
			app.settings.set("drawInternals", !app.settings.getBool("drawInternals"));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_G)) {
			app.settings.set("drawGrid", !app.settings.getBool("drawGrid"));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_R)) {
			app.settings.set("drawRoute", !app.settings.getBool("drawRoute"));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_L)) {
			lives += 1;
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_B)) {
			bonusCollection.add(levels.getBonusSymbol(level));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_P)) {
			board.tilesWithContent(Pellet).forEach(tile -> board.setContent(tile, None));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_E)) {
			board.tilesWithContent(Energizer).forEach(tile -> board.setContent(tile, None));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_T)) {
			app.selectNextTheme();
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_K)) {
			ghosts.get().forEach(Ghost::killed);
		}
		playControl.update();
	}

	private void initLevel() {
		ghostsEatenAtLevel = 0;
		board.resetContent();
		setBonusEnabled(false);
		app.entities.all().forEach(GameEntity::init);
		Log.info(format("Level %d initialized: %d pellets and %d energizers.", level, board.count(Pellet),
				board.count(Energizer)));
	}

	private void createPacManAndGhosts() {

		pacMan = new PacMan(app, board, PACMAN_HOME);

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
			bonusCollection.add(levels.getBonusSymbol(level));
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

		blinky = new Ghost(app, board, "Blinky", BLINKY_HOME);
		blinky.setColor(Color.RED);

		inky = new Ghost(app, board, "Inky", INKY_HOME);
		inky.setColor(new Color(64, 224, 208));

		pinky = new Ghost(app, board, "Pinky", PINKY_HOME);
		pinky.setColor(Color.PINK);

		clyde = new Ghost(app, board, "Clyde", CLYDE_HOME);
		clyde.setColor(Color.ORANGE);

		// Define common ghost behavior:

		ghosts.get().forEach(ghost -> {

			ghost.speed = () -> getGhostSpeed(ghost);

			ghost.control.state(GhostState.Waiting).update = state -> {
				if (state.isTerminated()) {
					switch (attackControl.currentState()) {
					case Scattering:
						ghost.startScattering();
						break;
					case Chasing:
						ghost.startChasing();
						break;
					default:
						break;
					}
				}
			};

			ghost.stateAfterFrightened = () -> {
				switch (attackControl.currentState()) {
				case Scattering:
					return GhostState.Scattering;
				case Chasing:
					return GhostState.Chasing;
				default:
					return ghost.control.stateID();
				}
			};

			// "frightened" state:

			ghost.control.state(GhostState.Frightened).update = state -> {
				ghost.moveRandomly();
			};

			// "dead" state:

			ghost.control.state(GhostState.Dead).update = state -> {
				ghost.follow(ghost.getHome());
				if (ghost.isAtHome()) {
					ghost.control.changeTo(GhostState.Recovering);
				}
			};

			// "recovering" state:

			ghost.control.state(GhostState.Recovering).entry = state -> {
				state.setDuration(getGhostRecoveringDuration(ghost));
			};

			ghost.control.state(GhostState.Recovering).update = state -> {
				if (state.isTerminated()) {
					ghost.control.changeTo(ghost.stateAfterFrightened.get());
					ghost.setAnimated(true);
				}
			};
		});

		// Define individual ghost behavior:

		// "Blinky", the red ghost

		// wait just before ghost house:
		blinky.control.state(GhostState.Waiting).entry = state -> {
			blinky.placeAt(blinky.getHome());
			blinky.setMoveDir(W);
			blinky.setAnimated(true);
		};

		// loop around block at right upper corner of maze:
		blinky.control.state(GhostState.Scattering, new LoopAroundWalls(blinky, 4, 26, Top4.S, true));

		// target Pac-Man's current position:
		blinky.control.state(GhostState.Chasing).update = state -> {
			blinky.follow(pacMan.currentTile());
		};

		// "Inky", the blue ghost

		// wait inside ghost house:
		inky.control.state(GhostState.Waiting).entry = state -> {
			inky.placeAt(inky.getHome());
			inky.setMoveDir(Top4.N);
			inky.setAnimated(true);
		};

		// bounce while waiting:
		inky.control.state(GhostState.Waiting).update = state -> {
			if (state.isTerminated()) {
				switch (attackControl.currentState()) {
				case Scattering:
					inky.startScattering();
					break;
				case Chasing:
					inky.startChasing();
					break;
				default:
					break;
				}
			} else {
				inky.bounce();
			}
		};

		// loop around block at right lower corner of maze:
		inky.control.state(GhostState.Scattering, new LoopAroundWalls(inky, 32, 26, W, true));

		// target tile in front of Pac-Man or target Pac-Man directly:
		inky.control.state(GhostState.Chasing, new ChaseWithPartner(inky, blinky, pacMan));

		// "Pinky", the pink ghost

		// wait inside ghost house:
		pinky.control.state(GhostState.Waiting).entry = state -> {
			pinky.placeAt(pinky.getHome());
			pinky.setMoveDir(Top4.S);
			pinky.setAnimated(true);
		};

		// bounce while waiting:
		pinky.control.state(GhostState.Waiting).update = state -> {
			if (state.isTerminated()) {
				switch (attackControl.currentState()) {
				case Scattering:
					pinky.startScattering();
					break;
				case Chasing:
					pinky.startChasing();
					break;
				default:
					break;
				}
			} else {
				pinky.bounce();
			}
		};

		// loop around block at right upper corner of maze:
		pinky.control.state(GhostState.Scattering, new LoopAroundWalls(pinky, 4, 1, Top4.S, false));

		// target tile which is 4 tiles ahead of Pac-Man's current position:
		pinky.control.state(GhostState.Chasing, new TargetAtTileAheadOfPacMan(pinky, pacMan, 4));

		// "Clyde", the yellow ghost

		// wait inside ghost house:
		clyde.control.state(GhostState.Waiting).entry = state -> {
			clyde.placeAt(clyde.getHome());
			clyde.setMoveDir(Top4.N);
			clyde.setAnimated(true);
		};

		// bounce when waiting:
		clyde.control.state(GhostState.Waiting).update = state -> {
			if (state.isTerminated()) {
				switch (attackControl.currentState()) {
				case Scattering:
					clyde.startScattering();
					break;
				case Chasing:
					clyde.startChasing();
					break;
				default:
					break;
				}
			} else {
				clyde.bounce();
			}
		};

		// loop around block at left lower corner of maze:
		clyde.control.state(GhostState.Scattering, new LoopAroundWalls(clyde, 32, 1, Top4.E, false));

		// target Pac-Man's position if more than 8 tiles away, otherwise move randomly:
		clyde.control.state(GhostState.Chasing).update = state -> {
			if (clyde.insideGhostHouse()) {
				clyde.follow(GHOST_HOUSE_ENTRY);
			} else if (clyde.currentTile().distance(pacMan.currentTile()) > 8) {
				clyde.follow(pacMan.currentTile());
			} else {
				clyde.moveRandomly();
			}
		};

		// add all to entity collection
		app.entities.removeAll(PacMan.class);
		app.entities.removeAll(Ghost.class);
		app.entities.add(pacMan);
		app.entities.add(blinky, inky, pinky, clyde);

	}

	private void createAttackControl() {
		attackControl = new GhostAttackTimer(app);
		attackControl.trace = true;

		attackControl.onPhaseStart = phase -> {
			if (phase == GhostAttackState.Initialized) {
				ghosts.get().forEach(ghost -> {
					ghost.control.changeTo(GhostState.Waiting);
				});
			} else if (phase == GhostAttackState.Scattering) {
				ghosts.get().forEach(Ghost::startScattering);
				app.assets.sound("sfx/siren.mp3").loop();
			} else if (phase == GhostAttackState.Chasing) {
				app.assets.sound("sfx/siren.mp3").loop();
				ghosts.get().forEach(Ghost::startChasing);
			}
		};

		attackControl.onPhaseEnd = phase -> {
			if (phase == GhostAttackState.Scattering) {
				app.assets.sound("sfx/siren.mp3").stop();
			} else if (phase == GhostAttackState.Chasing) {
				app.assets.sound("sfx/siren.mp3").stop();
			}
		};
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

	private int getGhostWaitingTime(Ghost ghost) {
		if ("Blinky".equals(ghost.getName())) {
			return 0;
		}
		return app.motor.toFrames(1 + rand.nextInt(2));
	}

	private int getGhostRecoveringDuration(Ghost ghost) {
		return app.motor.toFrames(rand.nextInt(4));
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

		// Grid lines & internal state
		if (app.settings.getBool("drawGrid")) {
			drawGridLines(g, getWidth(), getHeight());
		}
		if (app.settings.getBool("drawInternals")) {
			// mark home positions of ghosts
			ghosts.get().forEach(ghost -> {
				g.setColor(ghost.getColor());
				float x = ghost.getHome().x * TILE_SIZE, y = ghost.getHome().y * TILE_SIZE;
				g.fillRect((int) x, (int) y, TILE_SIZE, TILE_SIZE);
			});
		}

		// Entities
		pacMan.draw(g);
		if (!playControl.inState(PlayState.Crashing)) {
			ghosts.get().forEach(ghost -> ghost.draw(g));
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

		// Lives score
		range(0, lives).forEach(i -> drawSprite(g, board.numRows - 2, 2 * (i + 1), theme.getLifeSprite()));

		// Bonus score
		int col = board.numCols - 2;
		for (BonusSymbol bonus : bonusCollection) {
			drawSprite(g, board.numRows - 2, col, theme.getBonusSprite(bonus));
			col -= 2;
		}

		// Play state
		if (app.settings.getBool("drawInternals")) {
			drawTextCentered(g, getWidth(), 33, playControl.stateID() + "  " + attackControl.currentState());
		}

		// Flash texts
		app.entities.allOf(FlashText.class).forEach(text -> text.draw(g));
	}
}