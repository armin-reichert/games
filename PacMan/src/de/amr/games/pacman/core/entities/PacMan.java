package de.amr.games.pacman.core.entities;

import static de.amr.easy.game.Application.LOG;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.Top4;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.TileContent.Door;
import static de.amr.games.pacman.core.board.TileContent.None;
import static de.amr.games.pacman.core.board.TileContent.Wall;
import static de.amr.games.pacman.core.entities.PacManState.Initialized;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.amr.easy.game.Application;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * The Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan extends BoardMover {

	private static final Font TEXTFONT = new Font(Font.DIALOG, Font.PLAIN, TILE_SIZE * 9 / 10);

	public StateMachine<PacManState, PacManEvent> control;
	public Supplier<Integer> steering;
	public Consumer<TileContent> onContentFound;
	public Consumer<Ghost> onEnemyContact;

	private final Application app;
	private final Supplier<PacManTheme> theme;
	private final Set<Ghost> enemies;
	private int freezeTimer;

	public PacMan(Application app, Board board, Supplier<PacManTheme> theme) {
		super(board);
		this.app = app;
		this.theme = theme;
		this.enemies = new HashSet<>();
		setName("Pac-Man");
		control = new StateMachine<>("Pac-Man Control", PacManState.class, Initialized);
		steering = this::defaultSteering;
		canEnterTile = this::defaultCanEnterTileCondition;
		onContentFound = this::defaultContentFoundHandler;
		onEnemyContact = this::defaultEnemyContactHandler;
	}

	private void defaultEnemyContactHandler(Ghost enemy) {
		LOG.info("PacMan meets enemy '" + enemy.getName() + "' at " + currentTile());
	}

	private boolean defaultCanEnterTileCondition(Tile tile) {
		return board.isBoardTile(tile) && !(board.contains(tile, Wall) || board.contains(tile, Door));
	}

	private void defaultContentFoundHandler(TileContent content) {
		switch (content) {
		case Bonus:
		case Energizer:
		case Pellet:
			LOG.info(getName() + " eats " + content + " at " + currentTile());
			board.setContent(currentTile(), None);
			break;
		default:
			LOG.info("PacMan visits " + content + " at " + currentTile());
			break;
		}
	}

	private int defaultSteering() {
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
		control.setLogger(logger);
		control.setFrequency(app.pulse.getFrequency());
	}

	public void receiveEvent(PacManEvent event) {
		control.addInput(event);
	}

	@Override
	public Sprite currentSprite() {
		switch (control.stateID()) {
		case Dying:
			return theme.get().getPacManDyingSprite() != null ? theme.get().getPacManDyingSprite()
					: theme.get().getPacManStandingSprite(moveDir);
		case Initialized:
			return theme.get().getPacManStandingSprite(moveDir);
		default:
			return stuck ? theme.get().getPacManStandingSprite(moveDir) : theme.get().getPacManRunningSprite(moveDir);
		}
	}

	@Override
	public void setAnimated(boolean animated) {
		Top4.dirs().forEach(dir -> {
			theme.get().getPacManRunningSprite(dir).setAnimationEnabled(animated);
		});
	}

	public void freeze(int frames) {
		this.freezeTimer = frames;
	}

	public void walk() {
		nextMoveDir = steering.get();
		move();
		turnTo(nextMoveDir);
		TileContent content = board.getContent(currentTile());
		if (content != None) {
			onContentFound.accept(content);
		}
		enemies.stream().filter(enemy -> enemy.getCol() == getCol() && enemy.getRow() == getRow()).forEach(onEnemyContact);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(xOffset.getAsInt(), 0);
		super.draw(g);
		g.translate(-xOffset.getAsInt(), 0);

		if (app.settings.getAsBoolean("drawInternals")) {
			g.setColor(Color.WHITE);
			g.setFont(TEXTFONT);
			State state = control.state();
			StringBuilder text = new StringBuilder();
			text.append(getName()).append(" (").append(control.stateID());
			if (state.getDuration() != State.FOREVER) {
				text.append(":").append(state.getRemaining()).append("|").append(state.getDuration());
			}
			text.append(")");
			g.drawString(text.toString(), tf.getX(), tf.getY() - TILE_SIZE);
		}

		if (app.settings.getAsBoolean("drawGrid")) {
			g.setColor(isAdjusted() ? Color.GREEN : Color.LIGHT_GRAY);
			g.draw(getCollisionBox());
		}
	}
}