package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.Tile.BONUS_APPLE;
import static de.amr.games.pacman.model.Tile.BONUS_BELL;
import static de.amr.games.pacman.model.Tile.BONUS_CHERRIES;
import static de.amr.games.pacman.model.Tile.BONUS_GALAXIAN;
import static de.amr.games.pacman.model.Tile.BONUS_GRAPES;
import static de.amr.games.pacman.model.Tile.BONUS_KEY;
import static de.amr.games.pacman.model.Tile.BONUS_PEACH;
import static de.amr.games.pacman.model.Tile.BONUS_STRAWBERRY;
import static de.amr.games.pacman.model.Tile.ENERGIZER;
import static de.amr.games.pacman.model.Tile.PELLET;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.PacManApp;
import de.amr.games.pacman.controller.BonusFoundEvent;
import de.amr.games.pacman.controller.FoodFoundEvent;
import de.amr.games.pacman.controller.GameEvent;
import de.amr.games.pacman.controller.GhostContactEvent;
import de.amr.games.pacman.controller.PacManDiedEvent;
import de.amr.games.pacman.model.Game;

public class PacMan extends MazeMover<PacMan.State> {

	private static boolean DEBUG = false;

	public enum State {
		ALIVE, DYING
	};

	public final Sprite[] spriteWalking = new Sprite[4];
	public final Sprite spriteStanding;
	public final Sprite spriteDying;
	public final List<Ghost> enemies = new ArrayList<>();

	public PacMan(Game game) {
		super(game);
		spriteStanding = new Sprite(Spritesheet.getPacManStanding()).scale(getSpriteSize(), getSpriteSize());
		Stream.of(Top4.E, Top4.W, Top4.N, Top4.S).forEach(dir -> {
			spriteWalking[dir] = new Sprite(Spritesheet.getPacManWalking(dir)).scale(getSpriteSize(), getSpriteSize());
			spriteWalking[dir].makeAnimated(AnimationMode.CYCLIC, 120);
		});
		spriteDying = new Sprite(Spritesheet.getPacManDying()).scale(getSpriteSize(), getSpriteSize());
		spriteDying.makeAnimated(AnimationMode.LEFT_TO_RIGHT, 200);
	}

	@Override
	public void init() {
		setSpeed(PacManApp.TS / 8f);
		setMoveDirection(Top4.E);
		setNextMoveDirection(Top4.E);
		setState(State.ALIVE);
	}

	@Override
	public void update() {
		if (getState() == State.ALIVE) {
			Optional<GameEvent> discovery = inspectTile();
			if (discovery.isPresent()) {
				fireGameEvent(discovery.get());
			} else {
				if (nextMoveDirection != moveDirection && isExactlyOverTile() && canMove(nextMoveDirection)) {
					moveDirection = nextMoveDirection;
				}
				move();
			}
		} else if (getState() == State.DYING) {
			if (stateDurationSeconds() > 3) {
				fireGameEvent(new PacManDiedEvent());
			}
		}
	}

	@Override
	public String toString() {
		return "PacMan";
	}

	@Override
	protected int getSpriteSize() {
		return PacManApp.TS * 2;
	}

	@Override
	public void draw(Graphics2D g) {
		if (DEBUG) {
			g.setColor(isExactlyOverTile() ? Color.GREEN : Color.YELLOW);
			g.translate(tf.getX(), tf.getY());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.translate(-tf.getX(), -tf.getY());
		} else {
			super.draw(g);
		}
	}

	@Override
	public Sprite currentSprite() {
		if (getState() == State.ALIVE) {
			return canMove(moveDirection) ? spriteWalking[moveDirection] : spriteStanding;
		}
		if (getState() == State.DYING) {
			return spriteDying;
		}
		throw new IllegalStateException("Illegal PacMan state: " + getState());
	}

	private Optional<GameEvent> inspectTile() {
		Optional<GameEvent> ghostDiscovery = discoverGhost();
		if (ghostDiscovery.isPresent()) {
			return ghostDiscovery;
		}
		Optional<GameEvent> foodDiscovery = discoverFood();
		if (discoverFood().isPresent()) {
			return foodDiscovery;
		}
		Optional<GameEvent> bonusDiscovery = discoverBonus();
		if (bonusDiscovery.isPresent()) {
			return bonusDiscovery;
		}
		return Optional.empty();
	}

	private Optional<GameEvent> discoverBonus() {
		int col = col(), row = row();
		char content = game.maze.getContent(col, row);
		switch (content) {
		case BONUS_APPLE:
		case BONUS_BELL:
		case BONUS_CHERRIES:
		case BONUS_GALAXIAN:
		case BONUS_GRAPES:
		case BONUS_KEY:
		case BONUS_PEACH:
		case BONUS_STRAWBERRY:
			return Optional.of(new BonusFoundEvent(this, col, row, content));
		default:
			return Optional.empty();
		}
	}

	private Optional<GameEvent> discoverFood() {
		int col = col(), row = row();
		char content = game.maze.getContent(col, row);
		switch (content) {
		case PELLET:
		case ENERGIZER:
			return Optional.of(new FoodFoundEvent(this, col, row, content));
		default:
			return Optional.empty();
		}
	}

	private Optional<GameEvent> discoverGhost() {
		return enemies.stream().filter(this::collidesWith).findAny()
				.map(ghost -> new GhostContactEvent(this, ghost, col(), row()));
	}
}