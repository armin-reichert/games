package de.amr.games.pacman.controller;

import static de.amr.games.pacman.behavior.impl.NavigationSystem.ambush;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.bounce;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.chase;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.flee;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.followKeyboard;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.goHome;
import static de.amr.games.pacman.model.Spritesheet.ORANGE_GHOST;
import static de.amr.games.pacman.model.Spritesheet.PINK_GHOST;
import static de.amr.games.pacman.model.Spritesheet.RED_GHOST;
import static de.amr.games.pacman.model.Spritesheet.TURQUOISE_GHOST;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.ui.PlaySceneInfo.LOG;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.stream.Stream;

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
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Game;
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

	public enum State {
		READY, PLAYING, KILLING_GHOST, DYING, CHANGING_LEVEL, GAME_OVER
	};

	private final PacManApp app;

	// Model
	public final Game game = new Game();
	public final Maze maze;

	// View
	public final MazeUI mazeUI;
	public final HUD hud;
	public final StatusUI status;
	public final PacMan pacMan;
	public final Ghost blinky, pinky, inky, clyde;

	// Controller
	public final StateMachine<State, GameEvent> fsm;

	public PlayScene(PacManApp app) {
		this.app = app;
		this.maze = app.maze;

		// Create state machine controlling the game play
		fsm = createPlayControl();

		// Create the actors
		pacMan = createPacMan();
		blinky = createBlinky();
		pinky = createPinky();
		inky = createInky();
		clyde = createClyde();

		// Create and populate the board
		mazeUI = new MazeUI(maze, pacMan);
		mazeUI.addGhost(blinky);
		mazeUI.addGhost(pinky);
		mazeUI.addGhost(inky);
		mazeUI.addGhost(clyde);
		hud = new HUD(game);
		status = new StatusUI(game);
		buildLayout();

		// Pass game events to state machine
		mazeUI.observers.addObserver(fsm::enqueue);
		Stream.of(pacMan, blinky, pinky, inky, clyde)
				.forEach(entity -> entity.observers.addObserver(fsm::enqueue));
	}

	private StateMachine<State, GameEvent> createPlayControl() {
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

		fsm.changeOnInput(GhostKilledEvent.class, State.PLAYING, State.KILLING_GHOST,
				this::onGhostKilled);

		fsm.changeOnInput(PacManKilledEvent.class, State.PLAYING, State.DYING, this::onPacManKilled);

		fsm.changeOnInput(NextLevelEvent.class, State.PLAYING, State.CHANGING_LEVEL);

		// -- KILLING_GHOST

		fsm.state(State.KILLING_GHOST).entry = state -> {
			state.setDuration(sec(0.5f));
			pacMan.visibility = () -> false;
		};

		fsm.state(State.KILLING_GHOST).update = state -> {
			mazeUI.getGhosts().filter(ghost -> ghost.getState() == Ghost.State.DEAD)
					.forEach(Ghost::update);
		};

		fsm.changeOnTimeout(State.KILLING_GHOST, State.PLAYING, this::onGhostDied);

		fsm.state(State.KILLING_GHOST).exit = state -> {
			pacMan.visibility = () -> true;
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

		// -- DYING

		fsm.state(State.DYING).entry = state -> {
			state.setDuration(sec(3));
			pacMan.setState(PacMan.State.DYING);
			mazeUI.getGhosts().forEach(ghost -> ghost.visibility = () -> false);
			game.lives -= 1;
		};

		fsm.state(State.DYING).exit = state -> {
			mazeUI.getGhosts().forEach(ghost -> ghost.visibility = () -> true);
		};

		fsm.changeOnTimeout(State.DYING, State.GAME_OVER, () -> game.lives == 0);

		fsm.changeOnTimeout(State.DYING, State.PLAYING, () -> game.lives > 0, t -> {
			pacMan.currentSprite().resetAnimation();
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
		fsm.init();
	}

	/**
	 * Called on every clock tick.
	 */
	@Override
	public void update() {
		PlaySceneInfo.update(this);
		fsm.update();
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

	private PacMan createPacMan() {
		PacMan pacMan = new PacMan(maze, maze.pacManHome);
		pacMan.setNavigation(PacMan.State.ALIVE, followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		return pacMan;
	}

	private Ghost createBlinky() {
		Ghost blinky = new Ghost(maze, "Blinky", RED_GHOST, maze.blinkyHome);
		blinky.setNavigation(Ghost.State.AGGRO, chase(pacMan));
		blinky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		blinky.setNavigation(Ghost.State.BRAVE, flee(pacMan));
		blinky.setNavigation(Ghost.State.DEAD, goHome());
		blinky.setNavigation(Ghost.State.SAFE, bounce());
		return blinky;
	}

	private Ghost createPinky() {
		Ghost pinky = new Ghost(maze, "Pinky", PINK_GHOST, maze.pinkyHome);
		pinky.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		pinky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		pinky.setNavigation(Ghost.State.BRAVE, flee(pacMan));
		pinky.setNavigation(Ghost.State.DEAD, goHome());
		pinky.setNavigation(Ghost.State.SAFE, bounce());
		return pinky;
	}

	private Ghost createInky() {
		Ghost inky = new Ghost(maze, "Inky", TURQUOISE_GHOST, maze.inkyHome);
		inky.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		inky.setNavigation(Ghost.State.AFRAID, flee(pacMan));
		inky.setNavigation(Ghost.State.BRAVE, flee(pacMan));
		inky.setNavigation(Ghost.State.DEAD, goHome());
		inky.setNavigation(Ghost.State.SAFE, bounce());
		return inky;
	}

	private Ghost createClyde() {
		Ghost clyde = new Ghost(maze, "Clyde", ORANGE_GHOST, maze.clydeHome);
		clyde.setNavigation(Ghost.State.AGGRO, ambush(pacMan));
		clyde.setNavigation(Ghost.State.AFRAID, goHome());
		clyde.setNavigation(Ghost.State.BRAVE, flee(pacMan));
		clyde.setNavigation(Ghost.State.DEAD, goHome());
		clyde.setNavigation(Ghost.State.SAFE, bounce());
		return clyde;
	}

	private void buildLayout() {
		hud.tf.moveTo(0, 0);
		mazeUI.tf.moveTo(0, 3 * MazeUI.TS);
		status.tf.moveTo(0, (3 + maze.numRows()) * MazeUI.TS);
	}

	private void initActors() {
		pacMan.setState(PacMan.State.ALIVE);
		pacMan.placeAt(maze.pacManHome);
		pacMan.setSpeed(game::getPacManSpeed);
		pacMan.setDir(Top4.E);
		pacMan.setNextDir(Top4.E);

		blinky.setDir(Top4.E);
		pinky.setDir(Top4.S);
		inky.setDir(Top4.N);
		clyde.setDir(Top4.N);

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
			fsm.enqueue(new PacManKilledEvent(e.ghost));
			break;
		case AFRAID:
		case BRAVE:
			fsm.enqueue(new GhostKilledEvent(e.ghost));
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
			fsm.enqueue(new NextLevelEvent());
			return;
		}
		if (game.foodEaten == Game.DOTS_BONUS_1) {
			mazeUI.showBonus(BonusSymbol.CHERRIES, 100, sec(5));
		} else if (game.foodEaten == Game.DOTS_BONUS_2) {
			mazeUI.showBonus(BonusSymbol.STRAWBERRY, 100, sec(5));
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