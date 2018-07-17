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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.BonusFoundEvent;
import de.amr.games.pacman.controller.FoodFoundEvent;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameEvent;
import de.amr.games.pacman.controller.GhostContactEvent;
import de.amr.games.pacman.controller.PacManDiedEvent;
import de.amr.games.pacman.model.Maze;

public class PacMan extends MazeMover<PacMan.State> {

	public enum State {
		ALIVE, DYING
	};

	public final Sprite[] spriteWalking = new Sprite[4];
	public final Sprite spriteStanding;
	public final Sprite spriteDying;
	public final List<Ghost> enemies = new ArrayList<>();

	public PacMan(Maze maze) {
		super(maze);
		spriteStanding = new Sprite(Spritesheet.getPacManStanding()).scale(getSpriteSize(), getSpriteSize());
		Maze.TOPOLOGY.dirs().forEach(dir -> {
			spriteWalking[dir] = new Sprite(Spritesheet.getPacManWalking(dir)).scale(getSpriteSize(), getSpriteSize());
			spriteWalking[dir].makeAnimated(AnimationMode.CYCLIC, 150);
		});
		spriteDying = new Sprite(Spritesheet.getPacManDying()).scale(getSpriteSize(), getSpriteSize());
		spriteDying.makeAnimated(AnimationMode.LEFT_TO_RIGHT, 200);
	}

	@Override
	public void update() {
		if (getState() == State.ALIVE) {
			Optional<GameEvent> discovery = checkCurrentTile();
			if (discovery.isPresent()) {
				fireGameEvent(discovery.get());
				return;
			}
			nextMoveDirection = brain.recommendNextMoveDirection();
			if (canMove(nextMoveDirection)) {
				moveDirection = nextMoveDirection;
			}
			move();
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
		super.draw(g);
		GameController.debug(() -> {
			g.setColor(isExactlyOverTile() ? Color.GREEN : Color.YELLOW);
			g.translate(tf.getX(), tf.getY());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.translate(-tf.getX(), -tf.getY());
		});
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
		char content = maze.getContent(col, row);
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
		char content = maze.getContent(col, row);
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