package de.amr.games.pacman.controller;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Tile.EMPTY;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;
import static de.amr.games.pacman.ui.Spritesheet.BLUE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.ORANGE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.PINK_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.RED_GHOST;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.behavior.AmbushTarget;
import de.amr.games.pacman.controller.behavior.ChaseTarget;
import de.amr.games.pacman.controller.behavior.DoNothing;
import de.amr.games.pacman.controller.behavior.Flee;
import de.amr.games.pacman.controller.behavior.GoHome;
import de.amr.games.pacman.controller.behavior.KeyboardSteering;
import de.amr.games.pacman.controller.behavior.LackingBehindMoveBehavior;
import de.amr.games.pacman.controller.behavior.MoodyMoveBehavior;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameEventListener;
import de.amr.games.pacman.controller.event.GhostContactEvent;
import de.amr.games.pacman.controller.event.PacManDiedEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.PacMan;
import de.amr.games.pacman.ui.PacMan.State;
import de.amr.games.pacman.ui.PlayScene;

public class GameController implements Controller, GameEventListener {

	private static boolean DEBUG;

	public static void debug(Runnable code) {
		if (DEBUG) {
			code.run();
		}
	}

	private final PacManApp app;
	private Game game;
	private View currentView;
	private PacMan pacMan;
	private Ghost blinky, pinky, inky, clyde;

	public GameController(PacManApp app) {
		this.app = app;
	}

	public PacManApp getApp() {
		return app;
	}

	public Game getGame() {
		return game;
	}

	public PacMan getPacMan() {
		return pacMan;
	}

	public Stream<Ghost> getGhosts() {
		return Stream.of(blinky, pinky, inky, clyde);
	}

	@Override
	public View currentView() {
		return currentView;
	}

	@Override
	public void init() {
		game = new Game(Maze.of(Assets.text("maze.txt")));
		initLevel();
		currentView = new PlayScene(this);
	}

	private void initLevel() {
		game.maze.reset();
		initEntities();
	}

	private void initEntities() {
		blinky = new Ghost(game.maze, "Blinky", RED_GHOST);
		blinky.setTile(Maze.BLINKY_HOME);
		blinky.setMoveDirection(Top4.E);

		pinky = new Ghost(game.maze, "Pinky", PINK_GHOST);
		pinky.setTile(Maze.PINKY_HOME);
		pinky.setMoveDirection(Top4.S);

		inky = new Ghost(game.maze, "Inky", BLUE_GHOST);
		inky.setTile(Maze.INKY_HOME);
		inky.setMoveDirection(Top4.N);

		clyde = new Ghost(game.maze, "Clyde", ORANGE_GHOST);
		clyde.setTile(Maze.CLYDE_HOME);
		clyde.setMoveDirection(Top4.N);

		getGhosts().forEach(ghost -> {
			ghost.setSpeed(PacManApp.TS / 16f);
			ghost.setState(Ghost.State.ATTACKING);
			ghost.addObserver(this);
		});

		pacMan = new PacMan(game.maze, "Pac-Man");
		pacMan.setTile(Maze.PACMAN_HOME);
		pacMan.setSpeed(TS / 8f);
		pacMan.setMoveDirection(Top4.E);
		pacMan.setNextMoveDirection(Top4.E);
		pacMan.setState(State.ALIVE);
		pacMan.addObserver(this);
		getGhosts().forEach(pacMan::addEnemy);

		// define move behavior
		getGhosts().forEach(ghost -> {
			ghost.setMoveBehavior(Ghost.State.STARRED, new DoNothing(ghost));
			ghost.setMoveBehavior(Ghost.State.FRIGHTENED, new Flee(game.maze, ghost, pacMan));
		});
		blinky.setMoveBehavior(Ghost.State.ATTACKING, new ChaseTarget(game.maze, blinky, pacMan));
		blinky.setMoveBehavior(Ghost.State.DEAD, new GoHome(game.maze, blinky, Maze.BLINKY_HOME));
		pinky.setMoveBehavior(Ghost.State.ATTACKING, new AmbushTarget(game.maze, pinky, pacMan));
		pinky.setMoveBehavior(Ghost.State.DEAD, new GoHome(game.maze, pinky, Maze.PINKY_HOME));
		inky.setMoveBehavior(Ghost.State.ATTACKING, new MoodyMoveBehavior(inky));
		inky.setMoveBehavior(Ghost.State.DEAD, new GoHome(game.maze, inky, Maze.INKY_HOME));
		clyde.setMoveBehavior(Ghost.State.ATTACKING, new LackingBehindMoveBehavior(clyde));
		clyde.setMoveBehavior(Ghost.State.DEAD, new GoHome(game.maze, clyde, Maze.CLYDE_HOME));

		pacMan.setMoveBehavior(PacMan.State.ALIVE, new KeyboardSteering(pacMan, VK_UP, VK_RIGHT, VK_DOWN, VK_LEFT));
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
		}
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_D)) {
			DEBUG = !DEBUG;
		}
		pacMan.update();
		getGhosts().forEach(Ghost::update);
	}

	private void onPacManDied(PacManDiedEvent e) {
		initEntities();
	}

	private void onGhostContact(GhostContactEvent e) {
		if (pacMan.getState() != PacMan.State.DYING) {
			if (e.ghost.getState() == Ghost.State.FRIGHTENED) {
				e.ghost.setState(Ghost.State.DEAD);
				debug(() -> System.out.println(String.format("Killed %s at tile %s", e.ghost.getName(), e.tile)));
			} else if (e.ghost.getState() == Ghost.State.DEAD) {
				// do nothing
			} else {
				pacMan.setState(State.DYING);
				pacMan.setSpeed(0);
				pacMan.enemies().forEach(enemy -> {
					enemy.setSpeed(0);
					enemy.setAnimated(false);
					enemy.setState(Ghost.State.STARRED);
				});
				game.lives -= 1;
				debug(() -> System.out.println(String.format("Got killed by %s at tile %s", e.ghost.getName(), e.tile)));
			}
		}
	}

	private void onFoodFound(FoodFoundEvent e) {
		game.maze.setContent(e.tile, EMPTY);
		if (e.food == ENERGIZER) {
			debug(() -> System.out.println(String.format("Eat energizer at tile %s", e.tile)));
			game.score += 50;
			pacMan.enemies().forEach(enemy -> enemy.setState(Ghost.State.FRIGHTENED));
		} else if (e.food == PELLET) {
			game.score += 10;
		}
		if (game.maze.tiles().map(game.maze::getContent).noneMatch(c -> c == PELLET || c == ENERGIZER)) {
			++game.level;
			initLevel();
			return;
		}
	}

	private void onBonusFound(BonusFoundEvent e) {
		game.maze.setContent(e.tile, EMPTY);
		debug(() -> System.out.println(String.format("Found bonus %s at tile=%s", e.bonus, e.tile)));
	}
}