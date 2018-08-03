package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOG;

import java.awt.Color;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.games.pacman.controller.GameController.State;
import de.amr.games.pacman.controller.event.core.GameEvent;
import de.amr.games.pacman.controller.event.game.BonusFoundEvent;
import de.amr.games.pacman.controller.event.game.FoodFoundEvent;
import de.amr.games.pacman.controller.event.game.GhostKilledEvent;
import de.amr.games.pacman.controller.event.game.LevelCompletedEvent;
import de.amr.games.pacman.controller.event.game.PacManDiedEvent;
import de.amr.games.pacman.controller.event.game.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.game.PacManKilledEvent;
import de.amr.games.pacman.controller.event.game.PacManLosesPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.TileContent;
import de.amr.games.pacman.ui.MazeUI;
import de.amr.games.pacman.ui.actor.Bonus;
import de.amr.games.pacman.ui.actor.Ghost;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateTransition;

public class GameController extends StateMachine<State, GameEvent> {

	public enum State {
		READY, PLAYING, GHOST_DYING, PACMAN_DYING, CHANGING_LEVEL, GAME_OVER
	};

	private final Game game;
	private final Maze maze;
	private final MazeUI mazeUI;

	public GameController(Game game, Maze maze, MazeUI mazeUI) {
		super("GameController", State.class, State.READY);

		this.game = game;
		this.maze = maze;
		this.mazeUI = mazeUI;

		mazeUI.eventMgr.subscribe(this::enqueue);
		mazeUI.getPacMan().eventMgr.subscribe(this::enqueue);
		mazeUI.getActiveGhosts().forEach(ghost -> ghost.eventMgr.subscribe(this::enqueue));

		fnPulse = game.fnTicksPerSecond;

		/*@formatter:off*/
		
		// -- READY

		state(State.READY).entry = state -> {
			state.setDuration(game.sec(2));
			game.init(maze);
			mazeUI.initActors();
			mazeUI.enableAnimation(false);
			mazeUI.showInfo("Ready!", Color.YELLOW);
		};

		state(State.READY).exit = state -> {
			mazeUI.enableAnimation(true);
			mazeUI.hideInfo();
		};

		state(State.READY).changeOnTimeout(State.PLAYING);

		// -- PLAYING

		state(State.PLAYING).update = state -> mazeUI.update();

		state(State.PLAYING)
			.onInput(FoodFoundEvent.class, this::onFoodFound)
			.onInput(BonusFoundEvent.class, this::onBonusFound)
			.onInput(PacManGhostCollisionEvent.class, this::onPacManGhostCollision)
			.onInput(PacManGainsPowerEvent.class, this::onPacManGainsPower)
			.onInput(PacManLosesPowerEvent.class, this::onPacManLosesPower)
			.onInput(PacManLostPowerEvent.class, this::onPacManLostPower)
			.changeOnInput(GhostKilledEvent.class, State.GHOST_DYING, this::onGhostKilled)
			.changeOnInput(PacManKilledEvent.class, State.PACMAN_DYING, this::onPacManKilled)
			.changeOnInput(LevelCompletedEvent.class, State.CHANGING_LEVEL);
			;

		// -- CHANGING_LEVEL

		state(State.CHANGING_LEVEL).entry = state -> {
			state.setDuration(game.getLevelChangingTime());
			mazeUI.setFlashing(true);
		};

		state(State.CHANGING_LEVEL).update = state -> {
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

		state(State.CHANGING_LEVEL).changeOnTimeout(State.PLAYING);

		// -- GHOST_DYING

		state(State.GHOST_DYING).entry = state -> {
			state.setDuration(game.getGhostDyingTime());
			mazeUI.getPacMan().visibility = () -> false;
		};

		state(State.GHOST_DYING).update = state -> {
			mazeUI.getActiveGhosts().filter(ghost -> ghost.getState() == Ghost.State.DYING).forEach(Ghost::update);
		};

		state(State.GHOST_DYING).changeOnTimeout(State.PLAYING);

		state(State.GHOST_DYING).exit = state -> {
			mazeUI.getPacMan().visibility = () -> true;
		};

		// -- PACMAN_DYING

		state(State.PACMAN_DYING).entry = state -> {
			game.lives -= 1;
			mazeUI.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> false);
		};

		state(State.PACMAN_DYING).update = state -> {
			mazeUI.getPacMan().update();
		};

		state(State.PACMAN_DYING)
			.changeOnInput(PacManDiedEvent.class, State.GAME_OVER, () -> game.lives == 0)
			.changeOnInput(PacManDiedEvent.class, State.PLAYING, () -> game.lives > 0, t -> mazeUI.initActors());

		state(State.PACMAN_DYING).exit = state -> {
			mazeUI.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> true);
		};

		// -- GAME_OVER

		state(State.GAME_OVER).entry = state -> {
			mazeUI.enableAnimation(false);
			mazeUI.removeBonus();
			mazeUI.showInfo("Game Over!", Color.RED);
		};

		state(State.GAME_OVER).change(State.READY, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

		state(State.GAME_OVER).exit = state -> {
			mazeUI.hideInfo();
			game.init(maze);
		};
		
		/*@formatter:on*/
	}

	private void nextLevel() {
		game.level += 1;
		game.foodEaten = 0;
		game.ghostIndex = 0;
		maze.resetFood();
		mazeUI.initActors();
	}

	// Game event handling

	private void onPacManGhostCollision(StateTransition<State, GameEvent> t) {
		PacManGhostCollisionEvent e = t.typedEvent();
		switch (e.ghost.getState()) {
		case AGGRO:
		case SAFE:
		case SCATTERING:
			enqueue(new PacManKilledEvent(e.ghost));
			break;
		case AFRAID:
			enqueue(new GhostKilledEvent(e.ghost));
			break;
		case DYING:
		case DEAD:
			// no event should occur for collision with ghost corpse
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void onPacManKilled(StateTransition<State, GameEvent> t) {
		PacManKilledEvent e = t.typedEvent();
		mazeUI.getPacMan().processEvent(e);
		LOG.info(() -> String.format("PacMan killed by %s at %s", e.killer.getName(), e.killer.getTile()));
	}

	private void onGhostKilled(StateTransition<State, GameEvent> t) {
		GhostKilledEvent e = t.typedEvent();
		e.ghost.processEvent(e);
		LOG.info(() -> String.format("Ghost %s killed at %s", e.ghost.getName(), e.ghost.getTile()));
	}

	private void onFoodFound(StateTransition<State, GameEvent> t) {
		FoodFoundEvent e = t.typedEvent();
		maze.clearTile(e.tile);
		game.foodEaten += 1;
		int oldGameScore = game.score;
		game.score += game.getFoodValue(e.food);
		if (oldGameScore < Game.EXTRALIFE_SCORE && game.score >= Game.EXTRALIFE_SCORE) {
			game.lives += 1;
		}
		if (game.foodEaten == game.foodTotal) {
			enqueue(new LevelCompletedEvent());
			return;
		}
		if (game.foodEaten == Game.FOOD_EATEN_BONUS_1 || game.foodEaten == Game.FOOD_EATEN_BONUS_2) {
			mazeUI.addBonus(new Bonus(game.getBonusSymbol(), game.getBonusValue()), game.getBonusTime());
		}
		if (e.food == TileContent.ENERGIZER) {
			game.ghostIndex = 0;
			enqueue(new PacManGainsPowerEvent());
		}
	}

	private void onPacManGainsPower(StateTransition<State, GameEvent> t) {
		PacManGainsPowerEvent e = t.typedEvent();
		mazeUI.getPacMan().processEvent(e);
		mazeUI.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
	}

	private void onPacManLosesPower(StateTransition<State, GameEvent> t) {
		PacManLosesPowerEvent e = t.typedEvent();
		mazeUI.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
	}

	private void onPacManLostPower(StateTransition<State, GameEvent> t) {
		PacManLostPowerEvent e = t.typedEvent();
		mazeUI.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
	}

	private void onBonusFound(StateTransition<State, GameEvent> t) {
		BonusFoundEvent e = t.typedEvent();
		LOG.info(() -> String.format("PacMan found bonus %s of value %d", e.symbol, e.value));
		game.score += e.value;
		mazeUI.consumeBonusAfter(game.sec(2));
	}
}