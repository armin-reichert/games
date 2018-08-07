package de.amr.games.pacman.controller;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.pacman.controller.GameController.State.CHANGING_LEVEL;
import static de.amr.games.pacman.controller.GameController.State.GAME_OVER;
import static de.amr.games.pacman.controller.GameController.State.GHOST_DYING;
import static de.amr.games.pacman.controller.GameController.State.PACMAN_DYING;
import static de.amr.games.pacman.controller.GameController.State.PLAYING;
import static de.amr.games.pacman.controller.GameController.State.READY;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pacman.actor.Bonus;
import de.amr.games.pacman.actor.GameActors;
import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.actor.PacMan;
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
import de.amr.games.pacman.model.Content;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.ui.GameUI;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;
import de.amr.statemachine.StateTransition;

public class GameController implements Controller {

	public enum State {
		READY, PLAYING, GHOST_DYING, PACMAN_DYING, CHANGING_LEVEL, GAME_OVER
	};

	private final Game game;
	private final GameActors actors;
	private final GameUI gameUI;
	private StateMachine<State, GameEvent> sm;

	public GameController(Game game, GameActors actors, GameUI gameUI) {
		this.game = game;
		this.actors = actors;
		this.gameUI = gameUI;
	}

	@Override
	public View currentView() {
		return gameUI;
	}

	@Override
	public void init() {
		sm = buildStateMachine();
		sm.fnPulse = game.fnTicksPerSecond;
		sm.init();
		actors.addEventHandler(sm::enqueue);
		actors.getPacMan().setEnvironment(gameUI.mazeUI);
	}

	@Override
	public void update() {
		sm.update();
		gameUI.update();
	}

	public void setLogger(Logger log) {
		sm.traceTo(log);
	}

	private PlayingState playingState() {
		return sm.state(PLAYING);
	}

	private StateMachine<State, GameEvent> buildStateMachine() {
		/*@formatter:off*/
		return StateMachine.builder(State.class, GameEvent.class)
			.description("[GameController]")
			.initialState(State.READY)
		
			.states()
				.state(READY).impl(new ReadyState()).duration(game::getReadyTime).build()
				.state(PLAYING).impl(new PlayingState()).build()
				.state(CHANGING_LEVEL).impl(new ChangingLevelState()).duration(game::getLevelChangingTime).build()
				.state(GHOST_DYING).impl(new GhostDyingState()).duration(game::getGhostDyingTime).build()
				.state(PACMAN_DYING).impl(new PacManDyingState()).build()
				.state(GAME_OVER).impl(new GameOverState()).build()
			
			.transitions()
				.change(READY, PLAYING)
					.onTimeout()
					.build()
				.keep(PLAYING)
					.on(FoodFoundEvent.class)
					.act(t -> playingState().onFoodFound(t))
					.build()
				.keep(PLAYING)
					.on(BonusFoundEvent.class)
					.act(t -> playingState().onBonusFound(t))
					.build()
				.keep(PLAYING)
					.on(PacManGhostCollisionEvent.class)
					.act(t -> playingState().onPacManGhostCollision(t))
					.build()
				.keep(PLAYING)
					.on(PacManGainsPowerEvent.class)
					.act(t -> playingState().onPacManGainsPower(t))
					.build()
				.keep(PLAYING)
					.on(PacManLosesPowerEvent.class)
					.act(t -> playingState().onPacManLosesPower(t))
					.build()
				.keep(PLAYING)
					.on(PacManLostPowerEvent.class)
					.act(t -> playingState().onPacManLostPower(t))
					.build()
				.change(PLAYING, GHOST_DYING)
					.on(GhostKilledEvent.class)
					.act(t -> playingState().onGhostKilled(t))
					.build()
				.change(PLAYING, PACMAN_DYING)
					.on(PacManKilledEvent.class)
					.act(t -> playingState().onPacManKilled(t))
					.build()
				.change(PLAYING, CHANGING_LEVEL)
					.on(LevelCompletedEvent.class)
					.build()
				.change(State.CHANGING_LEVEL, PLAYING)
					.onTimeout()	
					.build()
				.change(GHOST_DYING, PLAYING)
					.onTimeout()
					.build()
				.change(PACMAN_DYING, State.GAME_OVER)
					.on(PacManDiedEvent.class)
					.when(() -> game.lives == 0)
					.build()
				.change(PACMAN_DYING, PLAYING)
					.on(PacManDiedEvent.class)
					.when(() -> game.lives > 0)
					.act(t -> actors.init())
					.build()
				.change(GAME_OVER, READY)
					.when(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
					.build()
		.buildStateMachine();
		/*@formatter:on*/
	}

	private class ReadyState extends StateObject<State, GameEvent> {

		@Override
		public void onEntry() {
			game.init();
			actors.init();
			gameUI.mazeUI.enableAnimation(false);
			gameUI.showInfo("Ready!", Color.YELLOW);
		}

		@Override
		public void onExit() {
			gameUI.mazeUI.enableAnimation(true);
			gameUI.hideInfo();
		}
	}

	private class PlayingState extends StateObject<State, GameEvent> {

		@Override
		public void onTick() {
			gameUI.mazeUI.update();
			actors.getPacMan().update();
			actors.getActiveGhosts().forEach(Ghost::update);
		}

		private void onPacManGhostCollision(StateTransition<State, GameEvent> t) {
			PacManGhostCollisionEvent e = t.typedEvent();
			PacMan.State pacManState = actors.getPacMan().getState();
			Ghost.State ghostState = e.ghost.getState();
			if (pacManState == PacMan.State.EMPOWERED) {
				if (ghostState == Ghost.State.AFRAID || ghostState == Ghost.State.AGGRO
						|| ghostState == Ghost.State.SCATTERING) {
					sm.enqueue(new GhostKilledEvent(e.ghost));
				}
				return;
			}
			if (pacManState == PacMan.State.DYING) {
				return;
			}
			sm.enqueue(new PacManKilledEvent(e.ghost));
		}

		private void onPacManKilled(StateTransition<State, GameEvent> t) {
			PacManKilledEvent e = t.typedEvent();
			actors.getPacMan().processEvent(e);
			LOG.info(() -> String.format("PacMan killed by %s at %s", e.killer.getName(), e.killer.getTile()));
		}

		private void onPacManGainsPower(StateTransition<State, GameEvent> t) {
			PacManGainsPowerEvent e = t.typedEvent();
			actors.getPacMan().processEvent(e);
			actors.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
		}

		private void onPacManLosesPower(StateTransition<State, GameEvent> t) {
			PacManLosesPowerEvent e = t.typedEvent();
			actors.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
		}

		private void onPacManLostPower(StateTransition<State, GameEvent> t) {
			PacManLostPowerEvent e = t.typedEvent();
			actors.getActiveGhosts().forEach(ghost -> ghost.processEvent(e));
		}

		private void onGhostKilled(StateTransition<State, GameEvent> t) {
			GhostKilledEvent e = t.typedEvent();
			e.ghost.processEvent(e);
			LOG.info(() -> String.format("Ghost %s killed at %s", e.ghost.getName(), e.ghost.getTile()));
		}

		private void onBonusFound(StateTransition<State, GameEvent> t) {
			BonusFoundEvent e = t.typedEvent();
			LOG.info(() -> String.format("PacMan found bonus %s of value %d", e.symbol, e.value));
			game.score += e.value;
			gameUI.mazeUI.honorBonusAndRemoveAfter(game.sec(2));
		}

		private void onFoodFound(StateTransition<State, GameEvent> t) {
			FoodFoundEvent e = t.typedEvent();
			game.maze.clearTile(e.tile);
			game.foodEaten += 1;
			int oldGameScore = game.score;
			game.score += game.getFoodValue(e.food);
			if (oldGameScore < Game.EXTRALIFE_SCORE && game.score >= Game.EXTRALIFE_SCORE) {
				game.lives += 1;
			}
			if (game.foodEaten == game.foodTotal) {
				sm.enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.foodEaten == Game.FOOD_EATEN_BONUS_1 || game.foodEaten == Game.FOOD_EATEN_BONUS_2) {
				gameUI.mazeUI.addBonus(new Bonus(game.getBonusSymbol(), game.getBonusValue()), game.getBonusTime());
			}
			if (e.food == Content.ENERGIZER) {
				game.ghostIndex = 0;
				sm.enqueue(new PacManGainsPowerEvent());
			}
		}
	}

	private class ChangingLevelState extends StateObject<State, GameEvent> {

		@Override
		public void onEntry() {
			gameUI.mazeUI.setFlashing(true);
		}

		@Override
		public void onTick() {
			if (getRemaining() == getDuration() / 2) {
				nextLevel();
				gameUI.showInfo("Ready!", Color.YELLOW);
				gameUI.mazeUI.setFlashing(false);
				gameUI.mazeUI.enableAnimation(false);
			} else if (isTerminated()) {
				gameUI.hideInfo();
				gameUI.mazeUI.enableAnimation(true);
			}
		}

		private void nextLevel() {
			game.level += 1;
			game.foodEaten = 0;
			game.ghostIndex = 0;
			game.maze.resetFood();
			actors.init();
		}
	}

	private class GhostDyingState extends StateObject<State, GameEvent> {

		@Override
		public void onEntry() {
			actors.getPacMan().visibility = () -> false;
		}

		@Override
		public void onTick() {
			actors.getActiveGhosts().filter(ghost -> ghost.getState() == Ghost.State.DYING).forEach(Ghost::update);
		}

		@Override
		public void onExit() {
			actors.getPacMan().visibility = () -> true;
		}
	}

	private class PacManDyingState extends StateObject<State, GameEvent> {

		@Override
		public void onEntry() {
			game.lives -= 1;
			actors.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> false);
			gameUI.mazeUI.init();
		}

		@Override
		public void onTick() {
			actors.getPacMan().update();
		}

		@Override
		public void onExit() {
			actors.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> true);
		}
	}

	private class GameOverState extends StateObject<State, GameEvent> {

		@Override
		public void onEntry() {
			gameUI.mazeUI.enableAnimation(false);
			gameUI.mazeUI.init();
			gameUI.showInfo("Game Over!", Color.RED);
		}

		@Override
		public void onExit() {
			gameUI.hideInfo();
		}
	}
}