package de.amr.games.pacman.scenes;

import static de.amr.easy.game.Application.Assets;
import static de.amr.easy.game.Application.Entities;
import static de.amr.easy.game.Application.GameLoop;
import static de.amr.easy.game.Application.Log;
import static de.amr.easy.game.Application.Settings;
import static de.amr.easy.grid.api.Direction.N;
import static de.amr.easy.grid.api.Direction.S;
import static de.amr.easy.grid.api.Direction.W;
import static de.amr.games.pacman.PacManGame.Data;
import static de.amr.games.pacman.data.Board.BlinkyHomeCol;
import static de.amr.games.pacman.data.Board.BlinkyHomeRow;
import static de.amr.games.pacman.data.Board.BonusCol;
import static de.amr.games.pacman.data.Board.BonusRow;
import static de.amr.games.pacman.data.Board.ClydeHomeCol;
import static de.amr.games.pacman.data.Board.ClydeHomeRow;
import static de.amr.games.pacman.data.Board.Cols;
import static de.amr.games.pacman.data.Board.Empty;
import static de.amr.games.pacman.data.Board.Energizer;
import static de.amr.games.pacman.data.Board.InkyHomeCol;
import static de.amr.games.pacman.data.Board.InkyHomeRow;
import static de.amr.games.pacman.data.Board.PacManHomeCol;
import static de.amr.games.pacman.data.Board.PacManHomeRow;
import static de.amr.games.pacman.data.Board.Pellet;
import static de.amr.games.pacman.data.Board.PinkyHomeCol;
import static de.amr.games.pacman.data.Board.PinkyHomeRow;
import static de.amr.games.pacman.data.Board.Rows;
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
import static de.amr.games.pacman.scenes.PlayScene.Play.Crashing;
import static de.amr.games.pacman.scenes.PlayScene.Play.GameOver;
import static de.amr.games.pacman.scenes.PlayScene.Play.Playing;
import static de.amr.games.pacman.scenes.PlayScene.Play.StartPlaying;
import static de.amr.games.pacman.scenes.PlayScene.Play.StartingGame;
import static de.amr.games.pacman.ui.PacManUI.SpriteSize;
import static de.amr.games.pacman.ui.PacManUI.TileSize;
import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_B;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_T;
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
import de.amr.easy.grid.api.Direction;
import de.amr.games.pacman.PacManGame;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.Bonus;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.entities.BasePacManEntity;
import de.amr.games.pacman.entities.PacMan;
import de.amr.games.pacman.entities.PacMan.PacManState;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.entities.ghost.GhostName;
import de.amr.games.pacman.entities.ghost.behaviors.DirectOrProactiveChasing;
import de.amr.games.pacman.entities.ghost.behaviors.GhostAction;
import de.amr.games.pacman.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.entities.ghost.behaviors.ProactiveChasing;
import de.amr.games.pacman.entities.ghost.behaviors.RunningAroundTheBlock;
import de.amr.games.pacman.fsm.StateMachine;
import de.amr.games.pacman.ui.PacManUI;

/**
 * The play scene of the Pac-Man game.
 */
public class PlayScene extends Scene<PacManGame> {

	enum Attack {
		Starting, Scattering, Chasing, Complete
	}

	class AttackStateMachine extends StateMachine<Attack> {

		AttackStateMachine() {
			super("Attack", new EnumMap<>(Attack.class));

			state(Attack.Starting).update = state -> {
				log();
				changeTo(Attack.Scattering);
			};

			state(Attack.Scattering).entry = state -> {
				state.setDuration(Data.getScatteringDuration());
				Entities.allOf(Ghost.class).forEach(ghost -> {
					ghost.perform(GhostAction.Scatter);
				});
				log();
			};

			state(Attack.Scattering).update = state -> {
				if (state.isTerminated()) {
					state(Attack.Chasing).setDuration(Data.getChasingDuration());
					changeTo(Attack.Chasing);
				} else {
					Entities.all().forEach(GameEntity::update);
				}
			};

			state(Attack.Chasing).entry = state -> {
				Entities.allOf(Ghost.class).forEach(ghost -> {
					ghost.perform(GhostAction.Chase);
				});
				Assets.sound("sfx/waza.mp3").loop();
				log();
			};

			state(Attack.Chasing).update = state -> {
				if (state.isTerminated()) {
					changeTo(Attack.Complete);
				}
				Entities.all().forEach(GameEntity::update);
			};

			state(Attack.Complete).entry = state -> Assets.sound("sfx/waza.mp3").stop();
		}

		void log() {
			Log.info(String.format("Level %d, wave %d: enter state %s for %d seconds", Data.levelNumber,
					Data.waveNumber, stateID(), GameLoop.framesToSec(state().getDuration())));
		}
	}

	enum Play {
		StartingGame, StartPlaying, Playing, Crashing, GameOver
	}

	class PlayStateMachine extends StateMachine<Play> {

		PlayStateMachine() {
			super("Play control", new EnumMap<>(Play.class));

			state(StartingGame).entry = state -> {
				Data.init(new Board(Assets.text("board.txt")));
				Data.initLevel();
				createEntities();
				applyTheme();
				Assets.sound("sfx/insert-coin.mp3").play();
			};

			state(StartingGame).update = state -> {
				if (Assets.sound("sfx/insert-coin.mp3").isRunning()) {
					return;
				}
				if (Key.pressedOnce(VK_ENTER)) {
					changeTo(StartPlaying, levelStart -> {
						levelStart.setDuration(Data.WaitTicksOnLevelStart);
						announceLevel();
					});
				} else if (Key.pressedOnce(VK_T)) {
					nextTheme();
				}
			};

			// --

			state(StartPlaying).entry = state -> {
				selectedTheme().getEnergizer().setAnimated(true);
				Entities.findAny(PacMan.class).control.changeTo(PacManState.Waiting);
				Entities.allOf(Ghost.class).forEach(ghost -> {
					ghost.init();
					ghost.setAnimated(true);
				});
			};

			state(StartPlaying).update = state -> {
				Entities.allOf(Ghost.class).forEach(Ghost::update);
				if (state.isTerminated()) {
					Entities.allOf(Ghost.class).forEach(ghost -> {
						int waitTicks = Data.getGhostWaitingDuration(GhostName.valueOf(ghost.getName()));
						ghost.control.state(GhostState.Waiting).setDuration(waitTicks);
						changeTo(Playing);
					});
				}
			};

			// --

			state(Playing).entry = state -> {
				Entities.findAny(PacMan.class).control.changeTo(PacManState.Exploring);
				Entities.findAny(PacMan.class).speed = Data.getPacManSpeed();
				attack.changeTo(Attack.Starting);
				Assets.sound("sfx/eating.mp3").loop();
			};

			state(Playing).update = state -> {
				handleCheats();
				if (Data.board.count(Pellet) == 0 && Data.board.count(Energizer) == 0) {
					attack.changeTo(Attack.Complete);
					++Data.levelNumber;
					changeTo(StartPlaying, levelStarting -> {
						levelStarting.setDuration(GameLoop.secToFrames(4));
						Data.initLevel();
						announceLevel();
					});
				} else if (attack.inState(Attack.Complete)) {
					attack.changeTo(Attack.Starting, newState -> ++Data.waveNumber);
				} else {
					attack.update();
				}
				Data.bonus.ifPresent(bonus -> {
					if (--Data.bonusTimeRemaining <= 0) {
						bonus(false);
					}
				});
			};

			state(Playing).exit = state -> {
				Assets.sound("sfx/eating.mp3").stop();
				Entities.removeAll(FlashText.class);
			};

			// --

			state(Crashing).entry = state -> {
				state.setDuration(GameLoop.secToFrames(3));
				Log.info("PacMan crashed, lives remaining: " + Data.liveCount);
				selectedTheme().getEnergizer().setAnimated(false);
				bonus(false);
				attack.changeTo(Attack.Complete);
				Assets.sounds().forEach(Sound::stop);
				Assets.sound("sfx/die.mp3").play();
			};

			state(Crashing).update = state -> {
				if (state.isTerminated()) {
					changeTo(StartPlaying, newState -> newState.setDuration(0));
				}
			};

			state(Crashing).exit = state -> {
				attack.changeTo(Attack.Complete);
			};

			// --

			state(GameOver).entry = state -> {
				Log.info("Game over.");
				Entities.all().forEach(entity -> entity.setAnimated(false));
				selectedTheme().getEnergizer().setAnimated(false);
				bonus(false);
				attack.changeTo(Attack.Complete);
				if (Data.score > Data.highscore) {
					Data.highscore = Data.score;
					Data.saveHighscore();
				}
				Assets.sounds().forEach(Sound::stop);
				Assets.sound("sfx/die.mp3").play();
			};

			state(GameOver).update = state -> {
				if (Key.pressedOnce(VK_SPACE)) {
					Entities.removeAll(GameEntity.class);
					changeTo(StartingGame);
				}
			};
		}
	}

	final StateMachine<Attack> attack = new AttackStateMachine();
	final StateMachine<Play> playState = new PlayStateMachine();
	int selectedTheme;

	public PlayScene(PacManGame game) {
		super(game);
	}

	@Override
	public void init() {
		playState.changeTo(StartingGame);
	}

	@Override
	public void update() {
		if (Key.pressedOnce(VK_CONTROL, VK_I)) {
			Settings.set("drawInternals", !Settings.getBool("drawInternals"));
		} else if (Key.pressedOnce(KeyEvent.VK_CONTROL, KeyEvent.VK_G)) {
			Settings.set("drawGrid", !Settings.getBool("drawGrid"));
		}
		playState.update();
	}

	void createEntities() {

		// Pac-Man, the one and only

		final PacMan pacMan = new PacMan(new Tile(PacManHomeRow, PacManHomeCol));
		pacMan.setName("Pac-Man");
		Entities.add(pacMan);

		pacMan.pelletFound = tile -> {
			Data.board.setContent(tile, Empty);
			score(Data.PointsForPellet);
			long pelletCount = Data.board.count(Pellet);
			if (pelletCount == Data.PelletsLeftForBonus1 || pelletCount == Data.PelletsLeftForBonus2) {
				bonus(true);
			}
			pacMan.freeze(Data.WaitTicksOnEatingPellet);
			Assets.sound("sfx/eat-pill.mp3").play();
		};

		pacMan.energizerFound = tile -> {
			Data.board.setContent(tile, Empty);
			score(Data.PointsForEnergizer);
			Data.ghostValue = Data.PointsForFirstGhost;
			pacMan.freeze(Data.WaitTicksOnEatingEnergizer);
			pacMan.startAttacking(Data.getGhostFrightenedDuration(), Data.getPacManAttackingSpeed());
			Assets.sound("sfx/eat-pill.mp3").play();
		};

		pacMan.bonusFound = bonus -> {
			int points = Data.getBonusValue();
			score(points);
			Data.bonusScore.add(bonus);
			flash(points, BonusCol * TileSize, BonusRow * TileSize);
			Assets.sound("sfx/eat-fruit.mp3").play();
		};

		pacMan.ghostMet = ghost -> {
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
				playState.changeTo(Data.liveCount > 0 ? Crashing : GameOver);
			}
		};

		// Ghosts

		final Ghost blinky = new Ghost(Blinky, Color.RED, new Tile(BlinkyHomeRow, BlinkyHomeCol));
		final Ghost inky = new Ghost(Inky, new Color(64, 224, 208), new Tile(InkyHomeRow, InkyHomeCol));
		final Ghost pinky = new Ghost(Pinky, Color.PINK, new Tile(PinkyHomeRow, PinkyHomeCol));
		final Ghost clyde = new Ghost(Clyde, Color.ORANGE, new Tile(ClydeHomeRow, ClydeHomeCol));
		Entities.add(blinky, inky, pinky, clyde);

		// Common ghost behavior
		Entities.allOf(Ghost.class).forEach(ghost -> {

			// state to restore after frightening or recovering ends
			ghost.stateAfterFrightened = () -> {
				if (attack.inState(Attack.Chasing)) {
					return Chasing;
				} else if (attack.inState(Attack.Scattering)) {
					return Scattering;
				} else {
					return ghost.control.stateID();
				}
			};

			ghost.control.state(Frightened).entry = state -> {
				ghost.speed = Data.getGhostSpeedWhenFrightened();
			};
			ghost.control.state(Frightened).update = state -> {
				ghost.moveRandomly();
			};
			ghost.control.state(Frightened).exit = state -> {
				ghost.speed = Data.getGhostSpeedNormal();
			};

			ghost.control.state(Dead).update = state -> {
				ghost.walkHome();
				if (ghost.isAtHome()) {
					ghost.control.changeTo(Recovering);
				}
			};

			ghost.control.state(Recovering)
					.setDuration(Data.getGhostRecoveringDuration(GhostName.valueOf(ghost.getName())));

			ghost.control.state(Recovering).update = state -> {
				if (state.isTerminated()) {
					ghost.control.changeTo(ghost.stateAfterFrightened.get());
				}
			};
		});

		// "Blinky"-specific behavior

		blinky.control.state(Waiting).entry = state -> {
			blinky.placeAt(blinky.home);
			blinky.moveDir = W;
		};

		blinky.control.state(Scattering, new RunningAroundTheBlock(blinky, 4, 26, S, true));

		blinky.control.state(Chasing).update = state -> {
			blinky.followRoute(pacMan.currentTile());
		};

		// "Inky"-specific behavior

		inky.control.state(Waiting).entry = state -> {
			inky.placeAt(inky.home);
			inky.moveDir = N;
		};
		inky.control.state(Waiting).update = state -> inky.moveBackAndForth();

		inky.control.state(Scattering, new RunningAroundTheBlock(inky, 32, 26, W, true));

		inky.control.state(Chasing, new DirectOrProactiveChasing(inky, blinky, pacMan));

		// "Pinky"-specific behavior

		pinky.control.state(Waiting).entry = state -> {
			pinky.placeAt(pinky.home);
			pinky.moveDir = S;
		};
		pinky.control.state(Waiting).update = state -> pinky.moveBackAndForth();

		pinky.control.state(Scattering, new RunningAroundTheBlock(pinky, 4, 1, S, false));

		pinky.control.state(Chasing, new ProactiveChasing(pinky, pacMan, 4));

		// "Clyde"-specific behavior

		clyde.control.state(Waiting).entry = state -> {
			clyde.placeAt(clyde.home);
			clyde.moveDir = N;
		};
		clyde.control.state(Waiting).update = state -> clyde.moveBackAndForth();

		clyde.control.state(Scattering, new RunningAroundTheBlock(clyde, 32, 1, Direction.E, false));

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
	}

	List<PacManUI> themes() {
		return Settings.get("themes");
	}

	PacManUI selectedTheme() {
		return themes().get(selectedTheme);
	}

	void nextTheme() {
		if (++selectedTheme == themes().size()) {
			selectedTheme = 0;
		}
		applyTheme();
	}

	void applyTheme() {
		Entities.allOf(BasePacManEntity.class).forEach(e -> e.setTheme(selectedTheme()));
		Entities.all().forEach(GameEntity::init);
		selectedTheme().getEnergizer().setAnimated(false);
	}

	void handleCheats() {
		if (Key.pressedOnce(VK_ALT, VK_L)) {
			++Data.liveCount;
		}
		if (Key.pressedOnce(VK_ALT, VK_B)) {
			Data.bonusScore.add(Data.getBonus());
		}
	}

	void announceLevel() {
		Assets.sound("sfx/ready.mp3").play();
		FlashText.show("Level " + Data.levelNumber, selectedTheme().getTextFont(), Color.YELLOW,
				GameLoop.secToFrames(0.5f), new Vector2(11, 21).times(TileSize), Vector2.nullVector());
	}

	void flash(Object object, float x, float y) {
		if (x > getWidth() - 3 * TileSize) {
			x -= 3 * TileSize;
		}
		FlashText.show(String.valueOf(object),
				selectedTheme().getTextFont().deriveFont(Font.PLAIN, SpriteSize), Color.YELLOW,
				GameLoop.secToFrames(1), new Vector2(x, y), new Vector2(0, -0.2f));
	}

	void bonus(boolean on) {
		if (on) {
			Data.bonus = Optional.of(Data.getBonus());
			Data.bonusTimeRemaining = GameLoop.secToFrames(9 + (float) Math.random());
		} else {
			Data.bonus = Optional.empty();
			Data.bonusTimeRemaining = 0;
		}
	}

	void score(int points) {
		if (Data.score < Data.ScoreForExtraLife && Data.score + points >= Data.ScoreForExtraLife) {
			++Data.liveCount;
			Assets.sound("sfx/extra-life.mp3").play();
		}
		Data.score += points;
	}

	// --- drawing ---

	void drawSpriteAt(Graphics2D g, float row, float col, Sprite sprite) {
		Graphics2D gg = (Graphics2D) g.create();
		gg.translate(TileSize * col, TileSize * row);
		sprite.draw(gg);
		gg.dispose();
	}

	void drawTextAt(Graphics2D g, float row, float col, String text) {
		g.drawString(text, TileSize * col, TileSize * row);
	}

	void drawTextCenteredAt(Graphics2D g, float row, String text) {
		g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2, TileSize * row);
	}

	@Override
	public void draw(Graphics2D g) {
		drawBoard(g, 3);
		Entities.findAny(PacMan.class).draw(g);
		if (playState.stateID() != Crashing) {
			Entities.allOf(Ghost.class).forEach(ghost -> ghost.draw(g));
		}
		drawGameState(g);
		Entities.allOf(FlashText.class).forEach(text -> text.draw(g));
	}

	void drawBoard(Graphics2D g, int firstRow) {
		drawSpriteAt(g, firstRow, 0, selectedTheme().getBoard());

		range(firstRow + 1, Rows - 3).forEach(row -> range(0, Cols).forEach(col -> {
			if (Data.board.has(Pellet, row, col)) {
				drawSpriteAt(g, row, col, selectedTheme().getPellet());
			} else if (Data.board.has(Energizer, row, col)) {
				drawSpriteAt(g, row, col, selectedTheme().getEnergizer());
			}
		}));

		Data.bonus
				.ifPresent(bonus -> drawSpriteAt(g, BonusRow, BonusCol, selectedTheme().getBonus(bonus)));

		if (Settings.getBool("drawGrid")) {
			g.drawImage(gridLines(), 0, 0, null);
		}

		if (Settings.getBool("drawInternals")) {
			// mark home positions of ghosts
			Entities.allOf(Ghost.class).forEach(ghost -> {
				g.setColor(ghost.color);
				g.fillRect(Math.round(ghost.home.x * TileSize), Math.round(ghost.home.y * TileSize),
						TileSize, TileSize);
			});
		}
	}

	void drawGameState(Graphics2D g) {
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
		if (playState.stateID() == StartingGame) {
			g.setColor(Color.RED);
			drawTextCenteredAt(g, 9.5f, "Press ENTER to start");
			g.setColor(selectedTheme().getHUDColor());
			drawTextCenteredAt(g, 21f, "Ready!");
		} else if (playState.stateID() == GameOver) {
			g.setColor(Color.RED);
			drawTextCenteredAt(g, 9.5f, "Press SPACE for new game");
			g.setColor(selectedTheme().getHUDColor());
			drawTextCenteredAt(g, 21f, "Game Over!");
		}

		// Lives
		range(0, Data.liveCount)
				.forEach(i -> drawSpriteAt(g, Rows - 2, 2 * (i + 1), selectedTheme().getLife()));

		// Bonus score
		float col = Cols - 2;
		for (Bonus bonus : Data.bonusScore) {
			drawSpriteAt(g, Rows - 2, col, selectedTheme().getBonus(bonus));
			col -= 2f;
		}

		if (Settings.getBool("drawInternals")) {
			drawTextCenteredAt(g, 33, playState.stateID().toString());
		}
	}

	Image gridLines;

	Image gridLines() {
		if (gridLines == null) {
			gridLines = PacManUI.createTransparentImage(getWidth(), getHeight());
			Graphics g = gridLines.getGraphics();
			g.setColor(new Color(200, 200, 200, 100));
			for (int col = 1, x = TileSize; col < Cols; ++col, x += TileSize) {
				g.drawLine(x, 0, x, getHeight());
			}
			for (int row = 1, y = TileSize; row < Rows; ++row, y += TileSize) {
				g.drawLine(0, y, getWidth(), y);
			}
		}
		return gridLines;
	}
}