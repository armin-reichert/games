package de.amr.games.pacman.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
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
	public final Set<Ghost> enemies = new HashSet<>();

	public PacMan(Maze maze, Tile home) {
		super(maze, home, new EnumMap<>(State.class));
		setName("Pac-Man");
		spriteStanding = new Sprite(Spritesheet.getPacManStanding()).scale(SPRITE_SIZE, SPRITE_SIZE);
		Maze.TOPOLOGY.dirs().forEach(dir -> {
			spriteWalking[dir] = new Sprite(Spritesheet.getPacManWalking(dir)).scale(SPRITE_SIZE, SPRITE_SIZE);
			spriteWalking[dir].makeAnimated(AnimationMode.CYCLIC, 100);
		});
		spriteDying = new Sprite(Spritesheet.getPacManDying()).scale(SPRITE_SIZE, SPRITE_SIZE);
		spriteDying.makeAnimated(AnimationMode.LEFT_TO_RIGHT, 100);
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		Debug.run(() -> {
			g.setColor(isExactlyOverTile() ? Color.GREEN : Color.YELLOW);
			g.translate(tf.getX(), tf.getY());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.translate(-tf.getX(), -tf.getY());
		});
	}

	@Override
	public Sprite currentSprite() {
		switch (getState()) {
		case ALIVE:
			return canMove(getMoveDirection()) ? spriteWalking[getMoveDirection()] : spriteStanding;
		case DYING:
			return spriteDying;
		}
		throw new IllegalStateException("Illegal PacMan state: " + getState());
	}

	// PacMan activity

	@Override
	public void update() {
		switch (getState()) {
		case ALIVE:
			Optional<GameEvent> discovery = inspectTile(getTile());
			if (discovery.isPresent()) {
				fireGameEvent(discovery.get());
			} else {
				move();
			}
			break;
		case DYING:
			if (stateDurationSeconds() > 3) {
				spriteDying.resetAnimation();
				fireGameEvent(new PacManDiedEvent());
			}
			break;
		default:
			throw new IllegalStateException("Illegal PacMan state: " + getState());
		}
	}

	private Optional<GameEvent> inspectTile(Tile tile) {
		char content = getMaze().getContent(tile);
		if (Tile.isBonus(content)) {
			return Optional.of(new BonusFoundEvent(tile, content));
		}
		if (Tile.isFood(content)) {
			return Optional.of(new FoodFoundEvent(tile, content));
		}
		return enemies.stream().filter(enemy -> enemy.getState() != Ghost.State.DEAD).filter(this::collidesWith).findAny()
				.map(ghost -> new GhostContactEvent(ghost));
	}
}