package de.amr.games.pacman.controller;

import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.ui.PlaySceneInfo.LOG;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.ViewController;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GhostContactEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.NextLevelEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Levels;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.HUD;
import de.amr.games.pacman.ui.MazeUI;
import de.amr.games.pacman.ui.PlaySceneInfo;
import de.amr.games.pacman.ui.StatusUI;
import de.amr.games.pacman.ui.actor.Ghost;
import de.amr.games.pacman.ui.actor.PacMan;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateTransition;

public class PlayScene implements ViewController {

	private final PacManApp app;
	public final Game game = new Game();
	public final Maze maze;
	public final MazeUI mazeUI;
	public final HUD hud;
	public final StatusUI status;

	public enum State {
		READY, PLAYING, GHOST_DYING, PACMAN_DYING, CHANGING_LEVEL, GAME_OVER
	};

	public final StateMachine<State, GameEvent> gameControl;

	public PlayScene(PacManApp app) {
		this.app = app;
		this.maze = app.maze;
		mazeUI = new MazeUI(maze);
		hud = new HUD(game);
		status = new StatusUI(game);
		buildLayout();

		gameControl = createGameControl();
		mazeUI.eventing.subscribe(gameControl::enqueue);
		mazeUI.getPacMan().eventing.subscribe(gameControl::enqueue);
		mazeUI.getGhosts().forEach(ghost -> ghost.eventing.subscribe(gameControl::enqueue));
	}

	private StateMachine<State, GameEvent> createGameControl() {
		StateMachine<State, GameEvent> fsm = new StateMachine<>("GameController", State.class,
				State.READY);
		fsm.fnFrequency = () -> app.pulse.getFrequency();
		fsm.setLogger(PlaySceneInfo.LOG);

		// -- READY

		fsm.state(State.READY).entry = state -> {
			state.setDuration(sec(2));
			game.init(maze);
			initActors();
			mazeUI.enableAnimation(false);
			mazeUI.showInfo("Ready!");
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

		fsm.changeOnInput(PacManKilledEvent.class, State.PLAYING, State.PACMAN_DYING,
				this::onPacManKilled);

		fsm.changeOnInput(NextLevelEvent.class, State.PLAYING, State.CHANGING_LEVEL);

		// -- GHOST_DYING

		fsm.state(State.GHOST_DYING).entry = state -> {
			state.setDuration(sec(0.5f));
			mazeUI.getPacMan().visibility = () -> false;
		};

		fsm.state(State.GHOST_DYING).update = state -> {
			mazeUI.getGhosts().filter(ghost -> ghost.getState() == Ghost.State.DEAD)
					.forEach(Ghost::update);
		};

		fsm.changeOnTimeout(State.GHOST_DYING, State.PLAYING, this::onGhostDied);

		fsm.state(State.GHOST_DYING).exit = state -> {
			mazeUI.getPacMan().visibility = () -> true;
		};

		// -- CHANGING_LEVEL

		fsm.state(State.CHANGING_LEVEL).entry = state -> {
			state.setDuration(sec(4));
			mazeUI.setFlashing(true);
		};

		fsm.state(State.CHANGING_LEVEL).update = state -> {
			if (state.getRemaining() == state.getDuration() / 2) {
				nextLevel();
				mazeUI.showInfo("Ready!");
				mazeUI.setFlashing(false);
				mazeUI.enableAnimation(false);
			} else if (state.isTerminated()) {
				mazeUI.hideInfo();
				mazeUI.enableAnimation(true);
			}
		};

		fsm.changeOnTimeout(State.CHANGING_LEVEL, State.PLAYING);

		// -- PACMAN_DYING

		fsm.state(State.PACMAN_DYING).entry = state -> {
			state.setDuration(sec(3));
			mazeUI.getPacMan().setState(PacMan.State.DYING);
			mazeUI.getGhosts().forEach(ghost -> ghost.visibility = () -> false);
			game.lives -= 1;
		};

		fsm.state(State.PACMAN_DYING).exit = state -> {
			mazeUI.getGhosts().forEach(ghost -> ghost.visibility = () -> true);
		};

		fsm.changeOnTimeout(State.PACMAN_DYING, State.GAME_OVER, () -> game.lives == 0);

		fsm.changeOnTimeout(State.PACMAN_DYING, State.PLAYING, () -> game.lives > 0, t -> {
			mazeUI.getPacMan().currentSprite().resetAnimation();
			initActors();
		});

		// -- GAME_OVER

		fsm.state(State.GAME_OVER).entry = state -> {
			mazeUI.enableAnimation(false);
			mazeUI.showInfo("Game Over!");
		};

		fsm.change(State.GAME_OVER, State.READY, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

		fsm.state(State.GAME_OVER).exit = state -> {
			mazeUI.hideInfo();
			mazeUI.removeBonus();
			game.init(maze);
		};

		return fsm;
	}

	@Override
	public int getWidth() {
		return app.settings.width;
	}

	@Override
	public int getHeight() {
		return app.settings.height;
	}

	@Override
	public void init() {
		gameControl.init();
	}

	/**
	 * Called on every clock tick.
	 */
	@Override
	public void update() {
		PlaySceneInfo.update(this);
		gameControl.update();
	}

	@Override
	public void draw(Graphics2D g) {
		hud.draw(g);
		mazeUI.draw(g);
		status.draw(g);
		PlaySceneInfo.draw(g, this);
	}

	private int sec(float seconds) {
		return app.pulse.secToTicks(seconds);
	}

	private void buildLayout() {
		hud.tf.moveTo(0, 0);
		mazeUI.tf.moveTo(0, 3 * MazeUI.TS);
		status.tf.moveTo(0, (3 + maze.numRows()) * MazeUI.TS);
	}

	private void initActors() {
		mazeUI.getPacMan().setState(PacMan.State.ALIVE);
		mazeUI.getPacMan().placeAt(maze.pacManHome);
		mazeUI.getPacMan().setSpeed(game::getPacManSpeed);
		mazeUI.getPacMan().setDir(Top4.E);
		mazeUI.getPacMan().setNextDir(Top4.E);

		mazeUI.getBlinky().setDir(Top4.E);
		mazeUI.getPinky().setDir(Top4.S);
		mazeUI.getInky().setDir(Top4.N);
		mazeUI.getClyde().setDir(Top4.N);
		mazeUI.getGhosts().forEach(ghost -> {
			ghost.setState(Ghost.State.SAFE);
			ghost.placeAt(ghost.homeTile);
			ghost.setSpeed(game::getGhostSpeed);
		});
	}

	private void nextLevel() {
		game.level += 1;
		game.foodEaten = 0;
		game.ghostIndex = 0;
		maze.resetFood();
		initActors();
	}

	private void startGhostHunting() {
		game.ghostIndex = 0;
		mazeUI.getGhosts()
				.filter(
						ghost -> ghost.getState() != Ghost.State.DEAD && ghost.getState() != Ghost.State.SAFE)
				.forEach(ghost -> ghost.setState(Ghost.State.AFRAID));
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
			gameControl.enqueue(new PacManKilledEvent(e.ghost));
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

	private void onPacManKilled(StateTransition<State, GameEvent> t) {
		PacManKilledEvent e = event(t);
		LOG.info(
				() -> String.format("PacMan killed by %s at %s", e.ghost.getName(), e.ghost.getTile()));
	}

	private void onGhostKilled(StateTransition<State, GameEvent> t) {
		GhostKilledEvent e = event(t);
		LOG.info(() -> String.format("Ghost %s killed at %s", e.ghost.getName(), e.ghost.getTile()));
		e.ghost.onWounded(game.ghostIndex);
		game.score += Game.GHOST_POINTS[game.ghostIndex];
		game.ghostIndex += 1;
	}

	private void onGhostDied(StateTransition<State, GameEvent> t) {
		// TODO get ghost from transition/event?
		mazeUI.getGhosts().filter(ghost -> ghost.getState() == Ghost.State.DYING).findFirst()
				.ifPresent(Ghost::onExitus);
	}

	private void onFoodFound(StateTransition<State, GameEvent> t) {
		FoodFoundEvent e = event(t);
		maze.clearTile(e.tile);
		game.foodEaten += 1;
		if (e.food == ENERGIZER) {
			LOG.info(() -> String.format("PacMan found energizer at %s", e.tile));
			game.score += Game.ENERGIZER_VALUE;
		} else {
			LOG.info(() -> String.format("PacMan found pellet at %s", e.tile));
			game.score += Game.PELLET_VALUE;
		}
		if (game.foodEaten == game.foodTotal) {
			gameControl.enqueue(new NextLevelEvent());
			return;
		}
		if (game.foodEaten == Game.DOTS_BONUS_1) {
			mazeUI.showBonus(Levels.getBonusSymbol(game.level), Levels.getBonusValue(game.level), sec(9));
		} else if (game.foodEaten == Game.DOTS_BONUS_2) {
			mazeUI.showBonus(Levels.getBonusSymbol(game.level), Levels.getBonusValue(game.level), sec(9));
		}
		if (e.food == ENERGIZER) {
			startGhostHunting();
		}
	}

	private void onBonusFound(StateTransition<State, GameEvent> t) {
		BonusFoundEvent e = event(t);
		LOG.info(() -> String.format("PacMan found bonus %s of value %d", e.symbol, e.value));
		game.score += e.value;
		mazeUI.honorBonus(sec(2));
	}
}