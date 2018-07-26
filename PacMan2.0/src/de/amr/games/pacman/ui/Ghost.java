package de.amr.games.pacman.ui;

import static java.util.Arrays.binarySearch;

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
		ATTACKING, SCATTERING, FRIGHTENED, DYING, DEAD, RECOVERING
	}

	private static final int[] POINTS = new int[] { 200, 400, 800, 1600 };

	private final int color;
	private int points = 200;
	private int dyingTime;
	private Sprite[] spriteNormal = new Sprite[4];
	private Sprite[] spriteDying = new Sprite[4];
	private Sprite[] spriteDead = new Sprite[4];
	private Sprite spriteFrightened;
	private final List<Sprite> animatedSprites = new ArrayList<>();

	public Ghost(Maze maze, String name, int color, Tile home) {
		super(maze, home, new EnumMap<>(State.class));
		setName(name);
		this.color = color;
		loadSprites();
	}

	private void loadSprites() {
		Maze.TOPOLOGY.dirs().forEach(dir -> {
			spriteNormal[dir] = new Sprite(Spritesheet.getGhostNormal(color, dir)).scale(SPRITE_SIZE,
					SPRITE_SIZE);
			spriteNormal[dir].animation(AnimationMode.BACK_AND_FORTH, 300);
			animatedSprites.add(spriteNormal[dir]);
			spriteDead[dir] = new Sprite(Spritesheet.getGhostDead(dir)).scale(SPRITE_SIZE, SPRITE_SIZE);
			animatedSprites.add(spriteDead[dir]);
		});
		for (int i = 0; i < 4; ++i) {
			spriteDying[i] = new Sprite(Spritesheet.getPinkNumber(i)).scale(SPRITE_SIZE, SPRITE_SIZE);
		}
		spriteFrightened = new Sprite(Spritesheet.getGhostFrightened()).scale(SPRITE_SIZE, SPRITE_SIZE);
		spriteFrightened.animation(AnimationMode.CYCLIC, 200);
		animatedSprites.add(spriteFrightened);
	}

	public int getColor() {
		return color;
	}

	public void kill(int points, int ticks) {
		this.points = points;
		this.dyingTime = ticks;
		setState(State.DYING);
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
				observers.fireGameEvent(new GhostDeadIsOverEvent(this));
			}
			break;
		case DYING:
			if (dyingTime-- <= 0) {
				setState(State.DEAD);
			}
			break;
		case FRIGHTENED:
			move();
			// TODO does not belong here
			if (stateDurationSeconds() > 4) {
				observers.fireGameEvent(new GhostFrightenedEndsEvent(this));
			}
			break;
		case RECOVERING:
			move();
			// TODO does not belong here
			if (stateDurationSeconds() > 2) {
				observers.fireGameEvent(new GhostRecoveringCompleteEvent(this));
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
		return animatedSprites.stream(); // TODO improve this
	}

	@Override
	public Sprite currentSprite() {
		switch (getState()) {
		case ATTACKING:
			return spriteNormal[getMoveDirection()];
		case DYING:
			return spriteDying[binarySearch(POINTS, points)];
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