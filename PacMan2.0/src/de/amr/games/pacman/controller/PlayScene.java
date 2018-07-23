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
import java.util.Optional;
import java.util.logging.Logger;
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
import de.amr.statemachine.StateMachine;

public class PlayScene extends ActiveScene<PacManApp> implements GameEventListener {

	public enum State {
		READY, RUNNING, KILLING_GHOST, GAMEOVER
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
		fsm.setLogger(Logger.getGlobal());
	}

	@Override
	public void onGameEvent(GameEvent e) {
		fsm.addInput(e);
	}

	private void defineStateMachine() {
		fsm = new StateMachine<>("Play scene control", State.class, State.READY);

		// -- READY

		fsm.state(State.READY).entry = state -> {
			initEntities();
			enableEntities(false);
			mazeUI.showReadyText(true);
		};
		fsm.state(State.READY).exit = state -> {
			enableEntities(true);
			mazeUI.showReadyText(false);
		};
		fsm.change(State.READY, State.RUNNING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_ENTER));

		// -- RUNNING

		fsm.state(State.RUNNING).update = state -> {
			updateEntities();
		};
		fsm.changeOnInput(NextLevelEvent.class, State.RUNNING, State.RUNNING, t -> {
			nextLevel();
		});

		fsm.change(State.RUNNING, State.GAMEOVER, () -> game.lives == 0);

		fsm.changeOnInput(GhostContactEvent.class, State.RUNNING, State.RUNNING, t -> {
			onGhostContact(t.getInput());
		});

		fsm.changeOnInput(GhostKilledEvent.class, State.RUNNING, State.KILLING_GHOST, t -> {
			onGhostKilled(t.getInput());
		});

		fsm.changeOnInput(FoodFoundEvent.class, State.RUNNING, State.RUNNING, t -> {
			onFoodFound((FoodFoundEvent) t.getInput().get());
		});

		fsm.changeOnInput(BonusFoundEvent.class, State.RUNNING, State.RUNNING, t -> {
			onBonusFound(t.getInput());
		});

		fsm.changeOnInput(GhostFrightenedEndsEvent.class, State.RUNNING, State.RUNNING, t -> {
			onGhostFrightenedEnds(t.getInput());
		});

		fsm.changeOnInput(GhostDeadIsOverEvent.class, State.RUNNING, State.RUNNING, t -> {
			onGhostDeadIsOver(t.getInput());
		});

		fsm.changeOnInput(GhostRecoveringCompleteEvent.class, State.RUNNING, State.RUNNING, t -> {
			onGhostRecoveringComplete(t.getInput());
		});

		fsm.changeOnInput(PacManDiedEvent.class, State.RUNNING, State.RUNNING, () -> game.lives > 0, t -> {
			onPacManDied(t.getInput());
		});

		fsm.changeOnInput(PacManDiedEvent.class, State.RUNNING, State.GAMEOVER, () -> game.lives == 0, t -> {
			onPacManDied(t.getInput());
		});

		// -- KILLING_GHOST

		fsm.state(State.KILLING_GHOST).entry = state -> {
			state.setDuration(60);
		};
		fsm.changeOnTimeout(State.KILLING_GHOST, State.RUNNING);
		fsm.state(State.KILLING_GHOST).exit = state -> {
			mazeUI.hideGhostPoints();
		};

		// -- GAME_OVER

		fsm.state(State.GAMEOVER).entry = state -> {
			enableEntities(false);
			mazeUI.showGameOverText(true);
		};
		fsm.change(State.GAMEOVER, State.READY, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		fsm.state(State.GAMEOVER).exit = state -> {
			mazeUI.showGameOverText(false);
			game = new Game();
		};
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

	private void createUI() {
		hud = new HUD(game);
		mazeUI = new MazeUI(getWidth(), getHeight() - 5 * TS, maze, pacMan, blinky, pinky, inky, clyde);
		status = new StatusUI(game);
		hud.tf.moveTo(0, 0);
		mazeUI.tf.moveTo(0, 3 * TS);
		status.tf.moveTo(0, getHeight() - 2 * TS);
	}

	@Override
	public void init() {
		game = new Game();
		maze = Maze.of(Assets.text("maze.txt"));
		createEntities();
		createUI();
		fsm.init();
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

	// Game event handling

	/**
	 * Called on every clock tick.
	 */
	@Override
	public void update() {
		Debug.update(this);
		fsm.update();
	}

	private void updateEntities() {
		mazeUI.update();
		pacMan.update();
		getGhosts().forEach(Ghost::update);
	}

	private void enableEntities(boolean enabled) {
		mazeUI.enableAnimation(enabled);
		pacMan.enableAnimation(enabled);
		getGhosts().forEach(ghost -> ghost.enableAnimation(enabled));
	}

	private void nextLevel() {
		++game.level;
		game.dotsEatenInLevel = 0;
		game.deadGhostScore = 0;
		maze.loadContent();
		maze.setContent(maze.bonusTile, Tile.EMPTY);
		initEntities();
	}

	// Game event handling

	private void onGhostContact(Optional<GameEvent> optEvent) {
		GhostContactEvent e = (GhostContactEvent) optEvent.get();
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
			fsm.addInput(new GhostKilledEvent(e.ghost));
			Debug.log(() -> String.format("Ghost %s got killed at tile %s", e.ghost.getName(), e.ghost.getTile()));
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void onGhostKilled(Optional<GameEvent> optEvent) {
		GhostKilledEvent e = (GhostKilledEvent) optEvent.get();
		e.ghost.setState(Ghost.State.DEAD);
		game.deadGhostScore = game.deadGhostScore == 0 ? 200 : 2 * game.deadGhostScore;
		game.score += game.deadGhostScore;
		mazeUI.showGhostPoints(e.ghost, game.deadGhostScore);
	}

	private void onPacManDied(Optional<GameEvent> optEvent) {
		initEntities();
		maze.setContent(maze.bonusTile, Tile.EMPTY);
	}

	private void onFoodFound(FoodFoundEvent e) {
		maze.setContent(e.tile, EMPTY);
		game.dotsEatenInLevel += 1;
		if (game.dotsEatenInLevel == 70) {
			maze.setContent(maze.bonusTile, Tile.BONUS_CHERRIES);
		} else if (game.dotsEatenInLevel == 170) {
			maze.setContent(maze.bonusTile, Tile.BONUS_STRAWBERRY);
		}
		if (e.food == ENERGIZER) {
			game.score += 50;
			game.deadGhostScore = 0;
		} else {
			game.score += 10;
		}
		if (maze.tiles().map(maze::getContent).noneMatch(Tile::isFood)) {
			fsm.addInput(new NextLevelEvent());
		} else if (e.food == ENERGIZER) {
			Debug.log(() -> String.format("PacMan found energizer at tile %s", e.tile));
			pacMan.enemies.stream().filter(ghost -> ghost.getState() != Ghost.State.DEAD)
					.forEach(enemy -> enemy.setState(Ghost.State.FRIGHTENED));
		}
	}

	private void onBonusFound(Optional<GameEvent> optEvent) {
		BonusFoundEvent e = (BonusFoundEvent) optEvent.get();
		maze.setContent(e.tile, EMPTY);
		Debug.log(() -> String.format("PacMan found bonus %s at tile=%s", e.bonus, e.tile));
	}

	private void onGhostFrightenedEnds(Optional<GameEvent> optEvent) {
		GhostFrightenedEndsEvent e = (GhostFrightenedEndsEvent) optEvent.get();
		// TODO depends on currently running wave (scattering or attacking wave)
		e.ghost.setState(Ghost.State.ATTACKING);
	}

	private void onGhostDeadIsOver(Optional<GameEvent> optEvent) {
		GhostDeadIsOverEvent e = (GhostDeadIsOverEvent) optEvent.get();
		e.ghost.setState(Ghost.State.RECOVERING);
		e.ghost.setMoveDirection(Top4.N);
	}

	private void onGhostRecoveringComplete(Optional<GameEvent> optEvent) {
		GhostRecoveringCompleteEvent e = (GhostRecoveringCompleteEvent) optEvent.get();
		e.ghost.setState(Ghost.State.ATTACKING);
	}
}