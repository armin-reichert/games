package de.amr.games.pacman;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.data.Board.BLINKY_HOME_COL;
import static de.amr.games.pacman.data.Board.BLINKY_HOME_ROW;
import static de.amr.games.pacman.data.Board.BONUS_COL;
import static de.amr.games.pacman.data.Board.BONUS_ROW;
import static de.amr.games.pacman.data.Board.CLYDE_HOME_COL;
import static de.amr.games.pacman.data.Board.CLYDE_HOME_ROW;
import static de.amr.games.pacman.data.Board.INKY_HOME_COL;
import static de.amr.games.pacman.data.Board.INKY_HOME_ROW;
import static de.amr.games.pacman.data.Board.NUM_COLS;
import static de.amr.games.pacman.data.Board.NUM_ROWS;
import static de.amr.games.pacman.data.Board.PACMAN_HOME_COL;
import static de.amr.games.pacman.data.Board.PACMAN_HOME_ROW;
import static de.amr.games.pacman.data.Board.PINKY_HOME_COL;
import static de.amr.games.pacman.data.Board.PINKY_HOME_ROW;
import static de.amr.games.pacman.data.Bonus.Apple;
import static de.amr.games.pacman.data.Bonus.Bell;
import static de.amr.games.pacman.data.Bonus.Cherries;
import static de.amr.games.pacman.data.Bonus.Galaxian;
import static de.amr.games.pacman.data.Bonus.Grapes;
import static de.amr.games.pacman.data.Bonus.Key;
import static de.amr.games.pacman.data.Bonus.Peach;
import static de.amr.games.pacman.data.Bonus.Strawberry;
import static de.amr.games.pacman.data.TileContent.Energizer;
import static de.amr.games.pacman.data.TileContent.None;
import static de.amr.games.pacman.data.TileContent.Pellet;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Frightened;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Waiting;
import static de.amr.games.pacman.ui.PacManUI.SPRITE_SIZE;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_T;
import static java.util.Arrays.asList;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.common.FlashText;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.Bonus;
import de.amr.games.pacman.data.Highscore;
import de.amr.games.pacman.entities.PacMan;
import de.amr.games.pacman.entities.PacManGameEntity;
import de.amr.games.pacman.entities.PacManState;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.entities.ghost.behaviors.DirectOrProactiveChasing;
import de.amr.games.pacman.entities.ghost.behaviors.GhostLoopingAroundWalls;
import de.amr.games.pacman.entities.ghost.behaviors.GhostMessage;
import de.amr.games.pacman.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.entities.ghost.behaviors.ProactiveChasing;
import de.amr.games.pacman.fsm.StateMachine;
import de.amr.games.pacman.scenes.PlayScene;
import de.amr.games.pacman.ui.ClassicUI;
import de.amr.games.pacman.ui.ModernUI;
import de.amr.games.pacman.ui.PacManUI;

/**
 * The Pac-Man game main class.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends Application {

	public static final PacManGame Game = new PacManGame();

	public static void main(String... args) {
		Game.settings.title = "Armin's Pac-Man";
		Game.settings.width = NUM_COLS * TILE_SIZE;
		Game.settings.height = NUM_ROWS * TILE_SIZE;
		Game.settings.scale = args.length > 0 ? Float.valueOf(args[0]) / Game.settings.height : 1;
		Game.settings.fullScreenOnStart = false;
		Game.settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		Game.settings.set("themes", Arrays.asList(new ClassicUI(), new ModernUI()));
		Game.settings.set("drawInternals", false);
		Game.settings.set("drawGrid", false);
		Log.setLevel(Level.ALL);
		Game.gameLoop.log = true;
		launch(Game);
	}

	// Game parameters

	private static final Object[][] LEVELS = {
		/*@formatter:off*/
		null,
		{ Cherries, 	100, 	.80f, .71f, .75f, .40f, 20, .8f, 10, 	.85f, 	.90f, 	.79f, 	.50f, 6 },
		{ Strawberry, 300, 	.90f, .79f, .85f, .45f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 5 },
		{ Peach, 			500, 	.90f, .79f, .85f, .45f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 4 },
		{ Peach, 			500, 	.90f, .79f, .85f, .50f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 3 },
		{ Apple, 			700, 		1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f,   	1f, 	.79f, 	.60f, 2 },
		{ Apple, 			700, 		1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 5 },
		{ Grapes, 		1000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ Grapes, 		1000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ Galaxian, 	2000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ Galaxian, 	2000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 5 },
		{ Bell, 			3000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ Bell, 			3000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ Key, 				5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ Key, 				5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 3 }, 
		{ Key, 				5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 }, 
		{ Key, 				5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 }, 
		{ Key, 				5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 0 }, 
		/*@formatter:on*/
	};

	private static final int[][] SCATTER_DURATION_SECS = {
		/*@formatter:off*/
		{ 7, 7, 5, 5, 0 }, 	// level 1
		{ 7, 7, 5, 0, 0 }, 	// level 2-4
		{ 5, 5, 5, 0, 0 }   // level 5-
		/*@formatter:on*/
	};

	private static final int[][] CHASE_DURATION_SECS = {
		/*@formatter:off*/
		{ 20, 20, 20, 	Integer.MAX_VALUE },  // level 1 
		{ 20, 20, 1033, Integer.MAX_VALUE },	// level 2-4
		{ 20, 20, 1037, Integer.MAX_VALUE } 	// level 5-
		/*@formatter:on*/
	};

	public static final int POINTS_FOR_PELLET = 10;
	public static final int POINTS_FOR_ENERGIZER = 50;
	public static final int BONUS1_REMAINING_PELLETS = 170;
	public static final int BONUS2_REMAINING_PELLETS = 70;
	public static final int EXTRA_LIFE_SCORE = 10000;
	public static final int FIRST_GHOST_VALUE = 200;
	public static final int WAIT_TICKS_ON_EATING_PELLET = 1;
	public static final int WAIT_TICKS_ON_EATING_ENERGIZER = 3;

	// Game data
	public PacMan pacMan;
	public Ghost blinky, inky, pinky, clyde;
	public Board board;
	public int level;
	private int wave;
	public int lives;
	public int score;
	public Highscore highscore;
	public List<Bonus> bonusScore;
	public Optional<Bonus> bonus;
	public int bonusTimeRemaining;
	private int ghostValue;
	private int ghostsEatenAtLevel;
	private StateMachine<PlayState> playControl;
	private StateMachine<AttackState> attackControl;
	private int themeIndex;

	@Override
	protected void init() {
		playControl = new PlayControl();
		attackControl = new AttackControl();
		board = new Board(assets.text("board.txt"));
		highscore = new Highscore("pacman-hiscore.txt");
		bonusScore = new ArrayList<>();
		views.add(new PlayScene(this));
		views.show(PlayScene.class);
		playControl.changeTo(PlayState.Initializing);
	}

	public void updateGameState() {
		playControl.update();
	}

	private void initLevel(int newLevel) {
		board.reset();
		level = newLevel;
		wave = 1;
		bonus = Optional.empty();
		bonusTimeRemaining = 0;
		ghostsEatenAtLevel = 0;
		Log.info(String.format("Level %d: %d pellets and %d energizers. Frames/sec: %d", level, board.count(Pellet),
				board.count(Energizer), gameLoop.getTargetFrameRate()));
	}

	private void createPacManAndGhosts() {

		pacMan = new PacMan(PACMAN_HOME_ROW, PACMAN_HOME_COL);

		pacMan.onPelletFound = tile -> {
			board.setContent(tile, None);
			score(POINTS_FOR_PELLET);
			long pelletCount = board.count(Pellet);
			if (pelletCount == BONUS1_REMAINING_PELLETS || pelletCount == BONUS2_REMAINING_PELLETS) {
				enableBonus(true);
			}
			pacMan.freeze(WAIT_TICKS_ON_EATING_PELLET);
			assets.sound("sfx/eat-pill.mp3").play();
		};

		pacMan.onEnergizerFound = tile -> {
			board.setContent(tile, None);
			score(POINTS_FOR_ENERGIZER);
			ghostValue = FIRST_GHOST_VALUE;
			pacMan.freeze(WAIT_TICKS_ON_EATING_ENERGIZER);
			pacMan.startAttacking(getGhostFrightenedDuration(), getPacManAttackingSpeed());
			assets.sound("sfx/eat-pill.mp3").play();
		};

		pacMan.onBonusFound = bonus -> {
			int points = getBonusValue();
			score(points);
			bonusScore.add(bonus);
			showFlashText(points, BONUS_COL * TILE_SIZE, BONUS_ROW * TILE_SIZE);
			assets.sound("sfx/eat-fruit.mp3").play();
		};

		pacMan.onGhostMet = ghost -> {
			if (ghost.control.inState(Dead, Waiting, Recovering)) {
				return;
			}
			if (pacMan.control.inState(PacManState.Frightening)) {
				Log.info("Pac-Man meets ghost " + ghost.getName());
				assets.sound("sfx/eat-ghost.mp3").play();
				score(ghostValue);
				showFlashText(ghostValue, ghost.tr.getX(), ghost.tr.getY());
				ghostValue *= 2;
				if (++ghostsEatenAtLevel == 16) {
					score(12000);
				}
				ghost.receive(GhostMessage.Die);
			} else {
				Log.info("Ghost " + ghost.getName() + " kills Pac-Man.");
				--lives;
				pacMan.control.changeTo(PacManState.Dying);
				playControl.changeTo(lives > 0 ? PlayState.Crashing : PlayState.GameOver);
			}
		};

		// Create the ghosts and define their behavior

		blinky = new Ghost("Blinky", BLINKY_HOME_ROW, BLINKY_HOME_COL);
		blinky.color = Color.RED;

		inky = new Ghost("Inky", INKY_HOME_ROW, INKY_HOME_COL);
		inky.color = new Color(64, 224, 208);

		pinky = new Ghost("Pinky", PINKY_HOME_ROW, PINKY_HOME_COL);
		pinky.color = Color.PINK;

		clyde = new Ghost("Clyde", CLYDE_HOME_ROW, CLYDE_HOME_COL);
		clyde.color = Color.ORANGE;

		// Common ghost behavior

		asList(blinky, inky, pinky, clyde).forEach(ghost -> {

			// ghost state after frightening or recovering ends
			ghost.stateAfterFrightened = () -> {
				if (attackControl.inState(AttackState.Chasing)) {
					return Chasing;
				} else if (attackControl.inState(AttackState.Scattering)) {
					return Scattering;
				} else {
					return ghost.control.stateID();
				}
			};

			// "frightened" state

			ghost.control.state(Frightened).entry = state -> {
				ghost.speed = getGhostSpeedWhenFrightened();
			};

			ghost.control.state(Frightened).update = state -> {
				ghost.moveRandomly();
			};

			ghost.control.state(Frightened).exit = state -> {
				ghost.speed = getGhostSpeedNormal();
			};

			// "dead" state

			ghost.control.state(Dead).update = state -> {
				ghost.walkHome();
				if (ghost.isAtHome()) {
					ghost.control.changeTo(Recovering);
				}
			};

			// "recovering" state

			ghost.control.state(Recovering).setDuration(getGhostRecoveringDuration(ghost));

			ghost.control.state(Recovering).update = state -> {
				if (state.isTerminated()) {
					ghost.control.changeTo(ghost.stateAfterFrightened.get());
				}
			};
		});

		// Define individual ghost behavior

		// "Blinky" directly follows Pac-Man

		blinky.control.state(Waiting).entry = state -> {
			blinky.placeAt(blinky.home);
			blinky.moveDir = W;
		};

		blinky.control.state(Scattering, new GhostLoopingAroundWalls(blinky, 4, 26, S, true));

		blinky.control.state(Chasing).update = state -> {
			blinky.followRoute(pacMan.currentTile());
		};

		// "Inky" aims at tile in front of Pac-Man or directly follows Pac-Man

		inky.control.state(Waiting).entry = state -> {
			inky.placeAt(inky.home);
			inky.moveDir = N;
		};
		inky.control.state(Waiting).update = state -> inky.moveBackAndForth();

		inky.control.state(Scattering, new GhostLoopingAroundWalls(inky, 32, 26, W, true));

		inky.control.state(Chasing, new DirectOrProactiveChasing(inky, blinky, pacMan));

		// "Pinky" aims at tile which is 4 tiles ahead of Pac-Man

		pinky.control.state(Waiting).entry = state -> {
			pinky.placeAt(pinky.home);
			pinky.moveDir = S;
		};
		pinky.control.state(Waiting).update = state -> pinky.moveBackAndForth();

		pinky.control.state(Scattering, new GhostLoopingAroundWalls(pinky, 4, 1, S, false));

		pinky.control.state(Chasing, new ProactiveChasing(pinky, pacMan, 4));

		// "Clyde" follows Pac-Man if it is more than 8 tiles away, otherwise moves randomly

		clyde.control.state(Waiting).entry = state -> {
			clyde.placeAt(clyde.home);
			clyde.moveDir = N;
		};
		clyde.control.state(Waiting).update = state -> clyde.moveBackAndForth();

		clyde.control.state(Scattering, new GhostLoopingAroundWalls(clyde, 32, 1, E, false));

		clyde.control.state(Chasing).update = state -> {
			if (clyde.insideGhostHouse()) {
				clyde.leaveGhostHouse();
			} else {
				if (clyde.currentTile().distance(pacMan.currentTile()) > 8) {
					clyde.followRoute(pacMan.currentTile());
				} else {
					clyde.moveRandomly();
				}
			}
		};

		// add entities to collection
		entities.removeAll(PacMan.class);
		entities.removeAll(Ghost.class);
		entities.add(pacMan, blinky, inky, pinky, clyde);
	}

	private void score(int points) {
		if (score < EXTRA_LIFE_SCORE && score + points >= EXTRA_LIFE_SCORE) {
			++lives;
			assets.sound("sfx/extra-life.mp3").play();
		}
		score += points;
	}

	private void enableBonus(boolean enabled) {
		if (enabled) {
			bonus = Optional.of(getBonus());
			bonusTimeRemaining = gameLoop.secToFrames(9 + (float) Math.random());
		} else {
			bonus = Optional.empty();
			bonusTimeRemaining = 0;
		}
	}

	private void showFlashText(Object object, float x, float y) {
		if (x > getWidth() - 3 * TILE_SIZE) {
			x -= 3 * TILE_SIZE;
		}
		FlashText.show(this, String.valueOf(object), selectedTheme().getTextFont().deriveFont(Font.PLAIN, SPRITE_SIZE),
				Color.YELLOW, gameLoop.secToFrames(1), new Vector2(x, y), new Vector2(0, -0.2f));
	}

	private List<PacManUI> themes() {
		return settings.get("themes");
	}

	public PacManUI selectedTheme() {
		return themes().get(themeIndex);
	}

	private void nextTheme() {
		if (++themeIndex == themes().size()) {
			themeIndex = 0;
		}
		applyTheme();
	}

	public void applyTheme() {
		entities.allOf(PacManGameEntity.class).forEach(e -> e.setTheme(selectedTheme()));
		entities.all().forEach(GameEntity::init);
		selectedTheme().getEnergizer().setAnimated(false);
	}

	private float getBaseSpeed() {
		return TILE_SIZE * 8 / settings.fps;
	}

	public int getGhostWaitingDuration(Ghost ghost) {
		float seconds = 0;
		if (ghost == blinky) {
			seconds = 0;
		} else if (ghost == clyde) {
			seconds = 1.5f;
		} else if (ghost == inky) {
			seconds = 10;
		} else if (ghost == pinky) {
			seconds = 0.5f;
		}
		return gameLoop.secToFrames(seconds);
	}

	public int getGhostRecoveringDuration(Ghost ghost) {
		return gameLoop.secToFrames(2);
	}

	public int getScatteringDuration() {
		int nCols = SCATTER_DURATION_SECS[0].length;
		int row = (level == 1) ? 0 : (level <= 4) ? 1 : 2;
		int col = wave <= nCols ? wave - 1 : nCols - 1;
		return gameLoop.secToFrames(SCATTER_DURATION_SECS[row][col]);
	}

	public int getChasingDuration() {
		int nCols = CHASE_DURATION_SECS[0].length;
		int row = (level == 1) ? 0 : (level <= 4) ? 1 : 2;
		int col = wave <= nCols ? wave - 1 : nCols - 1;
		return gameLoop.secToFrames(CHASE_DURATION_SECS[row][col]);
	}

	public Bonus getBonus() {
		return (Bonus) LEVELS[level][0];
	}

	public int getBonusValue() {
		return (Integer) LEVELS[level][1];
	}

	public float getPacManSpeed() {
		return getBaseSpeed() * (Float) LEVELS[level][2];
	}

	public float getGhostSpeedNormal() {
		return getBaseSpeed() * (Float) LEVELS[level][4];
	}

	public float getGhostSpeedInTunnel() {
		return getBaseSpeed() * (Float) LEVELS[level][5];
	}

	public float getGhostSpeedInHouse() {
		return getGhostSpeedNormal() / 2;
	}

	public float getPacManAttackingSpeed() {
		return getBaseSpeed() * (Float) LEVELS[level][10];
	}

	public float getGhostSpeedWhenFrightened() {
		return getBaseSpeed() * (Float) LEVELS[level][12];
	}

	public int getGhostFrightenedDuration() {
		return gameLoop.secToFrames((Integer) LEVELS[level][13]);
	}

	// State machine for ghost attacks

	public enum AttackState {
		Starting, Scattering, Chasing, Complete
	}

	private class AttackControl extends StateMachine<AttackState> {

		public AttackControl() {
			super("Attack", new EnumMap<>(AttackState.class));

			state(AttackState.Starting).update = state -> {
				log();
				changeTo(AttackState.Scattering);
			};

			state(AttackState.Scattering).entry = state -> {
				state.setDuration(getScatteringDuration());
				entities.allOf(Ghost.class).forEach(ghost -> {
					ghost.receive(GhostMessage.Scatter);
				});
				log();
			};

			state(AttackState.Scattering).update = state -> {
				if (state.isTerminated()) {
					state(AttackState.Chasing).setDuration(getChasingDuration());
					changeTo(AttackState.Chasing);
				} else {
					entities.all().forEach(GameEntity::update);
				}
			};

			state(AttackState.Chasing).entry = state -> {
				entities.allOf(Ghost.class).forEach(ghost -> {
					ghost.receive(GhostMessage.Chase);
				});
				assets.sound("sfx/waza.mp3").loop();
				log();
			};

			state(AttackState.Chasing).update = state -> {
				if (state.isTerminated()) {
					changeTo(AttackState.Complete);
				} else {
					entities.all().forEach(GameEntity::update);
				}
			};

			state(AttackState.Complete).entry = state -> assets.sound("sfx/waza.mp3").stop();
		}

		void log() {
			Log.info(String.format("Level %d, wave %d: enter state %s for %d seconds", level, wave, stateID(),
					gameLoop.framesToSec(state().getDuration())));
		}
	}

	// State machine for controlling the playing

	public PlayState getPlayState() {
		return playControl.stateID();
	}

	public enum PlayState {
		Initializing, Ready, StartingLevel, Playing, Crashing, GameOver
	}

	private class PlayControl extends StateMachine<PlayState> {

		public PlayControl() {
			super("Play control", new EnumMap<>(PlayState.class));

			// Initializing

			state(PlayState.Initializing).entry = state -> {
				assets.sound("sfx/insert-coin.mp3").play();
				lives = 3;
				score = 0;
				bonusScore.clear();
				ghostValue = 0;
				initLevel(1);
				entities.removeAll(GameEntity.class);
				createPacManAndGhosts();
				applyTheme();
			};

			state(PlayState.Initializing).update = state -> {
				if (!assets.sound("sfx/insert-coin.mp3").isRunning()) {
					changeTo(PlayState.Ready);
				}
			};

			// Ready

			state(PlayState.Ready).update = state -> {
				if (Keyboard.pressedOnce(VK_ENTER)) {
					changeTo(PlayState.StartingLevel);
				} else if (Keyboard.pressedOnce(VK_T)) {
					nextTheme();
				}
			};

			// StartingLevel

			state(PlayState.StartingLevel).entry = state -> {
				assets.sound("sfx/ready.mp3").play();
				selectedTheme().getEnergizer().setAnimated(true);
				pacMan.control.changeTo(PacManState.Waiting);
				entities.allOf(Ghost.class).forEach(ghost -> {
					ghost.control.state(GhostState.Waiting).setDuration(getGhostWaitingDuration(ghost));
					ghost.init();
					ghost.setAnimated(true);
				});
			};

			state(PlayState.StartingLevel).update = state -> {
				entities.all().forEach(GameEntity::update);
				if (!assets.sound("sfx/ready.mp3").isRunning()) {
					changeTo(PlayState.Playing);
				}
			};

			// Playing

			state(PlayState.Playing).entry = state -> {
				pacMan.speed = getPacManSpeed();
				pacMan.control.changeTo(PacManState.Eating);
				attackControl.changeTo(AttackState.Starting);
				assets.sound("sfx/eating.mp3").loop();
			};

			state(PlayState.Playing).update = state -> {
				if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
					attackControl.changeTo(AttackState.Complete);
					changeTo(PlayState.StartingLevel, newState -> {
						newState.setDuration(gameLoop.secToFrames(4));
						initLevel(++level);
					});
				} else if (attackControl.inState(AttackState.Complete)) {
					attackControl.changeTo(AttackState.Starting, newState -> ++wave);
				} else {
					attackControl.update();
				}
				bonus.ifPresent(bonus -> {
					if (--bonusTimeRemaining <= 0) {
						enableBonus(false);
					}
				});
			};

			state(PlayState.Playing).exit = state -> {
				assets.sound("sfx/eating.mp3").stop();
				entities.removeAll(FlashText.class);
			};

			// Crashing

			state(PlayState.Crashing).entry = state -> {
				assets.sounds().forEach(Sound::stop);
				assets.sound("sfx/die.mp3").play();
				state.setDuration(gameLoop.secToFrames(3));
				Log.info("PacMan crashed, lives remaining: " + lives);
				selectedTheme().getEnergizer().setAnimated(false);
				enableBonus(false);
				attackControl.changeTo(AttackState.Complete);
			};

			state(PlayState.Crashing).update = state -> {
				if (state.isTerminated()) {
					selectedTheme().getEnergizer().setAnimated(true);
					pacMan.control.changeTo(PacManState.Waiting);
					entities.allOf(Ghost.class).forEach(ghost -> {
						ghost.init();
						ghost.control.state(GhostState.Waiting).setDuration(getGhostWaitingDuration(ghost));
						ghost.setAnimated(true);
					});
					changeTo(PlayState.Playing, newState -> newState.setDuration(0));
				}
			};

			state(PlayState.Crashing).exit = state -> {
				attackControl.changeTo(AttackState.Complete);
			};

			// GameOver

			state(PlayState.GameOver).entry = state -> {
				assets.sounds().forEach(Sound::stop);
				assets.sound("sfx/die.mp3").play();
				Log.info("Game over.");
				entities.all().forEach(entity -> entity.setAnimated(false));
				selectedTheme().getEnergizer().setAnimated(false);
				enableBonus(false);
				attackControl.changeTo(AttackState.Complete);
				if (score > highscore.getPoints()) {
					highscore.save(score, level);
				}
			};

			state(PlayState.GameOver).update = state -> {
				if (Keyboard.pressedOnce(VK_SPACE)) {
					changeTo(PlayState.Initializing);
				}
			};
		}
	}
}