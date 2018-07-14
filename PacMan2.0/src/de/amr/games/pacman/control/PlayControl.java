package de.amr.games.pacman.control;

import static de.amr.games.pacman.board.Spritesheet.BLUE_GHOST;
import static de.amr.games.pacman.board.Spritesheet.ORANGE_GHOST;
import static de.amr.games.pacman.board.Spritesheet.PINK_GHOST;
import static de.amr.games.pacman.board.Spritesheet.RED_GHOST;

import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PlayScene;
import de.amr.games.pacman.board.Tile;
import de.amr.games.pacman.entities.Ghost;
import de.amr.games.pacman.entities.PacMan;
import de.amr.games.pacman.entities.PacMan.State;

public class PlayControl implements GameEventListener {

	private final PlayScene scene;
	private int currentLevel;

	public PlayControl(PlayScene scene) {
		this.scene = scene;
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
			scene.pacMan.setNextMoveDirection(Top4.W);
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			scene.pacMan.setNextMoveDirection(Top4.E);
		} else if (Keyboard.keyDown(KeyEvent.VK_DOWN)) {
			scene.pacMan.setNextMoveDirection(Top4.S);
		} else if (Keyboard.keyDown(KeyEvent.VK_UP)) {
			scene.pacMan.setNextMoveDirection(Top4.N);
		}
		scene.app.entities.all().forEach(GameEntity::update);
	}

	private void onPacManDied(PacManDiedEvent e) {
		scene.pacMan.spriteDying.resetAnimation();
		initEntities();
	}

	private void onGhostContact(GhostContactEvent e) {
		System.out.println(String.format("Collision with %s at col=%d, row=%d", e.ghost, e.col, e.row));
		if (e.pacMan.getState() != PacMan.State.DYING) {
			if (e.ghost.getState() == Ghost.State.FRIGHTENED) {
				e.ghost.setState(Ghost.State.DEAD);
			} else if (e.ghost.getState() == Ghost.State.DEAD) {
				// do nothing
			} else {
				e.pacMan.setState(State.DYING);
				e.pacMan.setSpeed(0);
				e.pacMan.enemies.forEach(enemy -> enemy.setSpeed(0));
			}
		}
	}

	private void onFoodFound(FoodFoundEvent e) {
		scene.board.setContent(e.col, e.row, Tile.EMPTY);
		if (scene.board.isMazeEmpty()) {
			onNewLevel(new StartLevelEvent(currentLevel + 1));
			return;
		}
		if (e.food == Tile.ENERGIZER) {
			System.out.println(String.format("Eat energizer at col=%d, row=%d", e.col, e.row));
			e.pacMan.enemies.forEach(enemy -> enemy.setState(Ghost.State.FRIGHTENED));
		}
	}

	private void onBonusFound(BonusFoundEvent e) {
		System.out.println(String.format("Found bonus %s at col=%d, row=%d", e.bonus, e.col, e.row));
		scene.board.setContent(e.col, e.row, Tile.EMPTY);
	}

	private void onNewLevel(StartLevelEvent e) {
		currentLevel = e.level;
		System.out.println("Starting level " + currentLevel);
		scene.board.resetContent();
		initEntities();
	}

	private void initEntities() {
		scene.ghosts[RED_GHOST].setMazePosition(13, 11);
		scene.ghosts[BLUE_GHOST].setMazePosition(11, 14);
		scene.ghosts[PINK_GHOST].setMazePosition(13, 14);
		scene.ghosts[ORANGE_GHOST].setMazePosition(15, 14);
		scene.pacMan.setMazePosition(14, 23);
		scene.pacMan.init();
		Stream.of(scene.ghosts).forEach(Ghost::init);
	}
}
