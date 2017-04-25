package de.amr.games.pacman.scenes;

import static de.amr.easy.game.Application.Assets;
import static de.amr.easy.game.Application.Entities;
import static de.amr.easy.game.Application.GameLoop;
import static de.amr.easy.game.Application.Log;
import static de.amr.easy.game.Application.Settings;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.PacManGame.Data;
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
import static de.amr.games.pacman.data.Board.PINKY_HOME_COL;
import static de.amr.games.pacman.data.Board.PINKY_HOME_ROW;
import static de.amr.games.pacman.data.TileContent.Energizer;
import static de.amr.games.pacman.data.TileContent.None;
import static de.amr.games.pacman.data.TileContent.Pellet;
import static de.amr.games.pacman.entities.ghost.GhostName.Blinky;
import static de.amr.games.pacman.entities.ghost.GhostName.Clyde;
import static de.amr.games.pacman.entities.ghost.GhostName.Inky;
import static de.amr.games.pacman.entities.ghost.GhostName.Pinky;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Frightened;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Waiting;
import static de.amr.games.pacman.ui.PacManUI.SPRITE_SIZE;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_B;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_T;
import static java.util.Arrays.asList;
import static java.util.stream.IntStream.range;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.common.FlashText;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Key;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.Bonus;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.entities.PacMan;
import de.amr.games.pacman.entities.PacMan.PacManState;
import de.amr.games.pacman.entities.PacManGameEntity;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.entities.ghost.GhostName;
import de.amr.games.pacman.entities.ghost.behaviors.DirectOrProactiveChasing;
import de.amr.games.pacman.entities.ghost.behaviors.GhostAction;
import de.amr.games.pacman.entities.ghost.behaviors.GhostLoopingAroundWalls;
import de.amr.games.pacman.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.entities.ghost.behaviors.ProactiveChasing;
import de.amr.games.pacman.fsm.StateMachine;
import de.amr.games.pacman.ui.PacManUI;

/**
 * The play scene of the Pac-Man game.
 * 
 * There are two inner classes representing state machines for controlling the attach waves of the
 * ghosts and the overall game state.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<PacManGame> {

	private enum AttackState {
		Starting, Scattering, Chasing, Complete
	}

	/**
	 * The state machine controlling the ghost attack waves.
	 *
	 */
	private class AttackControl extends StateMachine<AttackState> {

		public AttackControl() {
			super("Attack", new EnumMap<>(AttackState.class));

			state(AttackState.Starting).update = state -> {
				log();
				changeTo(AttackState.Scattering);
			};

			state(AttackState.Scattering).entry = state -> {
				state.setDuration(Data.getScatteringDuration());
				Entities.allOf(Ghost.class).forEach(ghost -> {
					ghost.perform(GhostAction.Scatter);
				});
				log();
			};

			state(AttackState.Scattering).update = state -> {
				if (state.isTerminated()) {
					state(AttackState.Chasing).setDuration(Data.getChasingDuration());
					changeTo(AttackState.Chasing);
				} else {
					Entities.all().forEach(GameEntity::update);
				}
			};

			state(AttackState.Chasing).entry = state -> {
				Entities.allOf(Ghost.class).forEach(ghost -> {
					ghost.perform(GhostAction.Chase);
				});
				Assets.sound("sfx/waza.mp3").loop();
				log();
			};

			state(AttackState.Chasing).update = state -> {
				if (state.isTerminated()) {
					changeTo(AttackState.Complete);
				} else {
					Entities.all().forEach(GameEntity::update);
				}
			};

			state(AttackState.Complete).entry = state -> Assets.sound("sfx/waza.mp3").stop();
		}

		void log() {
			Log.info(String.format("Level %d, wave %d: enter state %s for %d seconds", Data.levelNumber, Data.waveNumber,
					stateID(), GameLoop.framesToSec(state().getDuration())));
		}
	}

	private enum PlayState {
		StartingGame, StartPlaying, Playing, Crashing, GameOver
	}

	/**
	 * The state machine controlling the playing.
	 *
	 */
	private class PlayControl extends StateMachine<PlayState> {

		public PlayControl() {
			super("Play control", new EnumMap<>(PlayState.class));

			state(PlayState.StartingGame).entry = state -> {
				Data.newBoard();
				Data.initLevel();
				createPacManAndGhosts();
				applyTheme();
				Assets.sound("sfx/insert-coin.mp3").play();
			};

			state(PlayState.StartingGame).update = state -> {
				if (Assets.sound("sfx/insert-coin.mp3").isRunning()) {
					return;
				}
				if (Key.pressedOnce(VK_ENTER)) {
					changeTo(PlayState.StartPlaying, levelStart -> {
						levelStart.setDuration(Data.WaitTicksOnLevelStart);
						announceLevel();
					});
				} else if (Key.pressedOnce(VK_T)) {
					nextTheme();
				}
			};

			// --

			state(PlayState.StartPlaying).entry = state -> {
				selectedTheme().getEnergizer().setAnimated(true);
				Entities.findAny(PacMan.class).control.changeTo(PacManState.Waiting);
				Entities.allOf(Ghost.class).forEach(ghost -> {
					ghost.init();
					ghost.setAnimated(true);
				});
			};

			state(PlayState.StartPlaying).update = state -> {
				Entities.all().forEach(GameEntity::update);
				if (state.isTerminated()) {
					Entities.allOf(Ghost.class).forEach(ghost -> {
						int waitTicks = Data.getGhostWaitingDuration(GhostName.valueOf(ghost.getName()));
						ghost.control.state(GhostState.Waiting).setDuration(waitTicks);
					});
					changeTo(PlayState.Playing);
				}
			};

			// --

			state(PlayState.Playing).entry = state -> {
				PacMan pacMan = Entities.findAny(PacMan.class);
				pacMan.control.changeTo(PacManState.Exploring);
				pacMan.speed = Data.getPacManSpeed();
				attackControl.changeTo(AttackState.Starting);
				Assets.sound("sfx/eating.mp3").loop();
			};

			state(PlayState.Playing).update = state -> {
				handleCheats();
				if (Data.board.count(Pellet) == 0 && Data.board.count(Energizer) == 0) {
					attackControl.changeTo(AttackState.Complete);
					++Data.levelNumber;
					changeTo(PlayState.StartPlaying, levelStarting -> {
						levelStarting.setDuration(GameLoop.secToFrames(4));
						Data.initLevel();
						announceLevel();
					});
				} else if (attackControl.inState(AttackState.Complete)) {
					attackControl.changeTo(AttackState.Starting, newState -> ++Data.waveNumber);
				} else {
					attackControl.update();
				}
				Data.bonus.ifPresent(bonus -> {
					if (--Data.bonusTimeRemaining <= 0) {
						bonus(false);
					}
				});
			};

			state(PlayState.Playing).exit = state -> {
				Assets.sound("sfx/eating.mp3").stop();
				Entities.removeAll(FlashText.class);
			};

			// --

			state(PlayState.Crashing).entry = state -> {
				state.setDuration(GameLoop.secToFrames(3));
				Log.info("PacMan crashed, lives remaining: " + Data.liveCount);
				selectedTheme().getEnergizer().setAnimated(false);
				bonus(false);
				attackControl.changeTo(AttackState.Complete);
				Assets.sounds().forEach(Sound::stop);
				Assets.sound("sfx/die.mp3").play();
			};

			state(PlayState.Crashing).update = state -> {
				if (state.isTerminated()) {
					changeTo(PlayState.StartPlaying, newState -> newState.setDuration(0));
				}
			};

			state(PlayState.Crashing).exit = state -> {
				attackControl.changeTo(AttackState.Complete);
			};

			// --

			state(PlayState.GameOver).entry = state -> {
				Log.info("Game over.");
				Entities.all().forEach(entity -> entity.setAnimated(false));
				selectedTheme().getEnergizer().setAnimated(false);
				bonus(false);
				attackControl.changeTo(AttackState.Complete);
				if (Data.score > Data.highscore) {
					Data.highscore = Data.score;
					Data.saveHighscore();
				}
				Assets.sounds().forEach(Sound::stop);
				Assets.sound("sfx/die.mp3").play();
			};

			state(PlayState.GameOver).update = state -> {
				if (Key.pressedOnce(VK_SPACE)) {
					Entities.removeAll(GameEntity.class);
					changeTo(PlayState.StartingGame);
				}
			};
		}
	}

	private final StateMachine<AttackState> attackControl = new AttackControl();
	private final StateMachine<PlayState> playControl = new PlayControl();
	private int themeIndex;

	public PlayScene(PacManGame game) {
		super(game);
	}

	@Override
	public void init() {
		playControl.changeTo(PlayState.StartingGame);
	}

	@Override
	public void update() {
		if (Key.pressedOnce(VK_CONTROL, VK_I)) {
			Settings.set("drawInternals", !Settings.getBool("drawInternals"));
		} else if (Key.pressedOnce(KeyEvent.VK_CONTROL, KeyEvent.VK_G)) {
			Settings.set("drawGrid", !Settings.getBool("drawGrid"));
		}
		playControl.update();
	}

	private void createPacManAndGhosts() {

		// Create Pac-Man and define its event handlers

		final PacMan pacMan = new PacMan(new Tile(Board.PACMAN_HOME_ROW, Board.PACMAN_HOME_COL));

		pacMan.onPelletFound = tile -> {
			Data.board.setContent(tile, None);
			score(Data.PointsForPellet);
			long pelletCount = Data.board.count(Pellet);
			if (pelletCount == Data.PelletsLeftForBonus1 || pelletCount == Data.PelletsLeftForBonus2) {
				bonus(true);
			}
			pacMan.freeze(Data.WaitTicksOnEatingPellet);
			Assets.sound("sfx/eat-pill.mp3").play();
		};

		pacMan.onEnergizerFound = tile -> {
			Data.board.setContent(tile, None);
			score(Data.PointsForEnergizer);
			Data.ghostValue = Data.PointsForFirstGhost;
			pacMan.freeze(Data.WaitTicksOnEatingEnergizer);
			pacMan.startAttacking(Data.getGhostFrightenedDuration(), Data.getPacManAttackingSpeed());
			Assets.sound("sfx/eat-pill.mp3").play();
		};

		pacMan.onBonusFound = bonus -> {
			int points = Data.getBonusValue();
			score(points);
			Data.bonusScore.add(bonus);
			flash(points, BONUS_COL * TILE_SIZE, BONUS_ROW * TILE_SIZE);
			Assets.sound("sfx/eat-fruit.mp3").play();
		};

		pacMan.onGhostMet = ghost -> {
			if (ghost.control.inState(Dead, Waiting, Recovering)) {
				return;
			}
			if (pacMan.control.inState(PacManState.Frightening)) {
				Log.info("Pac-Man eats " + ghost.getName() + ".");
				Assets.sound("sfx/eat-ghost.mp3").play();
				score(Data.ghostValue);
				flash(Data.ghostValue, ghost.tr.getX(), ghost.tr.getY());
				Data.ghostValue *= 2;
				if (++Data.ghostsEatenAtLevel == 16) {
					score(12000);
				}
				ghost.perform(GhostAction.Die);
			} else {
				Log.info(ghost.getName() + " kills Pac-Man.");
				--Data.liveCount;
				pacMan.control.changeTo(PacManState.Dying);
				playControl.changeTo(Data.liveCount > 0 ? PlayState.Crashing : PlayState.GameOver);
			}
		};

		// Create the ghosts and define their behavior

		final Ghost blinky = new Ghost(Blinky, Color.RED, BLINKY_HOME_ROW, BLINKY_HOME_COL);

		final Ghost inky = new Ghost(Inky, new Color(64, 224, 208), INKY_HOME_ROW, INKY_HOME_COL);

		final Ghost pinky = new Ghost(Pinky, Color.PINK, PINKY_HOME_ROW, PINKY_HOME_COL);

		final Ghost clyde = new Ghost(Clyde, Color.ORANGE, CLYDE_HOME_ROW, CLYDE_HOME_COL);

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
				ghost.speed = Data.getGhostSpeedWhenFrightened();
			};

			ghost.control.state(Frightened).update = state -> {
				ghost.moveRandomly();
			};

			ghost.control.state(Frightened).exit = state -> {
				ghost.speed = Data.getGhostSpeedNormal();
			};

			// "dead" state

			ghost.control.state(Dead).update = state -> {
				ghost.walkHome();
				if (ghost.isAtHome()) {
					ghost.control.changeTo(Recovering);
				}
			};

			// "recovering" state

			ghost.control.state(Recovering).setDuration(Data.getGhostRecoveringDuration(GhostName.valueOf(ghost.getName())));

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

		// add entities into collection
		Entities.add(pacMan);
		Entities.add(blinky);
		Entities.add(inky);
		Entities.add(pinky);
		Entities.add(clyde);
	}

	private List<PacManUI> themes() {
		return Settings.get("themes");
	}

	private PacManUI selectedTheme() {
		return themes().get(themeIndex);
	}

	private void nextTheme() {
		if (++themeIndex == themes().size()) {
			themeIndex = 0;
		}
		applyTheme();
	}

	private void applyTheme() {
		Entities.allOf(PacManGameEntity.class).forEach(e -> e.setTheme(selectedTheme()));
		Entities.all().forEach(GameEntity::init);
		selectedTheme().getEnergizer().setAnimated(false);
	}

	private void handleCheats() {
		if (Key.pressedOnce(VK_ALT, VK_L)) {
			++Data.liveCount;
		}
		if (Key.pressedOnce(VK_ALT, VK_B)) {
			Data.bonusScore.add(Data.getBonus());
		}
	}

	private void announceLevel() {
		Assets.sound("sfx/ready.mp3").play();
		FlashText.show("Level " + Data.levelNumber, selectedTheme().getTextFont(), Color.YELLOW, GameLoop.secToFrames(0.5f),
				new Vector2(11, 21).times(TILE_SIZE), Vector2.nullVector());
	}

	private void flash(Object object, float x, float y) {
		if (x > getWidth() - 3 * TILE_SIZE) {
			x -= 3 * TILE_SIZE;
		}
		FlashText.show(String.valueOf(object), selectedTheme().getTextFont().deriveFont(Font.PLAIN, SPRITE_SIZE),
				Color.YELLOW, GameLoop.secToFrames(1), new Vector2(x, y), new Vector2(0, -0.2f));
	}

	private void bonus(boolean on) {
		if (on) {
			Data.bonus = Optional.of(Data.getBonus());
			Data.bonusTimeRemaining = GameLoop.secToFrames(9 + (float) Math.random());
		} else {
			Data.bonus = Optional.empty();
			Data.bonusTimeRemaining = 0;
		}
	}

	private void score(int points) {
		if (Data.score < Data.ScoreForExtraLife && Data.score + points >= Data.ScoreForExtraLife) {
			++Data.liveCount;
			Assets.sound("sfx/extra-life.mp3").play();
		}
		Data.score += points;
	}

	// --- drawing ---

	@Override
	public void draw(Graphics2D g) {
		drawBoard(g, 3);
		Entities.findAny(PacMan.class).draw(g);
		if (playControl.stateID() != PlayState.Crashing) {
			Entities.allOf(Ghost.class).forEach(ghost -> ghost.draw(g));
		}
		drawGameState(g);
		Entities.allOf(FlashText.class).forEach(text -> text.draw(g));
	}

	private void drawSpriteAt(Graphics2D g, float row, float col, Sprite sprite) {
		Graphics2D gg = (Graphics2D) g.create();
		gg.translate(TILE_SIZE * col, TILE_SIZE * row);
		sprite.draw(gg);
		gg.dispose();
	}

	private void drawTextAt(Graphics2D g, float row, float col, String text) {
		g.drawString(text, TILE_SIZE * col, TILE_SIZE * row);
	}

	private void drawTextCenteredAt(Graphics2D g, float row, String text) {
		g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2, TILE_SIZE * row);
	}

	private void drawBoard(Graphics2D g, int firstRow) {
		drawSpriteAt(g, firstRow, 0, selectedTheme().getBoard());

		range(firstRow + 1, NUM_ROWS - 3).forEach(row -> range(0, NUM_COLS).forEach(col -> {
			if (Data.board.contains(row, col, Pellet)) {
				drawSpriteAt(g, row, col, selectedTheme().getPellet());
			} else if (Data.board.contains(row, col, Energizer)) {
				drawSpriteAt(g, row, col, selectedTheme().getEnergizer());
			}
		}));

		Data.bonus.ifPresent(bonus -> drawSpriteAt(g, BONUS_ROW, BONUS_COL, selectedTheme().getBonus(bonus)));

		if (Settings.getBool("drawGrid")) {
			g.drawImage(gridLines(), 0, 0, null);
		}

		if (Settings.getBool("drawInternals")) {
			// mark home positions of ghosts
			Entities.allOf(Ghost.class).forEach(ghost -> {
				g.setColor(ghost.color);
				g.fillRect(Math.round(ghost.home.x * TILE_SIZE), Math.round(ghost.home.y * TILE_SIZE), TILE_SIZE, TILE_SIZE);
			});
		}
	}

	private void drawGameState(Graphics2D g) {
		g.setFont(selectedTheme().getTextFont());
		g.setColor(selectedTheme().getHUDColor());

		// HUD
		g.setColor(Color.LIGHT_GRAY);
		drawTextAt(g, 1, 1, "SCORE");
		drawTextAt(g, 1, 8, "HIGH");
		drawTextAt(g, 1, 12, "SCORE");
		g.setColor(selectedTheme().getHUDColor());
		drawTextAt(g, 2, 1, String.format("%02d", Data.score));
		drawTextAt(g, 2, 8, String.format("%02d   L%d", Data.highscore, Data.highscoreLevel));
		drawTextAt(g, 2, 20, "Level " + Data.levelNumber);

		// Ready!, Game Over!
		if (playControl.stateID() == PlayState.StartingGame) {
			g.setColor(Color.RED);
			drawTextCenteredAt(g, 9.5f, "Press ENTER to start");
			g.setColor(selectedTheme().getHUDColor());
			drawTextCenteredAt(g, 21f, "Ready!");
		} else if (playControl.stateID() == PlayState.GameOver) {
			g.setColor(Color.RED);
			drawTextCenteredAt(g, 9.5f, "Press SPACE for new game");
			g.setColor(selectedTheme().getHUDColor());
			drawTextCenteredAt(g, 21f, "Game Over!");
		}

		// Lives
		range(0, Data.liveCount).forEach(i -> drawSpriteAt(g, NUM_ROWS - 2, 2 * (i + 1), selectedTheme().getLife()));

		// Bonus score
		float col = NUM_COLS - 2;
		for (Bonus bonus : Data.bonusScore) {
			drawSpriteAt(g, NUM_ROWS - 2, col, selectedTheme().getBonus(bonus));
			col -= 2f;
		}

		if (Settings.getBool("drawInternals")) {
			drawTextCenteredAt(g, 33, playControl.stateID().toString());
		}
	}

	private Image gridLines;

	private Image gridLines() {
		if (gridLines == null) {
			gridLines = PacManUI.createTransparentImage(getWidth(), getHeight());
			Graphics g = gridLines.getGraphics();
			g.setColor(new Color(200, 200, 200, 100));
			for (int col = 1, x = TILE_SIZE; col < NUM_COLS; ++col, x += TILE_SIZE) {
				g.drawLine(x, 0, x, getHeight());
			}
			for (int row = 1, y = TILE_SIZE; row < NUM_ROWS; ++row, y += TILE_SIZE) {
				g.drawLine(0, y, getWidth(), y);
			}
		}
		return gridLines;
	}
}