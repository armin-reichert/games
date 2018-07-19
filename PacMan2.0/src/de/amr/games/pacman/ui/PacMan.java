package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;
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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.behavior.MoveBehavior;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GhostContactEvent;
import de.amr.games.pacman.controller.event.PacManDiedEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class PacMan extends MazeMover<PacMan.State> {

	public enum State {
		ALIVE, DYING
	};

	private final Sprite[] spriteWalking = new Sprite[4];
	private final Sprite spriteStanding;
	private final Sprite spriteDying;
	private final Set<Ghost> enemies = new HashSet<>();
	private final EnumMap<State, MoveBehavior> moveBehavior = new EnumMap<>(State.class);

	public PacMan(Maze maze, String name) {
		super(maze);
		setName(name);
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
			Optional<GameEvent> discovery = inspectCurrentTile();
			if (discovery.isPresent()) {
				fireGameEvent(discovery.get());
				return;
			}
			walk();
		} else if (getState() == State.DYING) {
			if (stateDurationSeconds() > 3) {
				fireGameEvent(new PacManDiedEvent());
			}
		}
	}

	@Override
	public int getSpriteSize() {
		return TS * 2;
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		PacManApp.debug(() -> {
			g.setColor(isExactlyOverTile() ? Color.GREEN : Color.YELLOW);
			g.translate(tf.getX(), tf.getY());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.translate(-tf.getX(), -tf.getY());
		});
	}

	@Override
	public Sprite currentSprite() {
		if (getState() == State.ALIVE) {
			return canMove(getMoveDirection()) ? spriteWalking[getMoveDirection()] : spriteStanding;
		}
		if (getState() == State.DYING) {
			return spriteDying;
		}
		throw new IllegalStateException("Illegal PacMan state: " + getState());
	}

	public void setMoveBehavior(State state, MoveBehavior behavior) {
		moveBehavior.put(state, behavior);
	}

	@Override
	public MoveBehavior currentMoveBehavior() {
		return moveBehavior.get(getState());
	}

	// PacMan enemies

	public void addEnemy(Ghost ghost) {
		enemies.add(ghost);
	}

	public void removeEnemy(Ghost ghost) {
		enemies.remove(ghost);
	}

	public Stream<Ghost> enemies() {
		return enemies.stream();
	}

	// PacMan activity

	private Optional<GameEvent> inspectCurrentTile() {
		Tile currentTile = getTile();
		Optional<GameEvent> enemy = checkEnemy(currentTile);
		if (enemy.isPresent()) {
			return enemy;
		}
		Optional<GameEvent> food = checkFood(currentTile);
		if (food.isPresent()) {
			return food;
		}
		Optional<GameEvent> bonus = checkBonus(currentTile);
		if (bonus.isPresent()) {
			return bonus;
		}
		return Optional.empty();
	}

	private Optional<GameEvent> checkBonus(Tile tile) {
		char content = maze.getContent(tile);
		switch (content) {
		case BONUS_APPLE:
		case BONUS_BELL:
		case BONUS_CHERRIES:
		case BONUS_GALAXIAN:
		case BONUS_GRAPES:
		case BONUS_KEY:
		case BONUS_PEACH:
		case BONUS_STRAWBERRY:
			return Optional.of(new BonusFoundEvent(tile, content));
		default:
			return Optional.empty();
		}
	}

	private Optional<GameEvent> checkFood(Tile tile) {
		char content = maze.getContent(tile);
		switch (content) {
		case PELLET:
		case ENERGIZER:
			return Optional.of(new FoodFoundEvent(tile, content));
		default:
			return Optional.empty();
		}
	}

	private Optional<GameEvent> checkEnemy(Tile tile) {
		return enemies.stream().filter(enemy -> enemy.getState() != Ghost.State.DEAD).filter(this::collidesWith).findAny()
				.map(ghost -> new GhostContactEvent(ghost, tile));
	}
}