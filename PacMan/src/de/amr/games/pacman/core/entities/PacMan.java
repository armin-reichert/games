package de.amr.games.pacman.core.entities;

import static de.amr.easy.game.Application.Log;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Bonus;
import static de.amr.games.pacman.core.board.TileContent.Door;
import static de.amr.games.pacman.core.board.TileContent.Energizer;
import static de.amr.games.pacman.core.board.TileContent.Pellet;
import static de.amr.games.pacman.core.board.TileContent.Wall;
import static de.amr.games.pacman.core.entities.PacManState.Dying;
import static de.amr.games.pacman.core.entities.PacManState.Eating;
import static de.amr.games.pacman.core.entities.PacManState.Frightening;
import static de.amr.games.pacman.core.entities.PacManState.Waiting;
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
import java.util.function.BiConsumer;
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

	public BiConsumer<Tile, TileContent> onContentFound;
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

		onContentFound = (tile, content) -> {
			Log.info("PacMan found " + content + " at tile " + tile);
			board.setContent(tile, TileContent.None);
		};

		onGhostMet = ghost -> {
			Log.info("PacMan meets ghost " + ghost);
		};

		// state machine

		control = new StateMachine<>("Pac-Man", new EnumMap<>(PacManState.class));

		control.state(Waiting).entry = state -> {
			couldMove = false;
			freezeTimer = 0;
			moveDir = W;
			nextMoveDir = W;
		};

		control.state(Eating).entry = state -> {
			setAnimated(true);
		};

		control.state(Eating).update = state -> {
			moveAndEat();
		};

		control.state(Frightening).entry = state -> {
			app.assets.sound("sfx/waza.mp3").loop();
			speedBeforeFrightening = speed;
			enemies.forEach(ghost -> ghost.beginBeingFrightened(state.getDuration()));
		};

		control.state(Frightening).update = state -> {
			moveAndEat();
			if (state.isTerminated()) {
				control.changeTo(Eating);
			}
		};

		control.state(Frightening).exit = state -> {
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
		control.changeTo(Waiting);
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
		if (control.inState(Waiting)) {
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
		return control.inState(Frightening)
				&& control.state(Frightening).getRemaining() < control.state(Frightening).getDuration() / 4;
	}

	public void moveAndEat() {
		turnTo(computeNextMoveDir());
		move();
		Tile tile = currentTile();
		TileContent content = board.getContent(tile);
		onContentFound.accept(tile, content);
		/*@formatter:off*/
		enemies.stream()
			.filter(ghost -> ghost.getCol() == getCol() && ghost.getRow() == getRow())
			.forEach(onGhostMet);
		/*@formatter:on*/
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