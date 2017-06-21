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
import static de.amr.games.pacman.core.entities.PacManState.Aggressive;
import static de.amr.games.pacman.core.entities.PacManState.Dying;
import static de.amr.games.pacman.core.entities.PacManState.Initialized;
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
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.statemachine.State;
import de.amr.games.pacman.core.statemachine.StateMachine;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * The Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan extends BoardMover {

	public final StateMachine<PacManState, PacManEvent> control;

	public Consumer<TileContent> onContentFound;
	public Consumer<Ghost> onEnemyContact;

	private final Application app;
	private final Supplier<PacManTheme> theme;
	private final Set<Ghost> enemies;
	private int freezeTimer;

	public PacMan(Application app, Board board, Supplier<PacManTheme> theme) {
		super(board);
		this.app = Objects.requireNonNull(app);
		this.theme = theme;
		this.enemies = new HashSet<>();
		setName("Pac-Man");
		control = new StateMachine<>(getName(), new EnumMap<>(PacManState.class), Initialized);
		definePacManControl();
		canEnterTile = this::defaultCanEnterTileCondition;
		onContentFound = this::defaultContentFoundHandler;
		onEnemyContact = this::defaultEnemyContactHandler;
	}

	private void defaultEnemyContactHandler(Ghost enemy) {
		Log.info("PacMan meets enemy '" + enemy.getName() + "' at " + currentTile());
	}

	private boolean defaultCanEnterTileCondition(Tile tile) {
		return board.isTileValid(tile) && !(board.contains(tile, Wall) || board.contains(tile, Door));
	}

	private void defaultContentFoundHandler(TileContent content) {
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
	}

	private void definePacManControl() {

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

	public void handleEvent(PacManEvent event) {
		control.addInput(event);
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
		return control.is(Aggressive)
				&& control.state(Aggressive).getRemaining() < control.state(Aggressive).getDuration() / 4;
	}

	public void walk() {
		requestMoveDirection();
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