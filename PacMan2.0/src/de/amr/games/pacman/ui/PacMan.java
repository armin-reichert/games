package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.Maze.TOPOLOGY;
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

	private Sprite[] spriteWalking = new Sprite[4];
	private Sprite spriteStanding;
	private Sprite spriteDying;
	private List<Sprite> allSprites = new ArrayList<>();

	public PacMan(Maze maze, Tile home) {
		super(maze, home, new EnumMap<>(State.class));
		setName("Pac-Man");
		createSprites();
	}

	private void createSprites() {
		spriteStanding = new Sprite(getPacManStanding()).scale(SPRITE_SIZE);
		spriteDying = new Sprite(getPacManDying()).scale(SPRITE_SIZE).animation(AnimationMode.LINEAR,
				100);
		TOPOLOGY.dirs().forEach(dir -> spriteWalking[dir] = new Sprite(getPacManWalking(dir))
				.scale(SPRITE_SIZE).animation(AnimationMode.BACK_AND_FORTH, 60));

		// TODO remove this:
		allSprites.add(spriteStanding);
		allSprites.add(spriteDying);
		TOPOLOGY.dirs().forEach(dir -> allSprites.add(spriteWalking[dir]));
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

	@Override
	protected Stream<Sprite> getSprites() {
		return allSprites.stream();
	}

	// PacMan activity

	@Override
	public void update() {
		switch (getState()) {
		case ALIVE:
			move();
			Tile tile = getTile();
			char content = getMaze().getContent(tile);
			if (Tile.isFood(content)) {
				observers.fireGameEvent(new FoodFoundEvent(tile, content));
			} else {
				interestingThings.stream().filter(this::collidesWith).findAny().ifPresent(thing -> {
					if (thing instanceof Ghost) {
						Ghost ghost = (Ghost) thing;
						// keine Leichenfledderei
						if (ghost.getState() != Ghost.State.DEAD && ghost.getState() != Ghost.State.DYING) {
							observers.fireGameEvent(new GhostContactEvent(ghost));
						}
					} else if (thing instanceof Bonus) {
						observers.fireGameEvent(new BonusFoundEvent(tile, (Bonus) thing));
					}
				});
			}
			break;
		case DYING:
			break;
		default:
			throw new IllegalStateException("Illegal PacMan state: " + getState());
		}
	}
}