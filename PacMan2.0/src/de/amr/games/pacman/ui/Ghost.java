package de.amr.games.pacman.ui;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.getGhostDead;
import static de.amr.games.pacman.ui.Spritesheet.getGhostFrightened;
import static de.amr.games.pacman.ui.Spritesheet.getGhostFrightenedEnding;
import static de.amr.games.pacman.ui.Spritesheet.getGhostNormal;
import static de.amr.games.pacman.ui.Spritesheet.getGreenNumber;

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
		ATTACKING, SCATTERING, FRIGHTENED, FRIGHTENED_ENDING, DYING, DEAD, RECOVERING
	}

	private final int color;

	private Sprite[] s_normal = new Sprite[4];
	private Sprite[] s_dying = new Sprite[4];
	private Sprite s_points;
	private Sprite[] s_dead = new Sprite[4];
	private Sprite s_frightened;
	private Sprite s_frightened_ending;
	private final List<Sprite> s_animated = new ArrayList<>();

	public Ghost(Maze maze, String name, int color, Tile home) {
		super(maze, home, new EnumMap<>(State.class));
		setName(name);
		this.color = color;
		int size = 2 * TS;
		TOPOLOGY.dirs().forEach(dir -> {
			s_normal[dir] = new Sprite(getGhostNormal(color, dir)).scale(size)
					.animation(AnimationMode.BACK_AND_FORTH, 300);
			s_animated.add(s_normal[dir]);
			s_dead[dir] = new Sprite(getGhostDead(dir)).scale(size);
		});
		for (int i = 0; i < 4; ++i) {
			s_dying[i] = new Sprite(getGreenNumber(i)).scale(size);
		}
		s_points = s_dying[0];
		s_frightened = new Sprite(getGhostFrightened()).scale(size).animation(AnimationMode.CYCLIC,
				200);
		s_frightened_ending = new Sprite(getGhostFrightenedEnding()).scale(size)
				.animation(AnimationMode.CYCLIC, 100);
		s_animated.add(s_frightened);
	}

	@Override
	protected Stream<Sprite> getSprites() {
		return s_animated.stream();
	}

	@Override
	public Sprite currentSprite() {
		switch (getState()) {
		case ATTACKING:
		case RECOVERING:
		case SCATTERING:
			return s_normal[getDir()];
		case DYING:
			return s_points;
		case DEAD:
			return s_dead[getDir()];
		case FRIGHTENED:
			return s_frightened;
		case FRIGHTENED_ENDING:
			return s_frightened_ending;
		default:
			throw new IllegalStateException("Illegal ghost state: " + getState());
		}
	}

	public int getColor() {
		return color;
	}

	public void onWounded(int ghostIndex) {
		s_points = s_dying[ghostIndex];
		setState(State.DYING);
	}
	
	public void onExitus() {
		setState(State.DEAD);
	}

	@Override
	public void update() {
		switch (getState()) {
		case ATTACKING:
			move();
			break;
		case DEAD:
			move();
			if (getTile().equals(homeTile)) {
				observers.fireGameEvent(new GhostDeadIsOverEvent(this));
			}
			break;
		case DYING:
			break;
		case FRIGHTENED:
			move();
			if (stateDurationSeconds() > 3) {
				setState(State.FRIGHTENED_ENDING);
			}
			break;
		case FRIGHTENED_ENDING:
			move();
			if (stateDurationSeconds() > 1) {
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