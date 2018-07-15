package de.amr.games.pacman.controller;

import static de.amr.games.pacman.ui.Spritesheet.BLUE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.ORANGE_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.PINK_GHOST;
import static de.amr.games.pacman.ui.Spritesheet.RED_GHOST;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.GameState;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.MazeUI;
import de.amr.games.pacman.ui.PacMan;
import de.amr.games.pacman.ui.PacMan.State;

public class PlayControl implements GameEventListener {

	private final GameState gameState;
	private final PacMan pacMan;
	private final Ghost[] ghosts;
	private int currentLevel;

	public PlayControl(GameState gameState, MazeUI maze) {
		this.gameState = gameState;
		this.pacMan = maze.pacMan;
		this.ghosts = maze.ghosts;
		pacMan.addObserver(this);
		Stream.of(ghosts).forEach(ghost -> ghost.addObserver(this));
	}

	@Override
	public void dispatch(GameEvent e) {
		if (e instanceof GhostContactEvent) {
			onGhostContact((GhostContactEvent) e);
		} else if (e instanceof FoodFoundEvent) {
			onFoodFound((FoodFoundEvent) e);
		} else if (e instanceof BonusFoundEvent) {
			onBonusFound((BonusFoundEvent) e);
		} else if (e instanceof StartLevelEvent) {
			onNewLevel((StartLevelEvent) e);
		} else if (e instanceof PacManDiedEvent) {
			onPacManDied((PacManDiedEvent) e);
		}
	}

	public void update() {
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			pacMan.setNextMoveDirection(Top4.W);
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			pacMan.setNextMoveDirection(Top4.E);
		} else if (Keyboard.keyDown(KeyEvent.VK_DOWN)) {
			pacMan.setNextMoveDirection(Top4.S);
		} else if (Keyboard.keyDown(KeyEvent.VK_UP)) {
			pacMan.setNextMoveDirection(Top4.N);
		}
		pacMan.update();
		Arrays.stream(ghosts).forEach(Ghost::update);
	}

	private void onPacManDied(PacManDiedEvent e) {
		pacMan.spriteDying.resetAnimation();
		initEntities();
	}

	private void onGhostContact(GhostContactEvent e) {
		System.out.println(String.format("Collision with %s at col=%d, row=%d", e.ghost, e.col, e.row));
		if (pacMan.getState() != PacMan.State.DYING) {
			if (e.ghost.getState() == Ghost.State.FRIGHTENED) {
				e.ghost.setState(Ghost.State.DEAD);
			} else if (e.ghost.getState() == Ghost.State.DEAD) {
				// do nothing
			} else {
				pacMan.setState(State.DYING);
				pacMan.setSpeed(0);
				pacMan.enemies.forEach(enemy -> enemy.setSpeed(0));
				gameState.lives -= 1;
			}
		}
	}

	private void onFoodFound(FoodFoundEvent e) {
		gameState.maze.setContent(e.col, e.row, Tile.EMPTY);
		if (gameState.maze.isMazeEmpty()) {
			onNewLevel(new StartLevelEvent(currentLevel + 1));
			return;
		}
		if (e.food == Tile.ENERGIZER) {
			System.out.println(String.format("Eat energizer at col=%d, row=%d", e.col, e.row));
			pacMan.enemies.forEach(enemy -> enemy.setState(Ghost.State.FRIGHTENED));
		}
	}

	private void onBonusFound(BonusFoundEvent e) {
		System.out.println(String.format("Found bonus %s at col=%d, row=%d", e.bonus, e.col, e.row));
		gameState.maze.setContent(e.col, e.row, Tile.EMPTY);
	}

	private void onNewLevel(StartLevelEvent e) {
		currentLevel = e.level;
		System.out.println("Starting level " + currentLevel);
		gameState.maze.reset();
		initEntities();
	}

	private void initEntities() {
		ghosts[RED_GHOST].setMazePosition(13, 11);
		ghosts[BLUE_GHOST].setMazePosition(11, 14);
		ghosts[PINK_GHOST].setMazePosition(13, 14);
		ghosts[ORANGE_GHOST].setMazePosition(15, 14);
		pacMan.setMazePosition(14, 23);
		pacMan.init();
		Stream.of(ghosts).forEach(Ghost::init);
	}
}
