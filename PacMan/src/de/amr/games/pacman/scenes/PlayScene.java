package de.amr.games.pacman.scenes;

import static de.amr.easy.game.Application.Log;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.PacManGame.BONUS1_REMAINING_PELLETS;
import static de.amr.games.pacman.PacManGame.BONUS2_REMAINING_PELLETS;
import static de.amr.games.pacman.PacManGame.EXTRA_LIFE_SCORE;
import static de.amr.games.pacman.PacManGame.FIRST_GHOST_POINTS;
import static de.amr.games.pacman.PacManGame.Game;
import static de.amr.games.pacman.PacManGame.POINTS_FOR_ENERGIZER;
import static de.amr.games.pacman.PacManGame.WAIT_TICKS_ON_EATING_ENERGIZER;
import static de.amr.games.pacman.PacManGame.WAIT_TICKS_ON_EATING_PELLET;
import static de.amr.games.pacman.data.Board.NUM_COLS;
import static de.amr.games.pacman.data.Board.NUM_ROWS;
import static de.amr.games.pacman.data.TileContent.Bonus;
import static de.amr.games.pacman.data.TileContent.Energizer;
import static de.amr.games.pacman.data.TileContent.GhostHouse;
import static de.amr.games.pacman.data.TileContent.None;
import static de.amr.games.pacman.data.TileContent.Pellet;
import static de.amr.games.pacman.data.TileContent.Tunnel;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Frightened;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Waiting;
import static de.amr.games.pacman.scenes.DrawUtil.drawGridLines;
import static de.amr.games.pacman.scenes.DrawUtil.drawSprite;
import static de.amr.games.pacman.scenes.DrawUtil.drawText;
import static de.amr.games.pacman.scenes.DrawUtil.drawTextCentered;
import static de.amr.games.pacman.ui.PacManUI.SPRITE_SIZE;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.lang.Math.round;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.common.FlashText;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.BonusSymbol;
import de.amr.games.pacman.data.Highscore;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.data.TileContent;
import de.amr.games.pacman.entities.PacMan;
import de.amr.games.pacman.entities.PacManState;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.entities.ghost.behaviors.DirectOrProactiveChasing;
import de.amr.games.pacman.entities.ghost.behaviors.GhostLoopingAroundWalls;
import de.amr.games.pacman.entities.ghost.behaviors.GhostMessage;
import de.amr.games.pacman.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.entities.ghost.behaviors.ProactiveChasing;
import de.amr.games.pacman.fsm.StateMachine;
import de.amr.games.pacman.ui.PacManUI;

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

	// State machine for controlling the ghost attacks

	public enum GhostAttackState {
		Starting, Scattering, Attacking, Over
	}

	private class GhostAttackControl extends StateMachine<GhostAttackState> {

		private final int[][] ATTACKING_DURATION_SECONDS = {
			/*@formatter:off*/
			{ 20, 20, 20, 	Integer.MAX_VALUE },  // level 1 
			{ 20, 20, 1033, Integer.MAX_VALUE },	// level 2-4
			{ 20, 20, 1037, Integer.MAX_VALUE } 	// level 5-
			/*@formatter:on*/
		};

		private final int[][] SCATTERING_DURATION_SECONDS = {
			/*@formatter:off*/
			{ 7, 7, 5, 5, 0 }, 	// level 1
			{ 7, 7, 5, 0, 0 }, 	// level 2-4
			{ 5, 5, 5, 0, 0 }   // level 5-
			/*@formatter:on*/
		};

		private int getScatteringDurationFrames() {
			int colCount = SCATTERING_DURATION_SECONDS[0].length;
			int row = (level == 1) ? 0 : (level <= 4) ? 1 : 2;
			int col = wave <= colCount ? wave - 1 : colCount - 1;
			return Game.gameLoop.secToFrames(SCATTERING_DURATION_SECONDS[row][col]);
		}

		private int getAttackingDurationFrames() {
			int colCount = ATTACKING_DURATION_SECONDS[0].length;
			int row = (level == 1) ? 0 : (level <= 4) ? 1 : 2;
			int col = wave <= colCount ? wave - 1 : colCount - 1;
			return Game.gameLoop.secToFrames(ATTACKING_DURATION_SECONDS[row][col]);
		}

		private void trace() {
			Application.Log.info(format("Level %d, wave %d: enter state %s for %d seconds", level, wave, stateID(),
					Game.gameLoop.framesToSec(state().getDuration())));
		}

		private void updateGhostSpeed(Ghost ghost) {
			TileContent content = board.getContent(ghost.currentTile());
			if (content == Tunnel) {
				ghost.speed = Game.getGhostSpeedInTunnel(level);
			} else if (content == GhostHouse) {
				ghost.speed = Game.getGhostSpeedInHouse();
			} else if (ghost.control.inState(Frightened)) {
				ghost.speed = Game.getGhostSpeedWhenFrightened(level);
			} else {
				ghost.speed = Game.getGhostSpeedNormal(level);
			}
		}

		public GhostAttackControl() {
			super("GhostAttack", new EnumMap<>(GhostAttackState.class));

			state(GhostAttackState.Starting).update = state -> {
				changeTo(GhostAttackState.Scattering);
			};

			state(GhostAttackState.Scattering).entry = state -> {
				trace();
				state.setDuration(getScatteringDurationFrames());
				ghosts.forEach(ghost -> {
					ghost.receive(GhostMessage.StartScattering);
				});
			};

			state(GhostAttackState.Scattering).update = state -> {
				if (state.isTerminated()) {
					changeTo(GhostAttackState.Attacking);
				} else {
					ghosts.forEach(this::updateGhostSpeed);
					Game.entities.all().forEach(GameEntity::update);
				}
			};

			state(GhostAttackState.Attacking).entry = state -> {
				trace();
				state.setDuration(getAttackingDurationFrames());
				ghosts.forEach(ghost -> {
					ghost.receive(GhostMessage.StartChasing);
				});
				Game.assets.sound("sfx/waza.mp3").loop();
			};

			state(GhostAttackState.Attacking).update = state -> {
				if (state.isTerminated()) {
					changeTo(GhostAttackState.Over);
				} else {
					ghosts.forEach(this::updateGhostSpeed);
					Game.entities.all().forEach(GameEntity::update);
				}
			};

			state(GhostAttackState.Over).entry = state -> {
				Game.assets.sound("sfx/waza.mp3").stop();
			};
		}
	}

	// State machine for controlling the game play

	public enum PlayState {
		Initializing, Ready, StartingLevel, Playing, Crashing, GameOver
	}

	private class PlayControl extends StateMachine<PlayState> {

		public PlayControl() {
			super("Play control", new EnumMap<>(PlayState.class));

			// Initializing

			state(PlayState.Initializing).entry = state -> {
				Game.assets.sound("sfx/insert-coin.mp3").play();
				lives = 3;
				score = 0;
				bonusCollection.clear();
				nextGhostPoints = 0;
				Game.entities.removeAll(GameEntity.class);
				createPacManAndGhosts();
				Game.applyTheme();
				level = 1;
				initLevel();
			};

			state(PlayState.Initializing).update = state -> {
				if (!Game.assets.sound("sfx/insert-coin.mp3").isRunning()) {
					changeTo(PlayState.Ready);
				}
			};

			// Ready

			state(PlayState.Ready).entry = state -> {
				ghosts.forEach(ghost -> {
					ghost.setAnimated(true);
					ghost.speed = Game.getGhostSpeedInHouse();
				});
			};

			state(PlayState.Ready).update = state -> {
				if (Keyboard.pressedOnce(VK_ENTER)) {
					changeTo(PlayState.StartingLevel);
				}
			};

			// StartingLevel

			state(PlayState.StartingLevel).entry = state -> {
				Game.assets.sound("sfx/ready.mp3").play();
			};

			state(PlayState.StartingLevel).update = state -> {
				Game.entities.all().forEach(GameEntity::update);
				if (!Game.assets.sound("sfx/ready.mp3").isRunning()) {
					changeTo(PlayState.Playing);
				}
			};

			// Playing

			state(PlayState.Playing).entry = state -> {
				Game.selectedTheme().getEnergizer().setAnimated(true);
				pacMan.speed = Game.getPacManSpeed(level);
				ghosts.forEach(ghost -> ghost.speed = Game.getGhostSpeedNormal(level));
				pacMan.control.changeTo(PacManState.Eating);
				attackControl.changeTo(GhostAttackState.Starting);
				Game.assets.sound("sfx/eating.mp3").loop();
			};

			state(PlayState.Playing).update = state -> {
				if (board.count(Pellet) == 0 && board.count(Energizer) == 0) {
					attackControl.changeTo(GhostAttackState.Over);
					++level;
					initLevel();
					changeTo(PlayState.StartingLevel);
				} else if (attackControl.inState(GhostAttackState.Over)) {
					attackControl.changeTo(GhostAttackState.Starting, newState -> ++wave);
				} else {
					attackControl.update();
				}
				if (isBonusEnabled() && --bonusTimeRemaining <= 0) {
					setBonusEnabled(false);
				}
			};

			state(PlayState.Playing).exit = state -> {
				Game.assets.sound("sfx/eating.mp3").stop();
				Game.entities.removeAll(FlashText.class);
			};

			// Crashing

			state(PlayState.Crashing).entry = state -> {
				Game.assets.sounds().forEach(Sound::stop);
				Game.assets.sound("sfx/die.mp3").play();
				Game.selectedTheme().getEnergizer().setAnimated(false);
				setBonusEnabled(false);
				attackControl.changeTo(GhostAttackState.Over);
				Application.Log.info("PacMan crashed, lives remaining: " + lives);
			};

			state(PlayState.Crashing).update = state -> {
				if (!Game.assets.sound("sfx/die.mp3").isRunning()) {
					changeTo(lives > 0 ? PlayState.Playing : PlayState.GameOver);
				}
			};

			state(PlayState.Crashing).exit = state -> {
				pacMan.init();
				ghosts.forEach(Ghost::init);
			};

			// GameOver

			state(PlayState.GameOver).entry = state -> {
				if (score > highscore.getPoints()) {
					highscore.save(score, level);
					highscore.load();
				}
				Game.entities.all().forEach(entity -> entity.setAnimated(false));
				Application.Log.info("Game over.");
			};

			state(PlayState.GameOver).update = state -> {
				if (Keyboard.pressedOnce(VK_SPACE)) {
					changeTo(PlayState.Initializing);
				}
			};
		}
	}

	// State machines
	private final StateMachine<PlayState> playControl = new PlayControl();
	private final StateMachine<GhostAttackState> attackControl = new GhostAttackControl();

	// Entities
	private PacMan pacMan;
	private Ghost blinky, inky, pinky, clyde;
	private List<Ghost> ghosts;

	// Scene-specific data
	private Board board;
	private int level;
	private int wave;
	private int lives;
	private int score;
	private Highscore highscore;
	private List<BonusSymbol> bonusCollection;
	private int bonusTimeRemaining;
	private int nextGhostPoints;
	private int ghostsEatenAtLevel;

	public PlayScene() {
		super(Game);
	}

	@Override
	public void init() {
		board = new Board(Game.assets.text("board.txt").split("\n"));
		highscore = new Highscore("pacman-hiscore.txt");
		bonusCollection = new ArrayList<>();
		playControl.changeTo(PlayState.Initializing);
	}

	@Override
	public void update() {
		// cheats and debug keys
		if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_I)) {
			Game.settings.set("drawInternals", !Game.settings.getBool("drawInternals"));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_G)) {
			Game.settings.set("drawGrid", !Game.settings.getBool("drawGrid"));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_L)) {
			lives += 1;
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_B)) {
			bonusCollection.add(Game.getBonusSymbol(level));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_P)) {
			board.tilesWithContent(Pellet).forEach(tile -> board.setContent(tile, TileContent.None));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_E)) {
			board.tilesWithContent(Energizer).forEach(tile -> board.setContent(tile, TileContent.None));
		} else if (Keyboard.pressedOnce(KeyEvent.VK_ALT, KeyEvent.VK_T)) {
			Game.nextTheme();
		}
		playControl.update();
	}

	@Override
	public void draw(Graphics2D g) {
		final PacManUI theme = Game.selectedTheme();

		// Board & content
		drawSprite(g, 3, 0, theme.getBoard());
		range(4, NUM_ROWS - 3).forEach(row -> range(0, NUM_COLS).forEach(col -> {
			if (board.contains(row, col, Pellet)) {
				drawSprite(g, row, col, theme.getPellet());
			} else if (board.contains(row, col, Energizer)) {
				drawSprite(g, row, col, theme.getEnergizer());
			} else if (board.contains(row, col, Bonus)) {
				BonusSymbol symbol = Game.getBonusSymbol(level);
				drawSprite(g, row - .5f, col, theme.getBonus(symbol));
			}
		}));

		// Grid lines & internal state
		if (Game.settings.getBool("drawGrid")) {
			drawGridLines(g, getWidth(), getHeight());
		}
		if (Game.settings.getBool("drawInternals")) {
			// mark home positions of ghosts
			Game.entities.allOf(Ghost.class).forEach(ghost -> {
				g.setColor(ghost.color);
				g.fillRect(round(ghost.home.x * TILE_SIZE), round(ghost.home.y * TILE_SIZE), TILE_SIZE, TILE_SIZE);
			});
		}

		// Entities
		pacMan.draw(g);
		if (!playControl.inState(PlayState.Crashing)) {
			Game.entities.allOf(Ghost.class).forEach(ghost -> ghost.draw(g));
		}

		// HUD
		g.setColor(Color.LIGHT_GRAY);
		g.setFont(theme.getTextFont());
		drawText(g, 1, 1, "SCORE");
		drawText(g, 1, 8, "HIGH");
		drawText(g, 1, 12, "SCORE");
		g.setColor(theme.getHUDColor());
		drawText(g, 2, 1, String.format("%02d", score));
		drawText(g, 2, 8, String.format("%02d   L%d", highscore.getPoints(), highscore.getLevel()));
		drawText(g, 2, 20, "Level " + level);

		// Status messages
		switch (playControl.stateID()) {
		case Ready:
			g.setColor(Color.RED);
			drawTextCentered(g, getWidth(), 9.5f, "Press ENTER to start");
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
		range(0, lives).forEach(i -> drawSprite(g, NUM_ROWS - 2, 2 * (i + 1), theme.getLife()));

		// Bonus score
		float col = NUM_COLS - 2;
		for (BonusSymbol bonus : bonusCollection) {
			drawSprite(g, NUM_ROWS - 2, col, theme.getBonus(bonus));
			col -= 2f;
		}

		// Play state
		if (Game.settings.getBool("drawInternals")) {
			drawTextCentered(g, getWidth(), 33, playControl.stateID().name());
		}

		// Flash texts
		Game.entities.allOf(FlashText.class).forEach(text -> text.draw(g));
	}

	private void initLevel() {
		wave = 1;
		bonusTimeRemaining = 0;
		ghostsEatenAtLevel = 0;
		board.resetContent();
		Game.entities.all().forEach(GameEntity::init);
		Application.Log.info(String.format("Level %d: %d pellets and %d energizers. Frames/sec: %d", level,
				board.count(Pellet), board.count(Energizer), Game.gameLoop.getTargetFrameRate()));
	}

	private void createPacManAndGhosts() {

		pacMan = new PacMan(board, PACMAN_HOME);

		pacMan.onPelletFound = tile -> {
			Game.assets.sound("sfx/eat-pill.mp3").play();
			score(PacManGame.POINTS_FOR_PELLET);
			long pelletCount = board.count(Pellet);
			if (pelletCount == BONUS1_REMAINING_PELLETS || pelletCount == BONUS2_REMAINING_PELLETS) {
				setBonusEnabled(true);
			}
			pacMan.freeze(WAIT_TICKS_ON_EATING_PELLET);
			board.setContent(tile, TileContent.None);
		};

		pacMan.onEnergizerFound = tile -> {
			Game.assets.sound("sfx/eat-pill.mp3").play();
			score(POINTS_FOR_ENERGIZER);
			nextGhostPoints = FIRST_GHOST_POINTS;
			pacMan.freeze(WAIT_TICKS_ON_EATING_ENERGIZER);
			pacMan.startAttacking(Game.getGhostFrightenedDuration(level), Game.getPacManAttackingSpeed(level));
			board.setContent(tile, None);
		};

		pacMan.onBonusFound = tile -> {
			Game.assets.sound("sfx/eat-fruit.mp3").play();
			int points = Game.getBonusValue(level);
			score(points);
			bonusCollection.add(Game.getBonusSymbol(level));
			showFlashText(points, tile.getCol() * TILE_SIZE, tile.getRow() * TILE_SIZE);
			board.setContent(tile, None);
			setBonusEnabled(false);
		};

		pacMan.onGhostMet = ghost -> {
			if (ghost.control.inState(GhostState.Dead, Waiting, GhostState.Recovering)) {
				return;
			}
			if (pacMan.control.inState(PacManState.Frightening)) {
				Log.info("Pac-Man meets ghost " + ghost.getName());
				Game.assets.sound("sfx/eat-ghost.mp3").play();
				score(nextGhostPoints);
				showFlashText(nextGhostPoints, ghost.tr.getX(), ghost.tr.getY());
				nextGhostPoints *= 2;
				if (++ghostsEatenAtLevel == 16) {
					score(12000);
				}
				ghost.receive(GhostMessage.Die);
			} else {
				Log.info("Ghost " + ghost.getName() + " kills Pac-Man.");
				--lives;
				pacMan.control.changeTo(PacManState.Dying);
				playControl.changeTo(PlayState.Crashing);
			}
		};

		// Create the ghosts:

		blinky = new Ghost("Blinky", board, BLINKY_HOME);
		blinky.color = Color.RED;

		inky = new Ghost("Inky", board, INKY_HOME);
		inky.color = new Color(64, 224, 208);

		pinky = new Ghost("Pinky", board, PINKY_HOME);
		pinky.color = Color.PINK;

		clyde = new Ghost("Clyde", board, CLYDE_HOME);
		clyde.color = Color.ORANGE;

		ghosts = asList(blinky, inky, pinky, clyde);

		// Define common ghost behavior:

		ghosts.forEach(ghost -> {

			ghost.control.state(Waiting).entry = state -> {
				state.setDuration(getGhostWaitingDuration(ghost));
			};

			// if leaving ghost house and pacman is frightening, then become frightened:
			ghost.control.state(Waiting).exit = state -> {
				if (pacMan.control.inState(PacManState.Frightening)) {
					ghost.receive(GhostMessage.StartBeingFrightened);
				}
			};

			// define ghost state after end of frightening or recovering:
			ghost.stateAfterFrightened = () -> {
				if (attackControl.inState(GhostAttackState.Attacking)) {
					return Chasing;
				} else if (attackControl.inState(GhostAttackState.Scattering)) {
					return Scattering;
				} else {
					return ghost.control.stateID();
				}
			};

			// "frightened" state:

			ghost.control.state(Frightened).entry = state -> {
				ghost.speed = Game.getGhostSpeedWhenFrightened(level);
			};

			ghost.control.state(Frightened).update = state -> {
				ghost.moveRandomly();
			};

			ghost.control.state(Frightened).exit = state -> {
				ghost.speed = Game.getGhostSpeedNormal(level);
			};

			// "dead" state:

			ghost.control.state(Dead).update = state -> {
				ghost.enterRoute(ghost.home);
				if (ghost.isAtHome()) {
					ghost.control.changeTo(Recovering);
				}
			};

			// "recovering" state:

			ghost.control.state(Recovering).entry = state -> {
				state.setDuration(getGhostRecoveringDuration(ghost));
			};

			ghost.control.state(Recovering).update = state -> {
				if (state.isTerminated()) {
					ghost.control.changeTo(ghost.stateAfterFrightened.get());
				}
			};
		});

		// Define individual ghost behavior:

		// "Blinky", the red ghost

		// wait just before ghost house:
		blinky.control.state(Waiting).entry = state -> {
			blinky.placeAt(blinky.home);
			blinky.moveDir = W;
		};

		// loop around block at right upper corner of maze:
		blinky.control.state(Scattering, new GhostLoopingAroundWalls(blinky, 4, 26, S, true));

		// target Pac-Man's current position:
		blinky.control.state(Chasing).update = state -> {
			blinky.enterRoute(pacMan.currentTile());
		};

		// "Inky", the blue ghost

		// wait inside ghost house:
		inky.control.state(Waiting).entry = state -> {
			inky.placeAt(inky.home);
			inky.moveDir = Top4.N;
		};

		// bounce when waiting:
		inky.control.state(Waiting).update = state -> inky.moveBackAndForth();

		// loop around block at right lower corner of maze:
		inky.control.state(Scattering, new GhostLoopingAroundWalls(inky, 32, 26, W, true));

		// target tile in front of Pac-Man or target Pac-Man directly:
		inky.control.state(Chasing, new DirectOrProactiveChasing(inky, blinky, pacMan));

		// "Pinky", the pink ghost

		// wait inside ghost house:
		pinky.control.state(Waiting).entry = state -> {
			pinky.placeAt(pinky.home);
			pinky.moveDir = S;
		};

		// bounce when waiting:
		pinky.control.state(Waiting).update = state -> pinky.moveBackAndForth();

		// loop around block at right upper corner of maze:
		pinky.control.state(Scattering, new GhostLoopingAroundWalls(pinky, 4, 1, S, false));

		// target tile which is 4 tiles ahead of Pac-Man's current position:
		pinky.control.state(Chasing, new ProactiveChasing(pinky, pacMan, 4));

		// "Clyde", the yellow ghost

		// wait inside ghost house:
		clyde.control.state(Waiting).entry = state -> {
			clyde.placeAt(clyde.home);
			clyde.moveDir = Top4.N;
		};

		// bounce when waiting:
		clyde.control.state(Waiting).update = state -> clyde.moveBackAndForth();

		// loop around block at left lower corner of maze:
		clyde.control.state(Scattering, new GhostLoopingAroundWalls(clyde, 32, 1, E, false));

		// target Pac-Man's position if more than 8 tiles away, otherwise move randomly:
		clyde.control.state(Chasing).update = state -> {
			if (clyde.insideGhostHouse()) {
				clyde.enterRoute(GHOST_HOUSE_ENTRY);
			} else if (clyde.currentTile().distance(pacMan.currentTile()) > 8) {
				clyde.enterRoute(pacMan.currentTile());
			} else {
				clyde.moveRandomly();
			}
		};

		// add all to entity collection
		Game.entities.removeAll(PacMan.class);
		Game.entities.removeAll(Ghost.class);
		Game.entities.add(pacMan);
		Game.entities.add(blinky, inky, pinky, clyde);
	}

	private int getGhostRecoveringDuration(Ghost ghost) {
		return Game.gameLoop.secToFrames(2);
	}

	private int getGhostWaitingDuration(Ghost ghost) {
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
		return Game.gameLoop.secToFrames(seconds);
	}

	private void score(int points) {
		if (score < EXTRA_LIFE_SCORE && EXTRA_LIFE_SCORE <= score + points) {
			++lives;
			Game.assets.sound("sfx/extra-life.mp3").play();
		}
		score += points;
	}

	private boolean isBonusEnabled() {
		return board.getContent(BONUS_TILE) == Bonus;
	}

	private void setBonusEnabled(boolean enabled) {
		if (enabled) {
			board.setContent(BONUS_TILE, Bonus);
			bonusTimeRemaining = Game.gameLoop.secToFrames(9 + (float) Math.random());
		} else {
			board.setContent(BONUS_TILE, None);
			bonusTimeRemaining = 0;
		}
	}

	private void showFlashText(Object object, float x, float y) {
		if (x > getWidth() - 3 * TILE_SIZE) {
			x -= 3 * TILE_SIZE;
		}
		FlashText.show(Game, String.valueOf(object), Game.selectedTheme().getTextFont().deriveFont(Font.PLAIN, SPRITE_SIZE),
				Color.YELLOW, Game.gameLoop.secToFrames(1), new Vector2(x, y), new Vector2(0, -0.2f));
	}

}