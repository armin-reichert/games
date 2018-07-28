package de.amr.games.pacman.ui.actor;

import static de.amr.games.pacman.PacManApp.TS;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.model.Spritesheet.getPacManDying;
import static de.amr.games.pacman.model.Spritesheet.getPacManStanding;
import static de.amr.games.pacman.model.Spritesheet.getPacManWalking;
import static de.amr.games.pacman.model.Tile.isFood;

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
import de.amr.games.pacman.ui.Bonus;

public class PacMan extends MazeMover<PacMan.State> {

	public enum State {
		ALIVE, DYING
	};

	public final Set<GameEntity> lookFor = new HashSet<>();

	private Sprite s_walking[] = new Sprite[4];
	private Sprite s_standing;
	private Sprite s_dying;
	private List<Sprite> s_list = new ArrayList<>();

	public PacMan(Maze maze, Tile home) {
		super(maze, home, new EnumMap<>(State.class));
		setName("Pac-Man");
		int size = 2 * TS;
		s_standing = new Sprite(getPacManStanding()).scale(size);
		s_dying = new Sprite(getPacManDying()).scale(size).animation(AnimationMode.LINEAR, 100);
		TOPOLOGY.dirs().forEach(dir -> {
			s_walking[dir] = new Sprite(getPacManWalking(dir)).scale(size)
					.animation(AnimationMode.BACK_AND_FORTH, 80);
		});
		s_list.add(s_standing);
		s_list.add(s_dying);
		TOPOLOGY.dirs().forEach(dir -> s_list.add(s_walking[dir]));
	}

	@Override
	protected Stream<Sprite> getSprites() {
		return s_list.stream();
	}

	@Override
	public Sprite currentSprite() {
		if (getState() == State.ALIVE) {
			return s_walking[getDir()];
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
		currentSprite().enableAnimation(canMove(getDir()));
		Tile tile = getTile();
		char content = maze.getContent(tile);
		if (isFood(content)) {
			observers.fireGameEvent(new FoodFoundEvent(tile, content));
		} else {
			lookFor.stream().filter(this::collidesWith).findAny().ifPresent(finding -> {
				if (finding instanceof Ghost) {
					Ghost ghost = (Ghost) finding;
					if (ghost.getState() != Ghost.State.DEAD && ghost.getState() != Ghost.State.DYING) {
						observers.fireGameEvent(new GhostContactEvent(ghost));
					}
				} else if (finding instanceof Bonus) {
					Bonus bonus = (Bonus) finding;
					observers.fireGameEvent(new BonusFoundEvent(bonus.getSymbol(), bonus.getValue()));
				}
			});
		}
	}
}