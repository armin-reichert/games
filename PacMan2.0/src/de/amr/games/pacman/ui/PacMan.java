package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.model.Tile.isFood;
import static de.amr.games.pacman.ui.Spritesheet.getPacManDying;
import static de.amr.games.pacman.ui.Spritesheet.getPacManStanding;
import static de.amr.games.pacman.ui.Spritesheet.getPacManWalking;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GhostContactEvent;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;

public class PacMan extends MazeMover<PacMan.State> {

	public enum State {
		ALIVE, DYING
	};

	public final Set<GameEntity> lookFor = new HashSet<>();

	private Sprite[] s_walking = new Sprite[4];
	private Sprite s_standing;
	private Sprite s_dying;
	private List<Sprite> s_animated = new ArrayList<>();

	public PacMan(Maze maze, Tile home) {
		super(maze, home, new EnumMap<>(State.class));
		setName("Pac-Man");
		s_standing = new Sprite(getPacManStanding()).scale(SPRITE_SIZE);
		s_dying = new Sprite(getPacManDying()).scale(SPRITE_SIZE).animation(AnimationMode.LINEAR, 100);
		TOPOLOGY.dirs().forEach(dir -> s_walking[dir] = new Sprite(getPacManWalking(dir))
				.scale(SPRITE_SIZE).animation(AnimationMode.BACK_AND_FORTH, 40));
		s_animated.add(s_standing);
		s_animated.add(s_dying);
		TOPOLOGY.dirs().forEach(dir -> s_animated.add(s_walking[dir]));
	}

	@Override
	protected Stream<Sprite> getSprites() {
		return s_animated.stream();
	}

	@Override
	public Sprite currentSprite() {
		if (getState() == State.ALIVE) {
			int dir = getMoveDirection();
			return canMove(dir) ? s_walking[dir] : s_standing;
		} else {
			return s_dying;
		}
	}

	@Override
	public void update() {
		if (getState() == State.DYING) {
			return;
		}
		move();
		Tile tile = getTile();
		char content = getMaze().getContent(tile);
		if (isFood(content)) {
			observers.fireGameEvent(new FoodFoundEvent(tile, content));
			return;
		}
		lookFor.stream().filter(this::collidesWith).findAny().ifPresent(finding -> {
			if (finding instanceof Ghost) {
				Ghost ghost = (Ghost) finding;
				if (ghost.getState() != Ghost.State.DEAD && ghost.getState() != Ghost.State.DYING) {
					observers.fireGameEvent(new GhostContactEvent(ghost));
				}
			} else if (finding instanceof Bonus) {
				observers.fireGameEvent(new BonusFoundEvent(tile, (Bonus) finding));
			}
		});
	}
}