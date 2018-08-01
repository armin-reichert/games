package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.pacman.model.TileContent.ENERGIZER;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.ViewController;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GhostContactEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.PacManDiedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLosesPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.GameInfo;
import de.amr.games.pacman.ui.HUD;
import de.amr.games.pacman.ui.MazeUI;
import de.amr.games.pacman.ui.Spritesheet;
import de.amr.games.pacman.ui.StatusUI;
import de.amr.games.pacman.ui.actor.Ghost;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateTransition;

public class PlayScene implements ViewController {

	public enum State {
		READY, PLAYING, GHOST_DYING, PACMAN_DYING, CHANGING_LEVEL, GAME_OVER
	};

	private final int width, height;
	private final StateMachine<State, GameEvent> gameControl;
	private final Game game;
	private final Maze maze;
	private final MazeUI mazeUI;
	private final HUD hud;
	private final StatusUI status;
	private final GameInfo gameInfo;

	public PlayScene(PacManApp app) {
		this.width = app.settings.width;
		this.height = app.settings.height;
		this.game = new Game(app.pulse::getFrequency);
		this.maze = app.maze;

		// UI
		mazeUI = new MazeUI(game, maze);
		hud = new HUD(game);
		status = new StatusUI(game);
		hud.tf.moveTo(0, 0);
		mazeUI.tf.moveTo(0, 3 * Spritesheet.TS);
		status.tf.moveTo(0, (3 + maze.numRows()) * Spritesheet.TS);

		// Game controller
		gameControl = createGameControl();
		gameControl.setLogger(LOG);
		mazeUI.eventing.subscribe(gameControl::enqueue);
		mazeUI.getPacMan().eventMgr.subscribe(gameControl::enqueue);
		mazeUI.getActiveGhosts().forEach(ghost -> ghost.eventMgr.subscribe(gameControl::enqueue));

		// Info
		gameInfo = new GameInfo(game, mazeUI, maze);
	}

	private StateMachine<State, GameEvent> createGameControl() {

		StateMachine<State, GameEvent> fsm = new StateMachine<>("GameController", State.class,
				State.READY);

		fsm.fnPulse = game.fnTicksPerSecond;

		// -- READY

		fsm.state(State.READY).entry = state -> {
			state.setDuration(game.sec(2));
			game.init(maze);
			mazeUI.initActors();
			mazeUI.enableAnimation(false);
			mazeUI.showInfo("Ready!", Color.YELLOW);
		};

		fsm.state(State.READY).exit = state -> {
			mazeUI.enableAnimation(true);
			mazeUI.hideInfo();
		};

		fsm.changeOnTimeout(State.READY, State.PLAYING);

		// -- PLAYING

		fsm.state(State.PLAYING).update = state -> mazeUI.update();

		fsm.changeOnInput(FoodFoundEvent.class, State.PLAYING, State.PLAYING, this::onFoodFound);

		fsm.changeOnInput(BonusFoundEvent.class, State.PLAYING, State.PLAYING, this::onBonusFound);

		fsm.changeOnInput(GhostContactEvent.class, State.PLAYING, State.PLAYING, this::onGhostContact);

		fsm.changeOnInput(GhostKilledEvent.class, State.PLAYING, State.GHOST_DYING,
				this::onGhostKilled);

		fsm.changeOnInput(PacManGainsPowerEvent.class, State.PLAYING, State.PLAYING,
				this::onPacManGainsPower);

		fsm.changeOnInput(PacManLosesPowerEvent.class, State.PLAYING, State.PLAYING,
				this::onPacManLosesPower);

		fsm.changeOnInput(PacManKilledEvent.class, State.PLAYING, State.PACMAN_DYING,
				this::onPacManKilled);

		fsm.changeOnInput(LevelCompletedEvent.class, State.PLAYING, State.CHANGING_LEVEL);

		// -- CHANGING_LEVEL

		fsm.state(State.CHANGING_LEVEL).entry = state -> {
			state.setDuration(game.getLevelChangingTime());
			mazeUI.setFlashing(true);
		};

		fsm.state(State.CHANGING_LEVEL).update = state -> {
			if (state.getRemaining() == state.getDuration() / 2) {
				nextLevel();
				mazeUI.showInfo("Ready!", Color.YELLOW);
				mazeUI.setFlashing(false);
				mazeUI.enableAnimation(false);
			} else if (state.isTerminated()) {
				mazeUI.hideInfo();
				mazeUI.enableAnimation(true);
			}
		};

		fsm.changeOnTimeout(State.CHANGING_LEVEL, State.PLAYING);

		// -- GHOST_DYING

		fsm.state(State.GHOST_DYING).entry = state -> {
			state.setDuration(game.getGhostDyingTime());
			mazeUI.getPacMan().visibility = () -> false;
		};

		fsm.state(State.GHOST_DYING).update = state -> {
			mazeUI.getActiveGhosts().forEach(Ghost::update);
		};

		fsm.changeOnTimeout(State.GHOST_DYING, State.PLAYING);

		fsm.state(State.GHOST_DYING).exit = state -> {
			mazeUI.getPacMan().visibility = () -> true;
		};

		// -- PACMAN_DYING

		fsm.state(State.PACMAN_DYING).entry = state -> {
			game.lives -= 1;
			mazeUI.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> false);
		};

		fsm.state(State.PACMAN_DYING).update = state -> {
			mazeUI.getPacMan().update();
		};

		fsm.changeOnInput(PacManDiedEvent.class, State.PACMAN_DYING, State.GAME_OVER,
				() -> game.lives == 0);

		fsm.changeOnInput(PacManDiedEvent.class, State.PACMAN_DYING, State.PLAYING,
				() -> game.lives > 0, t -> {
					mazeUI.initActors();
				});

		fsm.state(State.PACMAN_DYING).exit = state -> {
			mazeUI.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> true);
		};

		// -- GAME_OVER

		fsm.state(State.GAME_OVER).entry = state -> {
			mazeUI.enableAnimation(false);
			mazeUI.removeBonus();
			mazeUI.showInfo("Game Over!", Color.RED);
		};

		fsm.change(State.GAME_OVER, State.READY, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

		fsm.state(State.GAME_OVER).exit = state -> {
			mazeUI.hideInfo();
			game.init(maze);
		};

		return fsm;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void init() {
		gameControl.init();
		gameControl.getLogger().ifPresent(logger -> logger.setLevel(Level.INFO));
	}

	@Override
	public void update() {
		gameControl.update();
		gameInfo.update();
	}

	@Override
	public void draw(Graphics2D g) {
		hud.draw(g);
		mazeUI.draw(g);
		status.draw(g);
		gameInfo.draw(g);
	}

	private void nextLevel() {
		game.level += 1;
		game.foodEaten = 0;
		game.ghostIndex = 0;
		maze.resetFood();
		mazeUI.initActors();
	}

	// Game event handling

	@SuppressWarnings("unchecked")
	private <E extends GameEvent> E event(StateTransition<State, GameEvent> t) {
		return (E) t.event().get();
	}

	private void onGhostContact(StateTransition<State, GameEvent> t) {
		GhostContactEvent e = event(t);
		switch (e.ghost.getState()) {
		case AGGRO:
		case SAFE:
		case SCATTERING:
			gameControl.enqueue(new PacManKilledEvent(e.pacMan, e.ghost));
			break;
		case AFRAID:
		case BRAVE:
			gameControl.enqueue(new GhostKilledEvent(e.ghost));
			break;
		case DYING:
		case DEAD:
			// no event should be triggered by collision with ghost corpse
		default:
			throw new IllegalStateException();
		}
	}

	private void onPacManGainsPower(StateTransition<State, GameEvent> t) {
		PacManGainsPowerEvent e = event(t);
		e.pacMan.processEvent(e);
		mazeUI.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
	}

	private void onPacManLosesPower(StateTransition<State, GameEvent> t) {
		PacManLosesPowerEvent e = event(t);
		mazeUI.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
	}

	private void onPacManKilled(StateTransition<State, GameEvent> t) {
		PacManKilledEvent e = event(t);
		e.pacMan.processEvent(e);
		LOG.info(
				() -> String.format("PacMan killed by %s at %s", e.ghost.getName(), e.ghost.getTile()));
	}

	private void onGhostKilled(StateTransition<State, GameEvent> t) {
		GhostKilledEvent e = event(t);
		LOG.info(() -> String.format("Ghost %s killed at %s", e.ghost.getName(), e.ghost.getTile()));
		game.score += game.getGhostValue();
		game.ghostIndex += 1;
		e.ghost.processEvent(e);
	}

	private void onFoodFound(StateTransition<State, GameEvent> t) {
		FoodFoundEvent e = event(t);
		maze.clearTile(e.tile);
		game.foodEaten += 1;
		int oldGameScore = game.score;
		game.score += game.getFoodValue(e.food);
		if (oldGameScore < Game.EXTRALIFE_SCORE && game.score >= Game.EXTRALIFE_SCORE) {
			game.lives += 1;
		}
		if (game.foodEaten == game.foodTotal) {
			gameControl.enqueue(new LevelCompletedEvent());
			return;
		}
		if (game.foodEaten == Game.FOOD_EATEN_BONUS_1) {
			mazeUI.addBonus(game.getBonusSymbol(), game.getBonusValue(), game.sec(9));
		} else if (game.foodEaten == Game.FOOD_EATEN_BONUS_2) {
			mazeUI.addBonus(game.getBonusSymbol(), game.getBonusValue(), game.sec(9));
		}
		if (e.food == ENERGIZER) {
			game.ghostIndex = 0;
			gameControl
					.enqueue(new PacManGainsPowerEvent(mazeUI.getPacMan(), game.getPacManEmpoweringTime()));
		}
	}

	private void onBonusFound(StateTransition<State, GameEvent> t) {
		BonusFoundEvent e = event(t);
		LOG.info(() -> String.format("PacMan found bonus %s of value %d", e.symbol, e.value));
		game.score += e.value;
		mazeUI.honorAndRemoveBonus(game.sec(2));
	}
}