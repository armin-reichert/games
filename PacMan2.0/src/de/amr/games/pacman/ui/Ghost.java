package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.event.GhostDeadEndsEvent;
import de.amr.games.pacman.controller.event.GhostFrightenedEndsEvent;
import de.amr.games.pacman.model.Maze;

public class Ghost extends MazeMover<Ghost.State> {

	public enum State {
		ATTACKING, SCATTERING, FRIGHTENED, DEAD, STARRED
	}

	private final Sprite[] spriteNormal = new Sprite[4];
	private final Sprite[] spriteDead = new Sprite[4];
	private final Sprite spriteFrightened;
	// TODO remove this:
	private final List<Sprite> allSprites = new ArrayList<>();

	public Ghost(Maze maze, String name, int color) {
		super(maze, new EnumMap<>(State.class));
		setName(name);
		Maze.TOPOLOGY.dirs().forEach(dir -> {
			spriteNormal[dir] = new Sprite(Spritesheet.getNormalGhostImages(color, dir)).scale(getSpriteSize(),
					getSpriteSize());
			spriteNormal[dir].makeAnimated(AnimationMode.BACK_AND_FORTH, 300);
			spriteDead[dir] = new Sprite(Spritesheet.getDeadGhostImage(dir)).scale(getSpriteSize(), getSpriteSize());
			allSprites.add(spriteNormal[dir]);
			allSprites.add(spriteDead[dir]);
		});
		spriteFrightened = new Sprite(Spritesheet.getFrightenedGhostImages()).scale(getSpriteSize(), getSpriteSize());
		spriteFrightened.makeAnimated(AnimationMode.CYCLIC, 200);
		allSprites.add(spriteFrightened);
	}

	@Override
	public void update() {
		switch (getState()) {
		case ATTACKING:
			move();
			break;
		case DEAD:
			move();
			if (stateDurationSeconds() > 6) {
				fireGameEvent(new GhostDeadEndsEvent(this));
			}
			break;
		case FRIGHTENED:
			move();
			if (stateDurationSeconds() > 3) {
				fireGameEvent(new GhostFrightenedEndsEvent(this));
			}
			break;
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
	public int getSpriteSize() {
		return TS * 2;
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
		case SCATTERING:
			return spriteNormal[getMoveDirection()];
		case STARRED:
			return spriteNormal[getMoveDirection()];
		default:
			throw new IllegalStateException("Illegal ghost state: " + getState());
		}
	}

	@Override
	protected Sprite[] getSprites() {
		return allSprites.toArray(new Sprite[allSprites.size()]);
	}
}