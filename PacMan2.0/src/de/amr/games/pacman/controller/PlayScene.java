package de.amr.games.pacman.controller;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.behavior.impl.Behaviors.ambush;
import static de.amr.games.pacman.behavior.impl.Behaviors.bounce;
import static de.amr.games.pacman.behavior.impl.Behaviors.chase;
import static de.amr.games.pacman.behavior.impl.Behaviors.flee;
import static de.amr.games.pacman.behavior.impl.Behaviors.followKeyboard;
import static de.amr.games.pacman.behavior.impl.Behaviors.forward;
import static de.amr.games.pacman.behavior.impl.Behaviors.goHome;
import static de.amr.games.pacman.behavior.impl.Behaviors.moody;
import static de.amr.games.pacman.behavior.impl.Behaviors.stayBehind;
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
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameEventListener;
import de.amr.games.pacman.controller.event.GhostContactEvent;
import de.amr.games.pacman.controller.event.GhostDeadIsOverEvent;
import de.amr.games.pacman.controller.event.GhostFrightenedEndsEvent;
import de.amr.games.pacman.controller.event.GhostRecoveringCompleteEvent;
import de.amr.games.pacman.controller.event.PacManDiedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Debug;
import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.HUD;
import de.amr.games.pacman.ui.MazeUI;
import de.amr.games.pacman.ui.PacMan;
import de.amr.games.pacman.ui.StatusUI;

public class PlayScene extends ActiveScene<PacManApp> implements GameEventListener {

	public enum State {
		READY, RUNNING, GAMEOVER
	};
	
	public enum Event {
		NEXT_LEVEL;
	}

	private StateMachine<State, Event> fsm;
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

		fsm.state(State.READY).entry = state -> {
			initEntities();
			enableEntities(false);
		};
		fsm.change(State.READY, State.RUNNING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_ENTER));

		fsm.state(State.RUNNING).entry = state -> {
			enableEntities(true);
		};
		fsm.state(State.RUNNING).update = state -> {
			updateEntities();
		};
		fsm.changeOnInput(Event.NEXT_LEVEL, State.RUNNING, State.RUNNING, t -> {
			++game.level;
			initLevel();
		});
		fsm.change(State.RUNNING, State.GAMEOVER, () -> game.lives == 0);

		fsm.state(State.GAMEOVER).entry = state -> {
			enableEntities(false);
		};
		fsm.change(State.GAMEOVER, State.READY, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		fsm.state(State.GAMEOVER).exit = state -> {
			game = new Game();
		};
	}

	@Override
	public void processGameEvent(GameEvent e) {
		if (e instanceof GhostContactEvent) {
			onGhostContact((GhostContactEvent) e);
		} else if (e instanceof FoodFoundEvent) {
			onFoodFound((FoodFoundEvent) e);
		} else if (e instanceof BonusFoundEvent) {
			onBonusFound((BonusFoundEvent) e);
		} else if (e instanceof PacManDiedEvent) {
			onPacManDied((PacManDiedEvent) e);
		} else if (e instanceof GhostFrightenedEndsEvent) {
			onGhostFrightenedEnds((GhostFrightenedEndsEvent) e);
		} else if (e instanceof GhostDeadIsOverEvent) {
			onGhostDeadIsOver((GhostDeadIsOverEvent) e);
		} else if (e instanceof GhostRecoveringCompleteEvent) {
			onGhostRecoveringComplete((GhostRecoveringCompleteEvent) e);
		}
	}

	public PacManApp getApp() {
		return app;
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

	private void createUI() {
		hud = new HUD(game);
		mazeUI = new MazeUI(getWidth(), getHeight() - 5 * TS, maze, pacMan, blinky, pinky, inky, clyde);
		status = new StatusUI(game);
		hud.tf.moveTo(0, 0);
		mazeUI.tf.moveTo(0, 3 * TS);
		status.tf.moveTo(0, getHeight() - 2 * TS);
	}

	@Override
	public void draw(Graphics2D g) {
		hud.draw(g);
		mazeUI.draw(g);
		status.draw(g);
		Debug.draw(g, this);
	}

	@Override
	public void init() {
		game = new Game();
		maze = Maze.of(Assets.text("maze.txt"));
		createEntities();
		createUI();
		fsm.init();
	}

	private void enableEntities(boolean enabled) {
		mazeUI.enableAnimation(enabled);
		pacMan.enableAnimation(enabled);
		getGhosts().forEach(ghost -> ghost.enableAnimation(enabled));
	}

	private void updateEntities() {
		mazeUI.update();
		pacMan.update();
		getGhosts().forEach(Ghost::update);
	}

	private void createEntities() {
		blinky = new Ghost(maze, "Blinky", RED_GHOST, maze.blinkyHome);
		pinky = new Ghost(maze, "Pinky", PINK_GHOST, maze.pinkyHome);
		inky = new Ghost(maze, "Inky", BLUE_GHOST, maze.inkyHome);
		clyde = new Ghost(maze, "Clyde", ORANGE_GHOST, maze.clydeHome);
		pacMan = new PacMan(maze, maze.pacManHome);
		pacMan.enemies.addAll(Arrays.asList(blinky, pinky, inky, clyde));

		getGhosts().forEach(ghost -> ghost.addObserver(this));
		pacMan.addObserver(this);

		// define move behavior
		pacMan.setMoveBehavior(PacMan.State.ALIVE, followKeyboard(VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));

		getGhosts().forEach(ghost -> {
			ghost.setMoveBehavior(Ghost.State.STARRED, forward());
			ghost.setMoveBehavior(Ghost.State.FRIGHTENED, flee(pacMan));
		});

		blinky.setMoveBehavior(Ghost.State.ATTACKING, chase(pacMan));
		blinky.setMoveBehavior(Ghost.State.DEAD, goHome());
		blinky.setMoveBehavior(Ghost.State.RECOVERING, goHome());

		pinky.setMoveBehavior(Ghost.State.ATTACKING, ambush(pacMan));
		pinky.setMoveBehavior(Ghost.State.DEAD, goHome());
		pinky.setMoveBehavior(Ghost.State.RECOVERING, bounce());

		inky.setMoveBehavior(Ghost.State.ATTACKING, moody());
		inky.setMoveBehavior(Ghost.State.DEAD, goHome());
		inky.setMoveBehavior(Ghost.State.RECOVERING, bounce());

		clyde.setMoveBehavior(Ghost.State.ATTACKING, stayBehind());
		clyde.setMoveBehavior(Ghost.State.DEAD, goHome());
		clyde.setMoveBehavior(Ghost.State.RECOVERING, bounce());
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

	private void initLevel() {
		maze.loadContent();
		game.dotsEatenInLevel = 0;
		initEntities();
	}

	// Game event handling

	/**
	 * Called on every clock tick.
	 */
	@Override
	public void update() {
		Debug.update(this);
		fsm.update();
	}

	private void onGhostContact(GhostContactEvent e) {
		switch (e.ghost.getState()) {
		case ATTACKING:
		case RECOVERING:
		case SCATTERING:
		case STARRED:
			pacMan.setState(PacMan.State.DYING);
			pacMan.enemies.forEach(enemy -> enemy.setState(Ghost.State.STARRED));
			game.lives -= 1;
			Debug.log(() -> String.format("PacMan got killed by %s at tile %s", e.ghost.getName(), e.ghost.getTile()));
			break;
		case DEAD:
			break;
		case FRIGHTENED:
			e.ghost.setState(Ghost.State.DEAD);
			Debug.log(() -> String.format("PacMan killed %s at tile %s", e.ghost.getName(), e.ghost.getTile()));
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void onPacManDied(PacManDiedEvent e) {
		initEntities();
	}

	private void onFoodFound(FoodFoundEvent e) {
		maze.setContent(e.tile, EMPTY);
		game.dotsEatenInLevel += 1;
		if (game.dotsEatenInLevel == 70) {
			maze.setContent(maze.bonusTile, Tile.BONUS_CHERRIES);
		} else if (game.dotsEatenInLevel == 170) {
			maze.setContent(maze.bonusTile, Tile.BONUS_STRAWBERRY);
		}
		game.score += e.food == ENERGIZER ? 50 : 10;
		if (maze.tiles().map(maze::getContent).noneMatch(Tile::isFood)) {
			fsm.addInput(Event.NEXT_LEVEL);
		} else if (e.food == ENERGIZER) {
			Debug.log(() -> String.format("PacMan found energizer at tile %s", e.tile));
			pacMan.enemies.stream().filter(ghost -> ghost.getState() != Ghost.State.DEAD)
					.forEach(enemy -> enemy.setState(Ghost.State.FRIGHTENED));
		}
	}

	private void onBonusFound(BonusFoundEvent e) {
		maze.setContent(e.tile, EMPTY);
		Debug.log(() -> String.format("PacMan found bonus %s at tile=%s", e.bonus, e.tile));
	}

	private void onGhostFrightenedEnds(GhostFrightenedEndsEvent e) {
		// TODO depends on currently running wave (scattering or attacking wave)
		e.ghost.setState(Ghost.State.ATTACKING);
	}

	private void onGhostDeadIsOver(GhostDeadIsOverEvent e) {
		e.ghost.setState(Ghost.State.RECOVERING);
		e.ghost.setMoveDirection(Top4.N);
	}

	private void onGhostRecoveringComplete(GhostRecoveringCompleteEvent e) {
		e.ghost.setState(Ghost.State.ATTACKING);
	}
}