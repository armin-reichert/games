package de.amr.games.pacman.core.entities;

import static de.amr.easy.game.Application.Log;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Door;
import static de.amr.games.pacman.core.board.TileContent.None;
import static de.amr.games.pacman.core.board.TileContent.Wall;
import static de.amr.games.pacman.core.entities.PacManState.Dying;
import static de.amr.games.pacman.core.entities.PacManState.Initialized;
import static de.amr.games.pacman.core.entities.PacManState.PowerWalking;
import static de.amr.games.pacman.core.entities.PacManState.Walking;
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
import java.util.Objects;
import java.util.function.Consumer;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.core.app.AbstractPacManApp;
import de.amr.games.pacman.core.board.Board;
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

	public Consumer<TileContent> onContentFound;
	public Consumer<Ghost> onEnemyContact;

	private final AbstractPacManApp app;
	private final StateMachine<PacManState> control;
	private List<Ghost> enemies;
	private int freezeTimer;

	public PacMan(AbstractPacManApp app, Board board) {
		super(board);
		this.app = Objects.requireNonNull(app);
		setName("Pac-Man");
		enemies = Collections.emptyList();

		onContentFound = content -> {
			switch (content) {
			case Bonus:
			case Energizer:
			case Pellet:
				Log.info(getName() + " ate " + content + " at " + currentTile());
				board.setContent(currentTile(), None);
				break;
			case GhostHouse:
			case Door:
			case Tunnel:
			case Wall:
			case Wormhole:
				Log.info("PacMan visited " + content + " at " + currentTile());
				break;
			default:
				break;
			}
		};

		onEnemyContact = enemy -> Log.info("PacMan met enemy " + enemy.getName() + " at " + currentTile());

		canEnterTile = tile -> board.isTileValid(tile) && !(board.contains(tile, Wall) || board.contains(tile, Door));

		// state machine

		control = new StateMachine<>("Pac-Man", new EnumMap<>(PacManState.class));

		control.state(Walking).entry = state -> {
			setAnimated(true);
		};

		control.state(Walking).update = state -> {
			checkKeyboard();
			walk();
		};

		control.state(PowerWalking).entry = state -> {
			app.assets.sound("sfx/waza.mp3").loop();
			enemies.forEach(ghost -> ghost.beginBeingFrightened(state.getDuration()));
		};

		control.state(PowerWalking).update = state -> {
			checkKeyboard();
			walk();
			if (state.isTerminated()) {
				control.changeTo(Walking);
			}
		};

		control.state(PowerWalking).exit = state -> {
			app.assets.sound("sfx/waza.mp3").stop();
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
		return String.format("Pacman at %s", currentTile());
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

	public PacManState state() {
		return control.stateID();
	}

	public State state(PacManState stateID) {
		return control.state(stateID);
	}

	public void killed() {
		control.changeTo(Dying);
	}

	public void beginPowerWalking(int seconds) {
		control.state(PowerWalking).setDuration(app.motor.toFrames(seconds));
		control.changeTo(PowerWalking);
	}

	public void beginWalking() {
		control.changeTo(Walking);
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

	public boolean isPowerWalkingEnding() {
		return control.inState(PowerWalking)
				&& control.state(PowerWalking).getRemaining() < control.state(PowerWalking).getDuration() / 4;
	}

	public void walk() {
		move();
		TileContent content = board.getContent(currentTile());
		if (content != None) {
			onContentFound.accept(content);
		}
		enemies.stream().filter(enemy -> enemy.getCol() == getCol() && enemy.getRow() == getRow()).forEach(onEnemyContact);
		turnTo(nextMoveDir);
	}

	private void checkKeyboard() {
		if (Keyboard.keyDown(VK_LEFT)) {
			nextMoveDir = W;
		}
		if (Keyboard.keyDown(VK_RIGHT)) {
			nextMoveDir = E;
		}
		if (Keyboard.keyDown(VK_UP)) {
			nextMoveDir = N;
		}
		if (Keyboard.keyDown(VK_DOWN)) {
			nextMoveDir = S;
		}
	}

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