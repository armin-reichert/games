package de.amr.games.pacman.ui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.event.GhostDeadIsOverEvent;
import de.amr.games.pacman.controller.event.GhostFrightenedEndsEvent;
import de.amr.games.pacman.controller.event.GhostRecoveringCompleteEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class Ghost extends MazeMover<Ghost.State> {

	public enum State {
		ATTACKING, SCATTERING, FRIGHTENED, DEAD, RECOVERING
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
			spriteNormal[dir].createAnimation(AnimationMode.BACK_AND_FORTH, 300);
			spriteDead[dir] = new Sprite(Spritesheet.getDeadGhostImage(dir)).scale(SPRITE_SIZE, SPRITE_SIZE);
			allSprites.add(spriteNormal[dir]);
			allSprites.add(spriteDead[dir]);
		});
		spriteFrightened = new Sprite(Spritesheet.getFrightenedGhostImages()).scale(SPRITE_SIZE, SPRITE_SIZE);
		spriteFrightened.createAnimation(AnimationMode.CYCLIC, 200);
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
			// TODO does not belong here
			if (stateDurationSeconds() > 4) {
				fireGameEvent(new GhostFrightenedEndsEvent(this));
			}
			break;
		case RECOVERING:
			move();
			// TODO does not belong here
			if (stateDurationSeconds() > 2) {
				fireGameEvent(new GhostRecoveringCompleteEvent(this));
			}
		case SCATTERING:
			move();
			break;
		default:
			throw new IllegalStateException("Illegal ghost state: " + getState());
		}
	}

	@Override
	protected Stream<Sprite> getSprites() {
		return allSprites.stream();
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
		default:
			throw new IllegalStateException("Illegal ghost state: " + getState());
		}
	}
}