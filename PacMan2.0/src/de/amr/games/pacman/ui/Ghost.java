package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.getGhostDead;
import static de.amr.games.pacman.ui.Spritesheet.getGhostFrightened;
import static de.amr.games.pacman.ui.Spritesheet.getGhostNormal;
import static de.amr.games.pacman.ui.Spritesheet.getPinkNumber;
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
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class Ghost extends MazeMover<Ghost.State> {

	public enum State {
		ATTACKING, SCATTERING, FRIGHTENED, DYING, DEAD, RECOVERING
	}

	private final int color;
	private int dyingTime;

	private Sprite[] s_normal = new Sprite[4];
	private Sprite[] s_dying = new Sprite[4];
	private Sprite s_points;
	private Sprite[] s_dead = new Sprite[4];
	private Sprite s_frightened;
	private final List<Sprite> s_animated = new ArrayList<>();

	public Ghost(Maze maze, String name, int color, Tile home) {
		super(maze, home, new EnumMap<>(State.class));
		setName(name);
		this.color = color;
		TOPOLOGY.dirs().forEach(dir -> {
			s_normal[dir] = new Sprite(getGhostNormal(color, dir)).scale(SPRITE_SIZE)
					.animation(AnimationMode.BACK_AND_FORTH, 300);
			s_animated.add(s_normal[dir]);
			s_dead[dir] = new Sprite(getGhostDead(dir)).scale(SPRITE_SIZE);
		});
		for (int i = 0; i < 4; ++i) {
			s_dying[i] = new Sprite(getPinkNumber(i)).scale(SPRITE_SIZE);
		}
		s_points = s_dying[0];
		s_frightened = new Sprite(getGhostFrightened()).scale(SPRITE_SIZE)
				.animation(AnimationMode.CYCLIC, 200);
		s_animated.add(s_frightened);
	}

	@Override
	protected Stream<Sprite> getSprites() {
		return s_animated.stream();
	}

	@Override
	public Sprite currentSprite() {
		int dir = getMoveDirection();
		switch (getState()) {
		case ATTACKING:
		case RECOVERING:
		case SCATTERING:
			return s_normal[dir];
		case DYING:
			return s_points;
		case DEAD:
			return s_dead[dir];
		case FRIGHTENED:
			return s_frightened;
		default:
			throw new IllegalStateException("Illegal ghost state: " + getState());
		}
	}

	public int getColor() {
		return color;
	}

	public void killAndShowPoints(int points, int ticks) {
		this.dyingTime = ticks;
		s_points = s_dying[binarySearch(Game.GHOST_POINTS, points)];
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
}