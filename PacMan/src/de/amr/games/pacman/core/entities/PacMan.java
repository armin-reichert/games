package de.amr.games.pacman.core.entities;

import static de.amr.easy.game.Application.Log;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Door;
import static de.amr.games.pacman.core.board.TileContent.Wall;
import static de.amr.games.pacman.core.entities.PacManState.Dying;
import static de.amr.games.pacman.core.entities.PacManState.Walking;
import static de.amr.games.pacman.core.entities.PacManState.PowerWalking;
import static de.amr.games.pacman.core.entities.PacManState.Initialized;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.core.app.AbstractPacManApp;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.statemachine.State;
import de.amr.games.pacman.core.statemachine.StateMachine;

/**
 * The Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan extends BoardMover {

	public final StateMachine<PacManState> control;

	public Consumer<TileContent> onContentFound;
	public Consumer<Ghost> onGhostMet;

	private final AbstractPacManApp app;
	private List<Ghost> enemies;
	private Supplier<Float> speedBeforeFrightening;
	private int freezeTimer;

	public PacMan(AbstractPacManApp app, Board board) {
		super(board);
		setName("Pac-Man");

		this.app = app;
		enemies = Collections.emptyList();

		// default tile access
		canEnterTile = tile -> board.isTileValid(tile) && !board.contains(tile, Wall) && !board.contains(tile, Door);

		// default event handlers

		onContentFound = content -> {
			Tile tile = currentTile();
			Log.info("PacMan found " + content + " at tile " + tile);
			board.setContent(tile, TileContent.None);
		};

		onGhostMet = ghost -> {
			Log.info("PacMan meets ghost " + ghost);
		};

		// state machine

		control = new StateMachine<>("Pac-Man", new EnumMap<>(PacManState.class));

		control.state(Walking).entry = state -> {
			setAnimated(true);
		};

		control.state(Walking).update = state -> {
			walk();
		};

		control.state(PowerWalking).entry = state -> {
			app.assets.sound("sfx/waza.mp3").loop();
			speedBeforeFrightening = speed;
			enemies.forEach(ghost -> ghost.beginBeingFrightened(state.getDuration()));
		};

		control.state(PowerWalking).update = state -> {
			walk();
			if (state.isTerminated()) {
				control.changeTo(Walking);
			}
		};

		control.state(PowerWalking).exit = state -> {
			app.assets.sound("sfx/waza.mp3").stop();
			speed = speedBeforeFrightening;
			enemies.forEach(Ghost::endBeingFrightened);
		};

		control.state(Dying).entry = state -> {
			if (app.getTheme().getPacManDyingSprite() != null) {
				app.getTheme().getPacManDyingSprite().resetAnimation();
				app.getTheme().getPacManDyingSprite().setAnimated(true);
			}
		};
	}

	@Override
	public String toString() {
		return String.format("Pacman[row=%d,col=%d]", getRow(), getCol());
	}

	@Override
	public void init() {
		super.init();
		freezeTimer = 0;
		moveDir = W;
		nextMoveDir = W;
		control.changeTo(Initialized);
	}

	@Override
	public void update() {
		if (freezeTimer > 0) {
			--freezeTimer;
			return;
		}
		control.update();
	}

	@Override
	public Sprite currentSprite() {
		if (control.inState(Dying) && app.getTheme().getPacManDyingSprite() != null) {
			return app.getTheme().getPacManDyingSprite();
		}
		if (control.inState(Initialized)) {
			return app.getTheme().getPacManStandingSprite(moveDir);
		}
		Sprite runningSprite = app.getTheme().getPacManRunningSprite(moveDir);
		runningSprite.setAnimated(couldMove);
		return runningSprite;
	}

	@Override
	public void setAnimated(boolean animated) {
		Top4.INSTANCE.dirs().forEach(dir -> {
			app.getTheme().getPacManRunningSprite(dir).setAnimated(animated);
		});
	}

	public void setEnemies(Ghost... enemies) {
		this.enemies = Arrays.asList(enemies);
	}

	public void freeze(int frames) {
		this.freezeTimer = frames;
	}

	public boolean isFrighteningEnding() {
		return control.inState(PowerWalking)
				&& control.state(PowerWalking).getRemaining() < control.state(PowerWalking).getDuration() / 4;
	}

	public void walk() {
		move();
		TileContent content = board.getContent(currentTile());
		if (content != TileContent.None) {
			onContentFound.accept(content);
		}
		enemies.stream().filter(ghost -> ghost.getCol() == getCol() && ghost.getRow() == getRow()).forEach(onGhostMet);
		turnTo(computeNextMoveDir());
	}

	private int computeNextMoveDir() {
		if (Keyboard.keyDown(VK_LEFT)) {
			return W;
		}
		if (Keyboard.keyDown(VK_RIGHT)) {
			return E;
		}
		if (Keyboard.keyDown(VK_UP)) {
			return N;
		}
		if (Keyboard.keyDown(VK_DOWN)) {
			return S;
		}
		return nextMoveDir;
	}

	// -- drawing

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		if (app.settings.getBool("drawInternals")) {
			g.setColor(Color.WHITE);
			g.setFont(new Font(Font.DIALOG, Font.PLAIN, TILE_SIZE * 9 / 10));
			State state = control.state();
			StringBuilder text = new StringBuilder();
			text.append(getName()).append(" (").append(control.stateID());
			if (state.getDuration() != State.FOREVER) {
				text.append(":").append(state.getRemaining()).append("|").append(state.getDuration());
			}
			text.append(")");
			g.drawString(text.toString(), tr.getX(), tr.getY() - 10);
		}
		if (app.settings.getBool("drawGrid")) {
			drawCollisionBox(g, isExactlyOverTile() ? Color.GREEN : Color.LIGHT_GRAY);
		}
	}
}