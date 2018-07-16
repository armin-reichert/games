package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.Tile.BONUS_APPLE;
import static de.amr.games.pacman.model.Tile.BONUS_BELL;
import static de.amr.games.pacman.model.Tile.BONUS_CHERRIES;
import static de.amr.games.pacman.model.Tile.BONUS_GALAXIAN;
import static de.amr.games.pacman.model.Tile.BONUS_GRAPES;
import static de.amr.games.pacman.model.Tile.BONUS_KEY;
import static de.amr.games.pacman.model.Tile.BONUS_PEACH;
import static de.amr.games.pacman.model.Tile.BONUS_STRAWBERRY;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.BonusFoundEvent;
import de.amr.games.pacman.controller.FoodFoundEvent;
import de.amr.games.pacman.controller.GameEvent;
import de.amr.games.pacman.controller.GhostContactEvent;
import de.amr.games.pacman.controller.PacManDiedEvent;
import de.amr.games.pacman.model.Game;

public class PacMan extends MazeMover<PacMan.State> {

	private static boolean DEBUG = false;

	public enum State {
		ALIVE, DYING
	};

	public final Sprite[] spriteWalking = new Sprite[4];
	public final Sprite spriteStanding;
	public final Sprite spriteDying;
	public final List<Ghost> enemies = new ArrayList<>();

	public PacMan(Game game) {
		super(game);
		spriteStanding = new Sprite(Spritesheet.getPacManStanding()).scale(getSpriteSize(), getSpriteSize());
		Stream.of(Top4.E, Top4.W, Top4.N, Top4.S).forEach(dir -> {
			spriteWalking[dir] = new Sprite(Spritesheet.getPacManWalking(dir)).scale(getSpriteSize(), getSpriteSize());
			spriteWalking[dir].makeAnimated(AnimationMode.CYCLIC, 120);
		});
		spriteDying = new Sprite(Spritesheet.getPacManDying()).scale(getSpriteSize(), getSpriteSize());
		spriteDying.makeAnimated(AnimationMode.LEFT_TO_RIGHT, 200);
	}

	private void readSteering() {
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			setNextMoveDirection(Top4.W);
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			setNextMoveDirection(Top4.E);
		} else if (Keyboard.keyDown(KeyEvent.VK_DOWN)) {
			setNextMoveDirection(Top4.S);
		} else if (Keyboard.keyDown(KeyEvent.VK_UP)) {
			setNextMoveDirection(Top4.N);
		}
	}

	@Override
	public void update() {
		if (getState() == State.ALIVE) {
			Optional<GameEvent> discovery = checkCurrentTile();
			if (discovery.isPresent()) {
				fireGameEvent(discovery.get());
			} else {
				readSteering();
				if (nextMoveDirection != moveDirection && isExactlyOverTile() && canMove(nextMoveDirection)) {
					moveDirection = nextMoveDirection;
				}
				move();
			}
		} else if (getState() == State.DYING) {
			if (stateDurationSeconds() > 3) {
				fireGameEvent(new PacManDiedEvent());
			}
		}
	}

	@Override
	public String toString() {
		return "PacMan";
	}

	@Override
	protected int getSpriteSize() {
		return PacManApp.TS * 2;
	}

	@Override
	public void draw(Graphics2D g) {
		if (DEBUG) {
			g.setColor(isExactlyOverTile() ? Color.GREEN : Color.YELLOW);
			g.translate(tf.getX(), tf.getY());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.translate(-tf.getX(), -tf.getY());
		} else {
			super.draw(g);
		}
	}

	@Override
	public Sprite currentSprite() {
		if (getState() == State.ALIVE) {
			return canMove(moveDirection) ? spriteWalking[moveDirection] : spriteStanding;
		}
		if (getState() == State.DYING) {
			return spriteDying;
		}
		throw new IllegalStateException("Illegal PacMan state: " + getState());
	}

	private Optional<GameEvent> checkCurrentTile() {
		Optional<GameEvent> enemy = checkEnemy();
		if (enemy.isPresent()) {
			return enemy;
		}
		Optional<GameEvent> food = checkFood();
		if (food.isPresent()) {
			return food;
		}
		Optional<GameEvent> bonus = checkBonus();
		if (bonus.isPresent()) {
			return bonus;
		}
		return Optional.empty();
	}

	private Optional<GameEvent> checkBonus() {
		int col = col(), row = row();
		char content = game.maze.getContent(col, row);
		switch (content) {
		case BONUS_APPLE:
		case BONUS_BELL:
		case BONUS_CHERRIES:
		case BONUS_GALAXIAN:
		case BONUS_GRAPES:
		case BONUS_KEY:
		case BONUS_PEACH:
		case BONUS_STRAWBERRY:
			return Optional.of(new BonusFoundEvent(this, col, row, content));
		default:
			return Optional.empty();
		}
	}

	private Optional<GameEvent> checkFood() {
		int col = col(), row = row();
		char content = game.maze.getContent(col, row);
		switch (content) {
		case PELLET:
		case ENERGIZER:
			return Optional.of(new FoodFoundEvent(this, col, row, content));
		default:
			return Optional.empty();
		}
	}

	private Optional<GameEvent> checkEnemy() {
		return enemies.stream().filter(enemy -> enemy.getState() != Ghost.State.DEAD).filter(this::collidesWith).findAny()
				.map(ghost -> new GhostContactEvent(this, ghost, col(), row()));
	}
}