package de.amr.games.pacman.controller;

import static de.amr.games.pacman.model.Tile.EMPTY;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;
import static de.amr.games.pacman.ui.Spritesheet.BLUE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.ORANGE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.PINK_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.RED_GHOST;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
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
	private Ghost[] ghosts;

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

	public Ghost[] getGhosts() {
		return ghosts;
	}

	@Override
	public View currentView() {
		return currentView;
	}

	@Override
	public void init() {
		game = new Game(Assets.text("maze.txt"));
		initLevel();
		currentView = new PlayScene(this);
	}

	private void initLevel() {
		game.maze.reset();
		initEntities();
	}

	private void initEntities() {
		ghosts = new Ghost[4];
		ghosts[RED_GHOST] = new Ghost(game.maze, RED_GHOST);
		ghosts[RED_GHOST].setMazePosition(ChaserMoveBehavior.HOME);
		ghosts[RED_GHOST].setMoveDirection(Top4.E);
		ghosts[PINK_GHOST] = new Ghost(game.maze, PINK_GHOST);
		ghosts[PINK_GHOST].setMazePosition(13, 14);
		ghosts[PINK_GHOST].setMoveDirection(Top4.S);
		ghosts[BLUE_GHOST] = new Ghost(game.maze, BLUE_GHOST);
		ghosts[BLUE_GHOST].setMazePosition(11, 14);
		ghosts[BLUE_GHOST].setMoveDirection(Top4.N);
		ghosts[ORANGE_GHOST] = new Ghost(game.maze, ORANGE_GHOST);
		ghosts[ORANGE_GHOST].setMazePosition(15, 14);
		ghosts[ORANGE_GHOST].setMoveDirection(Top4.N);
		Stream.of(ghosts).forEach(ghost -> {
			ghost.setSpeed(PacManApp.TS / 16f);
			ghost.setState(Ghost.State.ATTACKING);
			ghost.addObserver(this);
		});

		pacMan = new PacMan(game.maze);
		pacMan.setMazePosition(14, 23);
		pacMan.setSpeed(PacManApp.TS / 8f);
		pacMan.setMoveDirection(Top4.E);
		pacMan.setNextMoveDirection(Top4.E);
		pacMan.setState(State.ALIVE);
		pacMan.enemies.addAll(Arrays.asList(ghosts));
		pacMan.addObserver(this);

		// behavior
		ghosts[RED_GHOST].setMoveBehavior(new ChaserMoveBehavior(game.maze, ghosts[RED_GHOST], pacMan));
		ghosts[PINK_GHOST].setMoveBehavior(new AmbusherMoveBehavior(ghosts[PINK_GHOST]));
		ghosts[BLUE_GHOST].setMoveBehavior(new MoodyMoveBehavior(ghosts[BLUE_GHOST]));
		// ghosts[ORANGE_GHOST].setMoveBehavior(new LackingBehindMoveBehavior(ghosts[ORANGE_GHOST]));
		ghosts[ORANGE_GHOST].setMoveBehavior(new KeyboardSteering(pacMan, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD6,
				KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD4));
		pacMan.setMoveBehavior(
				new KeyboardSteering(pacMan, KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT));
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
		Arrays.stream(ghosts).forEach(Ghost::update);
	}

	private void onPacManDied(PacManDiedEvent e) {
		initEntities();
	}

	private void onGhostContact(GhostContactEvent e) {
		if (pacMan.getState() != PacMan.State.DYING) {
			if (e.ghost.getState() == Ghost.State.FRIGHTENED) {
				e.ghost.setState(Ghost.State.DEAD);
				debug(() -> System.out.println(String.format("Killed %s at col=%d, row=%d", e.ghost, e.col, e.row)));
			} else if (e.ghost.getState() == Ghost.State.DEAD) {
				// do nothing
			} else {
				pacMan.setState(State.DYING);
				pacMan.setSpeed(0);
				pacMan.enemies.forEach(enemy -> {
					enemy.setSpeed(0);
					enemy.setAnimated(false);
					enemy.setState(Ghost.State.STARRED);
				});
				game.lives -= 1;
				debug(() -> System.out.println(String.format("Got killed by %s at col=%d, row=%d", e.ghost, e.col, e.row)));
			}
		}
	}

	private void onFoodFound(FoodFoundEvent e) {
		game.maze.setContent(new Tile(e.col, e.row), EMPTY);
		if (game.maze.tiles().map(game.maze::getContent).noneMatch(c -> c == PELLET || c == ENERGIZER)) {
			++game.level;
			initLevel();
			return;
		}
		if (e.food == ENERGIZER) {
			debug(() -> System.out.println(String.format("Eat energizer at col=%d, row=%d", e.col, e.row)));
			game.score += 50;
			pacMan.enemies.forEach(enemy -> enemy.setState(Ghost.State.FRIGHTENED));
		} else {
			game.score += 10;
		}
	}

	private void onBonusFound(BonusFoundEvent e) {
		game.maze.setContent(new Tile(e.col, e.row), EMPTY);
		debug(() -> System.out.println(String.format("Found bonus %s at col=%d, row=%d", e.bonus, e.col, e.row)));
	}
}