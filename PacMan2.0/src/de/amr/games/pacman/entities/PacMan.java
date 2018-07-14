package de.amr.games.pacman.entities;

import static de.amr.games.pacman.board.Tile.BONUS_APPLE;
import static de.amr.games.pacman.board.Tile.BONUS_BELL;
import static de.amr.games.pacman.board.Tile.BONUS_CHERRIES;
import static de.amr.games.pacman.board.Tile.BONUS_GALAXIAN;
import static de.amr.games.pacman.board.Tile.BONUS_GRAPES;
import static de.amr.games.pacman.board.Tile.BONUS_KEY;
import static de.amr.games.pacman.board.Tile.BONUS_PEACH;
import static de.amr.games.pacman.board.Tile.BONUS_STRAWBERRY;
import static de.amr.games.pacman.board.Tile.ENERGIZER;
import static de.amr.games.pacman.board.Tile.PELLET;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.board.Board;
import de.amr.games.pacman.board.Spritesheet;
import de.amr.games.pacman.control.BonusFoundEvent;
import de.amr.games.pacman.control.FoodFoundEvent;
import de.amr.games.pacman.control.GameEvent;
import de.amr.games.pacman.control.GhostContactEvent;
import de.amr.games.pacman.control.PacManDiedEvent;

public class PacMan extends BoardMover<PacMan.State> {

	private static final int SIZE = 2 * Board.TS;
	private static boolean DEBUG = false;

	public enum State {
		ALIVE, DYING
	};

	public final Sprite[] spriteWalking = new Sprite[4];
	public final Sprite spriteStanding;
	public final Sprite spriteDying;
	public final List<Ghost> enemies = new ArrayList<>();

	public PacMan(Board board) {
		super(board);
		spriteStanding = new Sprite(Spritesheet.getPacManStanding()).scale(SIZE, SIZE);
		Stream.of(Top4.E, Top4.W, Top4.N, Top4.S).forEach(direction -> {
			spriteWalking[direction] = new Sprite(Spritesheet.getPacManWalking(direction)).scale(SIZE, SIZE);
			spriteWalking[direction].makeAnimated(AnimationMode.CYCLIC, 120);
		});
		spriteDying = new Sprite(Spritesheet.getPacManDying()).scale(SIZE, SIZE);
		spriteDying.makeAnimated(AnimationMode.LEFT_TO_RIGHT, 200);
	}

	@Override
	public void init() {
		setSpeed(Board.TS / 8f);
		setMoveDirection(Top4.E);
		setNextMoveDirection(Top4.E);
		setState(State.ALIVE);
	}

	@Override
	public void update() {
		if (state == State.ALIVE) {
			Optional<GameEvent> discovery = inspectTile();
			if (discovery.isPresent()) {
				fireGameEvent(discovery.get());
			} else {
				changeDirection();
				move();
			}
		} else if (state == State.DYING) {
			if (secondsInState() > 3) {
				fireGameEvent(new PacManDiedEvent());
			}
		}
	}

	@Override
	public String toString() {
		return "PacMan";
	}

	@Override
	public void draw(Graphics2D g) {
		if (DEBUG) {
			g.translate(tf.getX(), tf.getY());
			g.setColor(isExactlyOverTile() ? Color.GREEN : Color.YELLOW);
			g.fillRect(0, 0, Board.TS, Board.TS);
			g.translate(-tf.getX(), -tf.getY());
		} else {
			super.draw(g);
		}
	}

	@Override
	public Sprite currentSprite() {
		if (state == State.ALIVE) {
			return canMove(moveDirection) ? spriteWalking[moveDirection] : spriteStanding;
		}
		if (state == State.DYING) {
			return spriteDying;
		}
		throw new IllegalStateException("Illegal PacMan state: " + state);
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
		char content = board.getContent(col, row);
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
		char content = board.getContent(col, row);
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