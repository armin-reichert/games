package de.amr.games.pacman.ui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.event.GhostDeadIsOverEvent;
import de.amr.games.pacman.controller.event.GhostFrightenedEndsEvent;
import de.amr.games.pacman.controller.event.GhostRecoveringCompleteEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class Ghost extends MazeMover<Ghost.State> {

	public enum State {
		ATTACKING, SCATTERING, FRIGHTENED, DEAD, RECOVERING, STARRED
	}

	private final int color;

	private Sprite[] spriteNormal = new Sprite[4];
	private Sprite[] spriteDead = new Sprite[4];
	private Sprite spriteFrightened;
	private final List<Sprite> allSprites = new ArrayList<>();

	public Ghost(Maze maze, String name, int color, Tile home) {
		super(maze, home, new EnumMap<>(State.class));
		setName(name);
		this.color = color;
		loadSprites();
	}

	private void loadSprites() {
		Maze.TOPOLOGY.dirs().forEach(dir -> {
			spriteNormal[dir] = new Sprite(Spritesheet.getNormalGhostImages(color, dir)).scale(SPRITE_SIZE, SPRITE_SIZE);
			spriteNormal[dir].makeAnimated(AnimationMode.BACK_AND_FORTH, 300);
			spriteDead[dir] = new Sprite(Spritesheet.getDeadGhostImage(dir)).scale(SPRITE_SIZE, SPRITE_SIZE);
			allSprites.add(spriteNormal[dir]);
			allSprites.add(spriteDead[dir]);
		});
		spriteFrightened = new Sprite(Spritesheet.getFrightenedGhostImages()).scale(SPRITE_SIZE, SPRITE_SIZE);
		spriteFrightened.makeAnimated(AnimationMode.CYCLIC, 200);
		allSprites.add(spriteFrightened);
	}

	public int getColor() {
		return color;
	}

	@Override
	public void update() {
		switch (getState()) {
		case ATTACKING:
			move();
			break;
		case DEAD:
			move();
			if (getTile().equals(getHome())) {
				fireGameEvent(new GhostDeadIsOverEvent(this));
			}
			break;
		case FRIGHTENED:
			move();
			if (stateDurationSeconds() > 3) {
				fireGameEvent(new GhostFrightenedEndsEvent(this));
			}
			break;
		case RECOVERING:
			move();
			if (stateDurationSeconds() > 3) {
				fireGameEvent(new GhostRecoveringCompleteEvent(this));
			}
		case SCATTERING:
			move();
			break;
		case STARRED:
			break;
		default:
			throw new IllegalStateException("Illegal ghost state: " + getState());
		}
	}

	@Override
	protected Sprite[] getSprites() {
		return allSprites.toArray(new Sprite[allSprites.size()]);
	}

	@Override
	public Sprite currentSprite() {
		switch (getState()) {
		case ATTACKING:
			return spriteNormal[getMoveDirection()];
		case DEAD:
			return spriteDead[getMoveDirection()];
		case FRIGHTENED:
			return spriteFrightened;
		case RECOVERING:
			return spriteNormal[getMoveDirection()];
		case SCATTERING:
			return spriteNormal[getMoveDirection()];
		case STARRED:
			return spriteNormal[getMoveDirection()];
		default:
			throw new IllegalStateException("Illegal ghost state: " + getState());
		}
	}
}