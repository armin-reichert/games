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
import de.amr.games.pacman.actor.Bonus;
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
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.MazeUI;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;
import de.amr.statemachine.StateTransition;

public class GameController {

	private final StateMachine<State, GameEvent> sm;
	private final Game game;
	private final Maze maze;
	private final MazeUI mazeUI;

	public GameController(Game game, Maze maze, MazeUI mazeUI) {
		
		this.game = game;
		this.maze = maze;
		this.mazeUI = mazeUI;
		
		// build the state machine
		sm = new StateMachine<>(State.class);
		sm.fnPulse = game.fnTicksPerSecond;
		buildStateMachine(sm);

		// Listen to events from actors
		mazeUI.eventMgr.subscribe(sm::enqueue);
		mazeUI.getPacMan().eventMgr.subscribe(sm::enqueue);
		mazeUI.getActiveGhosts().forEach(ghost -> ghost.eventMgr.subscribe(sm::enqueue));
	}

	public void init() {
		sm.init();
	}

	public void update() {
		sm.update();
	}

	public void setLogger(Logger log) {
		sm.setLogger(log);
	}

	public enum State {
		READY, PLAYING, GHOST_DYING, PACMAN_DYING, CHANGING_LEVEL, GAME_OVER
	};

	private void buildStateMachine(StateMachine<State, GameEvent> sm) {
		/*@formatter:off*/
		sm.builder()
			.description("GameController")
			.initialState(State.READY)
		
			.states()
				.state(READY).impl(ReadyState::new).build()
				.state(PLAYING).impl(PlayingState::new).build()
				.state(CHANGING_LEVEL).impl(ChangingLevelState::new).build()
				.state(GHOST_DYING).impl(GhostDyingState::new).build()
				.state(PACMAN_DYING).impl(PacManDyingState::new).build()
				.state(GAME_OVER).impl(GameOverState::new).build()
			
			.transitions()
				.onTimeout()
					.change(READY, PLAYING)
					.build()
				.on(FoodFoundEvent.class)
					.keep(PLAYING)
					.act(((PlayingState) sm.state(PLAYING))::onFoodFound)
					.build()
				.on(BonusFoundEvent.class)
					.keep(PLAYING)
					.act(((PlayingState) sm.state(PLAYING))::onBonusFound)
					.build()
				.on(PacManGhostCollisionEvent.class)
					.keep(PLAYING)
					.act(((PlayingState) sm.state(PLAYING))::onPacManGhostCollision)
					.build()
				.on(PacManGainsPowerEvent.class)
					.keep(PLAYING)
					.act(((PlayingState) sm.state(PLAYING))::onPacManGainsPower)
					.build()
				.on(PacManLosesPowerEvent.class)
					.keep(PLAYING)
					.act(((PlayingState) sm.state(PLAYING))::onPacManLosesPower)
					.build()
				.on(PacManLostPowerEvent.class)
					.keep(PLAYING)
					.act(((PlayingState) sm.state(PLAYING))::onPacManLostPower)
					.build()
				.on(GhostKilledEvent.class)
					.change(PLAYING, GHOST_DYING)
					.act(((PlayingState) sm.state(PLAYING))::onGhostKilled)
					.build()
				.on(PacManKilledEvent.class)
					.change(PLAYING, PACMAN_DYING)
					.act(((PlayingState) sm.state(PLAYING))::onPacManKilled)
					.build()
				.on(LevelCompletedEvent.class)
					.change(PLAYING, CHANGING_LEVEL)
					.build()
				.onTimeout()	
					.change(State.CHANGING_LEVEL, PLAYING)
					.build()
				.onTimeout()
					.change(GHOST_DYING, PLAYING)
					.build()
				.on(PacManDiedEvent.class)
					.change(PACMAN_DYING, State.GAME_OVER)
					.when(() -> game.lives == 0)
					.build()
				.on(PacManDiedEvent.class)
					.change(PACMAN_DYING, PLAYING)
					.when(() -> game.lives > 0)
					.act(t -> mazeUI.initActors())
					.build()
				.when(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
					.change(GAME_OVER, READY)
					.build()
		.buildStateMachine();
		/*@formatter:on*/
	}

	private class ReadyState extends StateObject<State, GameEvent> {

		public ReadyState() {
			super(sm, State.READY);
		}

		@Override
		public void onEntry(StateObject<State, GameEvent> self) {
			setDuration(game.sec(2));
			game.init(maze);
			mazeUI.initActors();
			mazeUI.enableAnimation(false);
			mazeUI.showInfo("Ready!", Color.YELLOW);
		}

		@Override
		public void onExit(StateObject<State, GameEvent> self) {
			mazeUI.enableAnimation(true);
			mazeUI.hideInfo();
		}
	}

	private class PlayingState extends StateObject<State, GameEvent> {

		public PlayingState() {
			super(sm, State.PLAYING);
		}

		@Override
		public void onTick(StateObject<State, GameEvent> self) {
			mazeUI.update();
		}

		private void onPacManGhostCollision(StateTransition<State, GameEvent> t) {
			PacManGhostCollisionEvent e = t.typedEvent();
			PacMan.State pacManState = mazeUI.getPacMan().getState();
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
			mazeUI.getPacMan().processEvent(e);
			LOG.info(() -> String.format("PacMan killed by %s at %s", e.killer.getName(), e.killer.getTile()));
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

		private void onGhostKilled(StateTransition<State, GameEvent> t) {
			GhostKilledEvent e = t.typedEvent();
			e.ghost.processEvent(e);
			LOG.info(() -> String.format("Ghost %s killed at %s", e.ghost.getName(), e.ghost.getTile()));
		}

		private void onBonusFound(StateTransition<State, GameEvent> t) {
			BonusFoundEvent e = t.typedEvent();
			LOG.info(() -> String.format("PacMan found bonus %s of value %d", e.symbol, e.value));
			game.score += e.value;
			mazeUI.honorBonusAndRemoveAfter(game.sec(2));
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
				sm.enqueue(new LevelCompletedEvent());
				return;
			}
			if (game.foodEaten == Game.FOOD_EATEN_BONUS_1 || game.foodEaten == Game.FOOD_EATEN_BONUS_2) {
				mazeUI.addBonus(new Bonus(game.getBonusSymbol(), game.getBonusValue()), game.getBonusTime());
			}
			if (e.food == Content.ENERGIZER) {
				game.ghostIndex = 0;
				sm.enqueue(new PacManGainsPowerEvent());
			}
		}
	}

	private class ChangingLevelState extends StateObject<State, GameEvent> {

		public ChangingLevelState() {
			super(sm, State.CHANGING_LEVEL);
		}

		@Override
		public void onEntry(StateObject<State, GameEvent> self) {
			setDuration(game.getLevelChangingTime());
			mazeUI.setFlashing(true);
		}

		@Override
		public void onTick(StateObject<State, GameEvent> self) {
			if (getRemaining() == getDuration() / 2) {
				nextLevel();
				mazeUI.showInfo("Ready!", Color.YELLOW);
				mazeUI.setFlashing(false);
				mazeUI.enableAnimation(false);
			} else if (isTerminated()) {
				mazeUI.hideInfo();
				mazeUI.enableAnimation(true);
			}
		}

		private void nextLevel() {
			game.level += 1;
			game.foodEaten = 0;
			game.ghostIndex = 0;
			maze.resetFood();
			mazeUI.initActors();
		}
	}

	private class GhostDyingState extends StateObject<State, GameEvent> {

		public GhostDyingState() {
			super(sm, State.GHOST_DYING);
		}

		@Override
		public void onEntry(StateObject<State, GameEvent> self) {
			setDuration(game.getGhostDyingTime());
			mazeUI.getPacMan().visibility = () -> false;
		}

		@Override
		public void onTick(StateObject<State, GameEvent> self) {
			mazeUI.getActiveGhosts().filter(ghost -> ghost.getState() == Ghost.State.DYING).forEach(Ghost::update);
		}

		@Override
		public void onExit(StateObject<State, GameEvent> self) {
			mazeUI.getPacMan().visibility = () -> true;
		}
	}

	private class PacManDyingState extends StateObject<State, GameEvent> {

		public PacManDyingState() {
			super(sm, State.PACMAN_DYING);
		}

		@Override
		public void onEntry(StateObject<State, GameEvent> self) {
			game.lives -= 1;
			mazeUI.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> false);
		}

		@Override
		public void onTick(StateObject<State, GameEvent> self) {
			mazeUI.getPacMan().update();
		}

		@Override
		public void onExit(StateObject<State, GameEvent> self) {
			mazeUI.removeBonus();
			mazeUI.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> true);
		}
	}

	private class GameOverState extends StateObject<State, GameEvent> {

		public GameOverState() {
			super(sm, State.GAME_OVER);
		}

		@Override
		public void onEntry(StateObject<State, GameEvent> self) {
			mazeUI.enableAnimation(false);
			mazeUI.removeBonus();
			mazeUI.showInfo("Game Over!", Color.RED);
		}

		@Override
		public void onExit(StateObject<State, GameEvent> self) {
			mazeUI.hideInfo();
			game.init(maze);
		}
	}
}