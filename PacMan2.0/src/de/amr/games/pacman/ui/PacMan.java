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

	public final Set<GameEntity> interestingThings = new HashSet<>();

	private Sprite[] walking = new Sprite[4];
	private Sprite standing;
	private Sprite dying;
	private List<Sprite> animated = new ArrayList<>();

	public PacMan(Maze maze, Tile home) {
		super(maze, home, new EnumMap<>(State.class));
		setName("Pac-Man");

		standing = new Sprite(getPacManStanding()).scale(SPRITE_SIZE);
		dying = new Sprite(getPacManDying()).scale(SPRITE_SIZE).animation(AnimationMode.LINEAR, 100);
		TOPOLOGY.dirs().forEach(dir -> walking[dir] = new Sprite(getPacManWalking(dir))
				.scale(SPRITE_SIZE).animation(AnimationMode.BACK_AND_FORTH, 60));

		// TODO: remove
		animated.add(standing);
		animated.add(dying);
		TOPOLOGY.dirs().forEach(dir -> animated.add(walking[dir]));
	}

	@Override
	protected Stream<Sprite> getSprites() {
		return animated.stream();
	}

	@Override
	public Sprite currentSprite() {
		if (getState() == State.ALIVE) {
			int dir = getMoveDirection();
			return canMove(dir) ? walking[dir] : standing;
		} else {
			return dying;
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
		interestingThings.stream().filter(this::collidesWith).findAny().ifPresent(thing -> {
			if (thing instanceof Ghost) {
				Ghost ghost = (Ghost) thing;
				if (ghost.getState() != Ghost.State.DEAD && ghost.getState() != Ghost.State.DYING) {
					observers.fireGameEvent(new GhostContactEvent(ghost));
				}
			} else if (thing instanceof Bonus) {
				observers.fireGameEvent(new BonusFoundEvent(tile, (Bonus) thing));
			}
		});
	}
}