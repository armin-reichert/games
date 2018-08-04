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
import de.amr.games.pacman.model.Content;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.MazeUI;
import de.amr.games.pacman.ui.actor.Bonus;
import de.amr.games.pacman.ui.actor.Ghost;
import de.amr.statemachine.CustomStateObject;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;
import de.amr.statemachine.StateTransition;

public class GameController extends StateMachine<State, GameEvent> {

	private final Game game;
	private final Maze maze;
	private final MazeUI mazeUI;

	public enum State {
		READY, PLAYING, GHOST_DYING, PACMAN_DYING, CHANGING_LEVEL, GAME_OVER
	};

	private class ReadyState extends CustomStateObject<State, GameEvent> {

		public ReadyState() {
			super(GameController.this, State.READY);
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

	private class PlayingState extends CustomStateObject<State, GameEvent> {

		public PlayingState() {
			super(GameController.this, State.PLAYING);
		}

		@Override
		public void onTick(StateObject<State, GameEvent> self) {
			mazeUI.update();
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
			if (e.food == Content.ENERGIZER) {
				game.ghostIndex = 0;
				enqueue(new PacManGainsPowerEvent());
			}
		}

	}

	private class ChangingLevelState extends CustomStateObject<State, GameEvent> {

		public ChangingLevelState() {
			super(GameController.this, State.CHANGING_LEVEL);
		}

		@Override
		public void defineTransitions() {
			changeOnTimeout(State.PLAYING);
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

	private class GhostDyingState extends CustomStateObject<State, GameEvent> {

		public GhostDyingState() {
			super(GameController.this, State.GHOST_DYING);
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

		private void onGhostKilled(StateTransition<State, GameEvent> t) {
			GhostKilledEvent e = t.typedEvent();
			e.ghost.processEvent(e);
			LOG.info(() -> String.format("Ghost %s killed at %s", e.ghost.getName(), e.ghost.getTile()));
		}
	}

	private class PacManDyingState extends CustomStateObject<State, GameEvent> {

		public PacManDyingState() {
			super(GameController.this, State.PACMAN_DYING);
		}

		// Game event handling

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
			mazeUI.getActiveGhosts().forEach(ghost -> ghost.visibility = () -> true);
		}

		private void onPacManKilled(StateTransition<State, GameEvent> t) {
			PacManKilledEvent e = t.typedEvent();
			mazeUI.getPacMan().processEvent(e);
			LOG.info(() -> String.format("PacMan killed by %s at %s", e.killer.getName(), e.killer.getTile()));
		}
	}

	private class GameOverState extends CustomStateObject<State, GameEvent> {

		public GameOverState() {
			super(GameController.this, State.GAME_OVER);
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

	public GameController(Game game, Maze maze, MazeUI mazeUI) {
		super("GameController", State.class, State.READY);

		fnPulse = game.fnTicksPerSecond;
		this.game = game;
		this.maze = maze;
		this.mazeUI = mazeUI;

		// Listen to events from actors
		mazeUI.eventMgr.subscribe(this::enqueue);
		mazeUI.getPacMan().eventMgr.subscribe(this::enqueue);
		mazeUI.getActiveGhosts().forEach(ghost -> ghost.eventMgr.subscribe(this::enqueue));

		// Create states
		ReadyState ready = createState(State.READY, ReadyState::new);
		PlayingState playing = createState(State.PLAYING, PlayingState::new);
		ChangingLevelState changingLevel = createState(State.CHANGING_LEVEL, ChangingLevelState::new);
		GhostDyingState ghostDying = createState(State.GHOST_DYING, GhostDyingState::new);
		PacManDyingState pacManDying = createState(State.PACMAN_DYING, PacManDyingState::new);
		GameOverState gameOver = createState(State.GAME_OVER, GameOverState::new);

		// Define the state transition graph
		/*@formatter:off*/
		ready
			.changeOnTimeout(playing.id);

		playing
			.onInput(FoodFoundEvent.class, playing::onFoodFound)
			.onInput(BonusFoundEvent.class, playing::onBonusFound)
			.onInput(PacManGhostCollisionEvent.class, playing::onPacManGhostCollision)
			.onInput(PacManGainsPowerEvent.class, playing::onPacManGainsPower)
			.onInput(PacManLosesPowerEvent.class, playing::onPacManLosesPower)
			.onInput(PacManLostPowerEvent.class, playing::onPacManLostPower)
			.changeOnInput(GhostKilledEvent.class, ghostDying.id, ghostDying::onGhostKilled)
			.changeOnInput(PacManKilledEvent.class, pacManDying.id, pacManDying::onPacManKilled)
			.changeOnInput(LevelCompletedEvent.class, changingLevel.id);
			;

		ghostDying
			.changeOnTimeout(playing.id);

		pacManDying
			.changeOnInput(PacManDiedEvent.class, gameOver.id, () -> game.lives == 0)
			.changeOnInput(PacManDiedEvent.class, playing.id, () -> game.lives > 0, t -> mazeUI.initActors());

		gameOver
			.change(ready.id, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		/*@formatter:on*/
	}
}