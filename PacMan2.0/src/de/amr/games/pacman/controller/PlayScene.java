package de.amr.games.pacman.controller;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.ambush;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.bounce;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.chase;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.flee;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.followKeyboard;
import static de.amr.games.pacman.behavior.impl.NavigationSystem.goHome;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.ui.Spritesheet.BLUE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.ORANGE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.PINK_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.RED_GHOST;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.ActiveScene;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameEventListener;
import de.amr.games.pacman.controller.event.GhostContactEvent;
import de.amr.games.pacman.controller.event.GhostDeadIsOverEvent;
import de.amr.games.pacman.controller.event.GhostFrightenedEndsEvent;
import de.amr.games.pacman.controller.event.GhostRecoveringCompleteEvent;
import de.amr.games.pacman.controller.event.NextLevelEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.model.BonusSymbol;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Debug;
import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.HUD;
import de.amr.games.pacman.ui.MazeUI;
import de.amr.games.pacman.ui.PacMan;
import de.amr.games.pacman.ui.StatusUI;
import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateTransition;

public class PlayScene extends ActiveScene<PacManApp> implements GameEventListener {

	public enum State {
		READY, PLAYING, DYING, CHANGING_LEVEL, GAME_OVER
	};

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
		super(app);
		this.maze = app.maze;

		// Actors
		pacMan = createPacMan();
		blinky = createBlinky();
		pinky = createPinky();
		inky = createInky();
		clyde = createClyde();

		// Board
		mazeUI = new MazeUI(maze, pacMan);
		mazeUI.observers.addObserver(this);
		mazeUI.addGhost(blinky);
		mazeUI.addGhost(pinky);
		mazeUI.addGhost(inky);
		mazeUI.addGhost(clyde);

		hud = new HUD(game);
		status = new StatusUI(game);
		buildLayout();

		// Controller

		fsm = new StateMachine<>("Game control", State.class, State.READY);
		fsm.fnFrequency = () -> app.pulse.getFrequency();

		// -- READY

		fsm.state(State.READY).entry = state -> {
			state.setDuration(sec(2));
			game.init(maze);
			initEntities();
			mazeUI.enableAnimation(false);
			mazeUI.showInfo("Ready!");
		};

		fsm.state(State.READY).exit = state -> {
			mazeUI.enableAnimation(true);
			mazeUI.hideInfo();
		};

		fsm.changeOnTimeout(State.READY, State.PLAYING);

		// -- RUNNING

		fsm.state(State.PLAYING).update = state -> mazeUI.update();

		fsm.changeOnInput(FoodFoundEvent.class, State.PLAYING, State.PLAYING, this::onFoodFound);

		fsm.changeOnInput(BonusFoundEvent.class, State.PLAYING, State.PLAYING, this::onBonusFound);

		fsm.changeOnInput(GhostFrightenedEndsEvent.class, State.PLAYING, State.PLAYING,
				this::onGhostFrightenedEnds);

		fsm.changeOnInput(GhostDeadIsOverEvent.class, State.PLAYING, State.PLAYING,
				this::onGhostDeadIsOver);

		fsm.changeOnInput(GhostRecoveringCompleteEvent.class, State.PLAYING, State.PLAYING,
				this::onGhostRecoveringComplete);

		fsm.changeOnInput(GhostContactEvent.class, State.PLAYING, State.PLAYING, this::onGhostContact);

		fsm.changeOnInput(PacManKilledEvent.class, State.PLAYING, State.DYING, this::onPacManKilled);

		fsm.changeOnInput(NextLevelEvent.class, State.PLAYING, State.CHANGING_LEVEL);

		// -- CHANGING_LEVEL

		fsm.state(State.CHANGING_LEVEL).entry = state -> {
			state.setDuration(sec(4));
			mazeUI.setFlashing(true);
		};

		fsm.state(State.CHANGING_LEVEL).update = state -> {
			if (state.getRemaining() == state.getDuration() / 2) {
				nextLevel();
				mazeUI.setFlashing(false);
				mazeUI.enableAnimation(false);
			} else if (state.isTerminated()) {
				mazeUI.enableAnimation(true);
			}
		};

		fsm.changeOnTimeout(State.CHANGING_LEVEL, State.PLAYING);

		// -- DYING

		fsm.state(State.DYING).entry = state -> {
			state.setDuration(sec(3));
			pacMan.setState(PacMan.State.DYING);
			game.livesLeft -= 1;
		};

		fsm.changeOnTimeout(State.DYING, State.GAME_OVER, () -> game.livesLeft == 0);

		fsm.changeOnTimeout(State.DYING, State.PLAYING, () -> game.livesLeft > 0, t -> {
			pacMan.currentSprite().resetAnimation();
			initEntities();
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
	}

	@Override
	public void onGameEvent(GameEvent e) {
		fsm.enqueue(e);
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
		Debug.update(this);
		fsm.update();
	}

	@Override
	public void draw(Graphics2D g) {
		hud.draw(g);
		mazeUI.draw(g);
		status.draw(g);
		Debug.draw(g, this);
	}

	private int sec(float seconds) {
		return app.pulse.secToTicks(seconds);
	}

	private PacMan createPacMan() {
		PacMan pacMan = new PacMan(maze, maze.pacManHome);
		pacMan.observers.addObserver(this);
		pacMan.setNavigation(PacMan.State.ALIVE, followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		return pacMan;
	}

	private Ghost createBlinky() {
		Ghost blinky = new Ghost(maze, "Blinky", RED_GHOST, maze.blinkyHome);
		blinky.observers.addObserver(this);
		blinky.setNavigation(Ghost.State.FRIGHTENED, flee(pacMan));
		blinky.setNavigation(Ghost.State.DEAD, goHome());
		blinky.setNavigation(Ghost.State.RECOVERING, bounce());
		blinky.setNavigation(Ghost.State.ATTACKING, chase(pacMan));
		return blinky;
	}

	private Ghost createPinky() {
		Ghost pinky = new Ghost(maze, "Pinky", PINK_GHOST, maze.pinkyHome);
		pinky.observers.addObserver(this);
		pinky.setNavigation(Ghost.State.FRIGHTENED, flee(pacMan));
		pinky.setNavigation(Ghost.State.DEAD, goHome());
		pinky.setNavigation(Ghost.State.RECOVERING, bounce());
		pinky.setNavigation(Ghost.State.ATTACKING, ambush(pacMan));
		return pinky;
	}

	private Ghost createInky() {
		Ghost inky = new Ghost(maze, "Inky", BLUE_GHOST, maze.inkyHome);
		inky.observers.addObserver(this);
		inky.setNavigation(Ghost.State.FRIGHTENED, flee(pacMan));
		inky.setNavigation(Ghost.State.DEAD, goHome());
		inky.setNavigation(Ghost.State.RECOVERING, bounce());
		// inky.setMoveBehavior(Ghost.State.ATTACKING, moody());
		return inky;
	}

	private Ghost createClyde() {
		Ghost clyde = new Ghost(maze, "Clyde", ORANGE_GHOST, maze.clydeHome);
		clyde.observers.addObserver(this);
		clyde.setNavigation(Ghost.State.FRIGHTENED, flee(pacMan));
		clyde.setNavigation(Ghost.State.DEAD, goHome());
		clyde.setNavigation(Ghost.State.RECOVERING, bounce());
		// clyde.setMoveBehavior(Ghost.State.ATTACKING, stayBehind());
		return clyde;
	}

	private void buildLayout() {
		hud.tf.moveTo(0, 0);
		mazeUI.tf.moveTo(0, 3 * TS);
		status.tf.moveTo(0, (3 + maze.numRows()) * TS);
	}

	private void initEntities() {
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
			ghost.setState(Ghost.State.RECOVERING);
			ghost.placeAt(ghost.homeTile);
			ghost.setSpeed(game::getGhostSpeed);
		});
	}

	private void nextLevel() {
		game.level += 1;
		game.dotsEaten = 0;
		game.ghostIndex = 0;
		maze.resetFood();
		initEntities();
	}

	private void startGhostHunting() {
		mazeUI.getGhosts().filter(ghost -> ghost.getState() != Ghost.State.DEAD)
				.forEach(ghost -> ghost.setState(Ghost.State.FRIGHTENED));
	}

	// Game event handling

	@SuppressWarnings("unchecked")
	private <E extends GameEvent> E event(StateTransition<State, GameEvent> t) {
		return (E) t.getInput().get();
	}

	private void onGhostContact(StateTransition<State, GameEvent> t) {
		GhostContactEvent e = event(t);
		switch (e.ghost.getState()) {
		case ATTACKING:
		case RECOVERING:
		case SCATTERING:
			fsm.enqueue(new PacManKilledEvent(e.ghost));
			break;
		case FRIGHTENED:
			onGhostKilled(e.ghost); // internal event
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
		Debug.log(
				() -> String.format("PacMan got killed by %s at %s", e.ghost.getName(), e.ghost.getTile()));
	}

	private void onGhostKilled(Ghost ghost) {
		Debug.log(() -> String.format("Ghost %s got killed at %s", ghost.getName(), ghost.getTile()));
		ghost.killAndShowPoints(game.ghostIndex, sec(1));
		game.score += Game.GHOST_POINTS[game.ghostIndex];
		game.ghostIndex += 1;
	}

	private void onFoodFound(StateTransition<State, GameEvent> t) {
		FoodFoundEvent e = event(t);
		maze.clearTile(e.tile);
		game.dotsEaten += 1;
		if (game.dotsEaten == game.dotsTotal) {
			fsm.enqueue(new NextLevelEvent());
			return;
		}
		if (game.dotsEaten == Game.DOTS_BONUS_1) {
			mazeUI.showBonus(BonusSymbol.CHERRIES, 100, sec(5));
		} else if (game.dotsEaten == Game.DOTS_BONUS_2) {
			mazeUI.showBonus(BonusSymbol.STRAWBERRY, 100, sec(5));
		}
		if (e.food == ENERGIZER) {
			Debug.log(() -> String.format("PacMan found energizer at %s", e.tile));
			game.score += Game.ENERGIZER_VALUE;
			game.ghostIndex = 0;
			startGhostHunting();
		} else {
			game.score += Game.PELLET_VALUE;
		}
	}

	private void onBonusFound(StateTransition<State, GameEvent> t) {
		BonusFoundEvent e = event(t);
		Debug.log(() -> String.format("PacMan found bonus %s at %s", e.bonus, e.tile));
		game.score += e.bonus.getValue();
		mazeUI.honorBonus(sec(2));
	}

	private void onGhostFrightenedEnds(StateTransition<State, GameEvent> t) {
		GhostFrightenedEndsEvent e = event(t);
		e.ghost.setState(Ghost.State.ATTACKING);
	}

	private void onGhostDeadIsOver(StateTransition<State, GameEvent> t) {
		GhostDeadIsOverEvent e = event(t);
		e.ghost.setState(Ghost.State.RECOVERING);
	}

	private void onGhostRecoveringComplete(StateTransition<State, GameEvent> t) {
		GhostRecoveringCompleteEvent e = event(t);
		e.ghost.setState(Ghost.State.ATTACKING);
	}
}