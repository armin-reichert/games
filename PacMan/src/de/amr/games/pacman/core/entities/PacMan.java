package de.amr.games.pacman.core.entities;

import static de.amr.easy.game.Application.Log;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.Top4;
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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent;
import de.amr.games.pacman.core.statemachine.State;
import de.amr.games.pacman.core.statemachine.StateMachine;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * The Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan extends BoardMover {

	public Consumer<TileContent> onContentFound;
	public Consumer<Ghost> onEnemyContact;

	private final Application app;
	private final Supplier<PacManTheme> theme;
	public final StateMachine<PacManState, PacManEvent> control;
	private final Set<Ghost> enemies;
	private int freezeTimer;

	public PacMan(Application app, Board board, Supplier<PacManTheme> theme) {
		super(board);
		this.app = Objects.requireNonNull(app);
		this.theme = theme;
		setName("Pac-Man");
		enemies = new HashSet<>();

		onContentFound = content -> {
			switch (content) {
			case Bonus:
			case Energizer:
			case Pellet:
				Log.info(getName() + " eats " + content + " at " + currentTile());
				board.setContent(currentTile(), None);
				break;
			case GhostHouse:
			case Door:
			case Tunnel:
			case Wall:
			case Wormhole:
				Log.info("PacMan visits " + content + " at " + currentTile());
				break;
			default:
				break;
			}
		};

		onEnemyContact = enemy -> Log.info("PacMan meets enemy '" + enemy.getName() + "' at " + currentTile());

		canEnterTile = tile -> board.isTileValid(tile) && !(board.contains(tile, Wall) || board.contains(tile, Door));

		// state machine

		control = new StateMachine<>(getName(), new EnumMap<>(PacManState.class), Initialized);
		
		control.changeOnInput(PacManEvent.WalkingStarts, Initialized, Walking);

		control.state(Walking).entry = state -> {
			setAnimated(true);
		};

		control.state(Walking).update = state -> {
			requestMoveDirection();
			walk();
		};
		
		control.changeOnInput(PacManEvent.Killed, Walking, Dying);

		control.state(PowerWalking).entry = state -> {
			app.assets.sound("sfx/waza.mp3").loop();
			// enemies.forEach(ghost -> ghost.beginBeingFrightened(state.getDuration()));
			enemies.forEach(enemy -> enemy.control.feed(GhostEvent.PacManAttackStarts));
		};

		control.state(PowerWalking).update = state -> {
			requestMoveDirection();
			walk();
		};

		control.state(PowerWalking).exit = state -> {
			app.assets.sound("sfx/waza.mp3").stop();
			enemies.forEach(Ghost::endBeingFrightened);
		};

		control.changeOnTimeout(PowerWalking, Walking);

		control.state(Dying).entry = state -> {
			if (theme.get().getPacManDyingSprite() != null) {
				theme.get().getPacManDyingSprite().resetAnimation();
				theme.get().getPacManDyingSprite().setAnimated(true);
			}
		};
	}

	@Override
	public void init() {
		super.init();
		freezeTimer = 0;
		moveDir = W;
		nextMoveDir = W;
		control.init();
	}

	@Override
	public void update() {
		if (freezeTimer > 0) {
			--freezeTimer;
			return;
		}
		control.update();
	}

	public Set<Ghost> enemies() {
		return enemies;
	}

	public void setLogger(Logger logger) {
		control.setLogger(logger, app.motor.getFrequency());
	}

	public void killed() {
		control.feed(PacManEvent.Killed);
	}

	public void beginPowerWalking(int seconds) {
		control.feed(PacManEvent.PowerWalkingStarts);
	}

	public void beginWalking() {
		control.feed(PacManEvent.WalkingStarts);
	}

	@Override
	public Sprite currentSprite() {
		if (control.is(Dying)) {
			return theme.get().getPacManDyingSprite() != null ? theme.get().getPacManDyingSprite()
					: theme.get().getPacManStandingSprite(moveDir);
		}
		if (control.is(Initialized) || stuck) {
			return theme.get().getPacManStandingSprite(moveDir);
		}
		return theme.get().getPacManRunningSprite(moveDir);
	}

	@Override
	public void setAnimated(boolean animated) {
		Top4.dirs().forEach(dir -> {
			theme.get().getPacManRunningSprite(dir).setAnimated(animated);
		});
	}

	public void freeze(int frames) {
		this.freezeTimer = frames;
	}

	public boolean isPowerWalkingEnding() {
		return control.is(PowerWalking)
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

	private void requestMoveDirection() {
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
		g.translate(xOffset.getAsInt(), 0);
		super.draw(g);
		g.translate(-xOffset.getAsInt(), 0);
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
			drawCollisionBox(g, isAdjusted() ? Color.GREEN : Color.LIGHT_GRAY);
		}
	}
}