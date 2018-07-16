package de.amr.games.pacman.controller;

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
import de.amr.games.pacman.ui.MazeMover;
import de.amr.games.pacman.ui.PacMan;
import de.amr.games.pacman.ui.PacMan.State;
import de.amr.games.pacman.ui.PlayScene;

public class GameController implements Controller, GameEventListener {

	private static boolean DEBUG;

	public static void whenDebugging(Runnable code) {
		if (DEBUG) {
			code.run();
		}
	}

	public final PacManApp app;

	private Game game;
	private View currentView;
	private PacMan pacMan;
	private Ghost[] ghosts;

	public GameController(final PacManApp app) {
		this.app = app;
	}

	public Game getGame() {
		return game;
	}

	public MazeMover<State> getPacMan() {
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
		currentView = new PlayScene(this);
		startLevel();
	}

	private void startLevel() {
		game.maze.reset();
		initEntities();
		System.out.println("Started level " + game.level);
	}

	private void initEntities() {
		ghosts = new Ghost[4];
		ghosts[RED_GHOST] = new Ghost(game, RED_GHOST);
		ghosts[RED_GHOST].setMazePosition(BlinkyBrain.HOME);
		ghosts[RED_GHOST].setMoveDirection(Top4.E);
		ghosts[PINK_GHOST] = new Ghost(game, PINK_GHOST);
		ghosts[PINK_GHOST].setMazePosition(13, 14);
		ghosts[PINK_GHOST].setMoveDirection(Top4.S);
		ghosts[BLUE_GHOST] = new Ghost(game, BLUE_GHOST);
		ghosts[BLUE_GHOST].setMazePosition(11, 14);
		ghosts[BLUE_GHOST].setMoveDirection(Top4.N);
		ghosts[ORANGE_GHOST] = new Ghost(game, ORANGE_GHOST);
		ghosts[ORANGE_GHOST].setMazePosition(15, 14);
		ghosts[ORANGE_GHOST].setMoveDirection(Top4.N);
		Stream.of(ghosts).forEach(ghost -> {
			ghost.setSpeed(PacManApp.TS / 16f);
			ghost.setState(Ghost.State.ATTACKING);
			ghost.addObserver(this);
		});

		pacMan = new PacMan(game);
		pacMan.setMazePosition(14, 23);
		pacMan.setSpeed(PacManApp.TS / 8f);
		pacMan.setMoveDirection(Top4.E);
		pacMan.setNextMoveDirection(Top4.E);
		pacMan.setState(State.ALIVE);
		pacMan.enemies.addAll(Arrays.asList(ghosts));
		pacMan.addObserver(this);

		// Wirf Hirn...
		ghosts[RED_GHOST].setBrain(new BlinkyBrain(ghosts[RED_GHOST], pacMan, game.maze));
		ghosts[PINK_GHOST].setBrain(new PinkyBrain(ghosts[PINK_GHOST]));
		ghosts[BLUE_GHOST].setBrain(new InkyBrain(ghosts[BLUE_GHOST]));
		ghosts[ORANGE_GHOST].setBrain(new ClydeBrain(ghosts[ORANGE_GHOST]));
		pacMan.setBrain(new PacManBrain(pacMan));
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
		pacMan.spriteDying.resetAnimation(); // TODO
		initEntities();
	}

	private void onGhostContact(GhostContactEvent e) {
		if (pacMan.getState() != PacMan.State.DYING) {
			if (e.ghost.getState() == Ghost.State.FRIGHTENED) {
				e.ghost.setState(Ghost.State.DEAD);
				System.out.println(String.format("Killed %s at col=%d, row=%d", e.ghost, e.col, e.row));
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
				System.out.println(String.format("Got killed by %s at col=%d, row=%d", e.ghost, e.col, e.row));
			}
		}
	}

	private void onFoodFound(FoodFoundEvent e) {
		game.maze.setContent(e.col, e.row, Tile.EMPTY);
		if (!game.maze.containsFood()) {
			++game.level;
			startLevel();
			return;
		}
		if (e.food == Tile.ENERGIZER) {
			System.out.println(String.format("Eat energizer at col=%d, row=%d", e.col, e.row));
			game.score += 50;
			pacMan.enemies.forEach(enemy -> enemy.setState(Ghost.State.FRIGHTENED));
		} else {
			game.score += 10;
		}
	}

	private void onBonusFound(BonusFoundEvent e) {
		System.out.println(String.format("Found bonus %s at col=%d, row=%d", e.bonus, e.col, e.row));
		game.maze.setContent(e.col, e.row, Tile.EMPTY);
	}
}