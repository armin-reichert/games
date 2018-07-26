package de.amr.games.pacman.controller;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.behavior.impl.Behaviors.ambush;
import static de.amr.games.pacman.behavior.impl.Behaviors.bounce;
import static de.amr.games.pacman.behavior.impl.Behaviors.chase;
import static de.amr.games.pacman.behavior.impl.Behaviors.flee;
import static de.amr.games.pacman.behavior.impl.Behaviors.followKeyboard;
import static de.amr.games.pacman.behavior.impl.Behaviors.goHome;
import static de.amr.games.pacman.model.Tile.EMPTY;
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
import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
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
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.GhostRecoveringCompleteEvent;
import de.amr.games.pacman.controller.event.NextLevelEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Bonus;
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
		READY, RUNNING, DYING, CHANGING_LEVEL, GAMEOVER
	};

	private StateMachine<State, GameEvent> fsm;
	private Game game;
	private Maze maze;
	private PacMan pacMan;
	private Ghost blinky, pinky, inky, clyde;
	private MazeUI mazeUI;
	private HUD hud;
	private StatusUI status;

	public PlayScene(PacManApp app) {
		super(app);
		defineStateMachine();
	}

	private void defineStateMachine() {
		fsm = new StateMachine<>("Play scene control", State.class, State.READY);
		fsm.fnFrequency = () -> app.pulse.getFrequency();

		// -- READY

		fsm.state(State.READY).entry = state -> {
			state.setDuration(sec(2));
			maze = new Maze(Assets.text("maze.txt"));
			game = new Game();
			game.totalDotsInLevel = maze.tiles().map(maze::getContent).filter(Tile::isFood).count();
			createUI(game, maze);
			createPacManAndFriends();
			mazeUI.populate(pacMan, blinky, pinky, inky, clyde);
			initEntities();
			animateEntities(false);
			mazeUI.showText("Ready!");
		};

		fsm.state(State.READY).exit = state -> {
			animateEntities(true);
			mazeUI.hideText();
		};

		fsm.changeOnTimeout(State.READY, State.RUNNING);

		// -- RUNNING

		fsm.state(State.RUNNING).update = state -> {
			updateEntities();
		};

		fsm.changeOnInput(FoodFoundEvent.class, State.RUNNING, State.RUNNING, this::onFoodFound);

		fsm.changeOnInput(BonusFoundEvent.class, State.RUNNING, State.RUNNING, this::onBonusFound);

		fsm.changeOnInput(GhostFrightenedEndsEvent.class, State.RUNNING, State.RUNNING,
				this::onGhostFrightenedEnds);

		fsm.changeOnInput(GhostDeadIsOverEvent.class, State.RUNNING, State.RUNNING,
				this::onGhostDeadIsOver);

		fsm.changeOnInput(GhostRecoveringCompleteEvent.class, State.RUNNING, State.RUNNING,
				this::onGhostRecoveringComplete);

		fsm.changeOnInput(GhostContactEvent.class, State.RUNNING, State.RUNNING, this::onGhostContact);

		fsm.changeOnInput(GhostKilledEvent.class, State.RUNNING, State.RUNNING, this::onGhostKilled);

		fsm.changeOnInput(PacManKilledEvent.class, State.RUNNING, State.DYING, this::onPacManKilled);

		fsm.changeOnInput(NextLevelEvent.class, State.RUNNING, State.CHANGING_LEVEL);

		// -- CHANGING_LEVEL

		fsm.state(State.CHANGING_LEVEL).entry = state -> {
			state.setDuration(sec(3));
			initNextLevel();
			mazeUI.setFlashing(true);
		};

		fsm.state(State.CHANGING_LEVEL).update = state -> {
			if (state.getRemaining() < sec(2)) {
				mazeUI.setFlashing(false);
			}
		};

		fsm.changeOnTimeout(State.CHANGING_LEVEL, State.RUNNING);

		// -- DYING

		fsm.state(State.DYING).entry = state -> {
			state.setDuration(sec(3));
			pacMan.setState(PacMan.State.DYING);
			game.lives -= 1;
		};

		fsm.changeOnTimeout(State.DYING, State.GAMEOVER, () -> game.lives == 0);

		fsm.changeOnTimeout(State.DYING, State.RUNNING, () -> game.lives > 0, t -> {
			pacMan.currentSprite().resetAnimation();
			initEntities();
		});

		// -- GAME_OVER

		fsm.state(State.GAMEOVER).entry = state -> {
			animateEntities(false);
			mazeUI.showText("Game Over!");
		};

		fsm.change(State.GAMEOVER, State.READY, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

		fsm.state(State.GAMEOVER).exit = state -> {
			mazeUI.hideText();
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

	public PacManApp getApp() {
		return app;
	}

	public StateMachine<State, GameEvent> getFsm() {
		return fsm;
	}

	public Game getGame() {
		return game;
	}

	public MazeUI getMazeUI() {
		return mazeUI;
	}

	public PacMan getPacMan() {
		return pacMan;
	}

	public Stream<Ghost> getGhosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	public int sec(float seconds) {
		return app.pulse.secToTicks(seconds);
	}

	private void createPacManAndFriends() {
		blinky = new Ghost(maze, "Blinky", RED_GHOST, maze.blinkyHome);
		pinky = new Ghost(maze, "Pinky", PINK_GHOST, maze.pinkyHome);
		inky = new Ghost(maze, "Inky", BLUE_GHOST, maze.inkyHome);
		clyde = new Ghost(maze, "Clyde", ORANGE_GHOST, maze.clydeHome);
		pacMan = new PacMan(maze, maze.pacManHome);
		pacMan.interestingThings.addAll(Arrays.asList(blinky, pinky, inky, clyde));

		getGhosts().forEach(ghost -> ghost.observers.addObserver(this));
		pacMan.observers.addObserver(this);

		// define move behavior
		pacMan.setMoveBehavior(PacMan.State.ALIVE, followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
		getGhosts().forEach(ghost -> {
			ghost.setMoveBehavior(Ghost.State.FRIGHTENED, flee(pacMan));
			ghost.setMoveBehavior(Ghost.State.DEAD, goHome());
			ghost.setMoveBehavior(Ghost.State.RECOVERING, bounce());
		});
		blinky.setMoveBehavior(Ghost.State.ATTACKING, chase(pacMan));
		pinky.setMoveBehavior(Ghost.State.ATTACKING, ambush(pacMan));
		// inky.setMoveBehavior(Ghost.State.ATTACKING, moody());
		// clyde.setMoveBehavior(Ghost.State.ATTACKING, stayBehind());
	}

	private void createUI(Game game, Maze maze) {
		mazeUI = new MazeUI(maze);
		hud = new HUD(game);
		status = new StatusUI(game);
		// layout
		hud.tf.moveTo(0, 0);
		mazeUI.tf.moveTo(0, 3 * TS);
		status.tf.moveTo(0, (3 + maze.numRows()) * TS);
		// events
		mazeUI.observers.addObserver(this);
	}

	private void initEntities() {
		pacMan.setState(PacMan.State.ALIVE);
		pacMan.setTile(maze.pacManHome);
		pacMan.setSpeed(game::getPacManSpeed);
		pacMan.setMoveDirection(Top4.E);
		pacMan.setNextMoveDirection(Top4.E);

		blinky.setMoveDirection(Top4.E);
		pinky.setMoveDirection(Top4.S);
		inky.setMoveDirection(Top4.N);
		clyde.setMoveDirection(Top4.N);

		getGhosts().forEach(ghost -> {
			ghost.setState(Ghost.State.RECOVERING);
			ghost.setTile(ghost.getHome());
			ghost.setSpeed(game::getGhostSpeed);
		});
	}

	private void updateEntities() {
		mazeUI.update();
		pacMan.update();
		getGhosts().forEach(Ghost::update);
	}

	private void animateEntities(boolean enabled) {
		mazeUI.enableAnimation(enabled);
		pacMan.enableAnimation(enabled);
		getGhosts().forEach(ghost -> ghost.enableAnimation(enabled));
	}

	private void initNextLevel() {
		game.level += 1;
		maze.reset();
		game.totalDotsInLevel = maze.tiles().map(maze::getContent).filter(Tile::isFood).count();
		game.dotsEaten = 0;
		game.ghostPoints = 0;
		initEntities();
	}

	private void startHuntingGhosts() {
		getGhosts().filter(ghost -> ghost.getState() != Ghost.State.DEAD)
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
			fsm.enqueue(new GhostKilledEvent(e.ghost));
			break;
		case DYING:
		case DEAD:
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void onPacManKilled(StateTransition<State, GameEvent> t) {
		PacManKilledEvent e = event(t);
		Debug.log(() -> String.format("PacMan got killed by %s at tile %s", e.ghost.getName(),
				e.ghost.getTile()));
	}

	private void onGhostKilled(StateTransition<State, GameEvent> t) {
		GhostKilledEvent e = event(t);
		Debug.log(() -> String.format("Ghost %s got killed at tile %s", e.ghost.getName(),
				e.ghost.getTile()));
		e.ghost.kill(game.ghostPoints, sec(1));
		game.score += game.ghostPoints;
		game.ghostPoints *= 2;
	}

	private void onFoodFound(StateTransition<State, GameEvent> t) {
		FoodFoundEvent e = event(t);
		maze.setContent(e.tile, EMPTY);
		game.dotsEaten += 1;
		if (game.dotsEaten == 70) {
			mazeUI.showBonus(new Bonus(Tile.BONUS_CHERRIES, 100), sec(5));
		} else if (game.dotsEaten == 170) {
			mazeUI.showBonus(new Bonus(Tile.BONUS_STRAWBERRY, 100), sec(5));
		}
		if (e.food == ENERGIZER) {
			game.score += 50;
			game.ghostPoints = 200;
		} else {
			game.score += 10;
		}
		if (game.dotsEaten == game.totalDotsInLevel) {
			fsm.enqueue(new NextLevelEvent());
		} else if (e.food == ENERGIZER) {
			Debug.log(() -> String.format("PacMan found energizer at tile %s", e.tile));
			startHuntingGhosts();
		}
	}

	private void onBonusFound(StateTransition<State, GameEvent> t) {
		BonusFoundEvent e = event(t);
		Debug.log(() -> String.format("PacMan found bonus %s at tile=%s", e.bonus, e.tile));
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
		e.ghost.setMoveDirection(Top4.N);
	}

	private void onGhostRecoveringComplete(StateTransition<State, GameEvent> t) {
		GhostRecoveringCompleteEvent e = event(t);
		e.ghost.setState(Ghost.State.ATTACKING);
	}
}