package de.amr.games.pacman.core.entities.ghost;

import static de.amr.games.pacman.core.board.TileContent.Door;
import static de.amr.games.pacman.core.board.TileContent.GhostHouse;
import static de.amr.games.pacman.core.board.TileContent.Wall;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Frightened;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Initialized;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Waiting;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.function.Supplier;

import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.core.app.AbstractPacManApp;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.BoardMover;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.core.statemachine.State;
import de.amr.games.pacman.core.statemachine.StateMachine;

/**
 * A ghost.
 */
public class Ghost extends BoardMover {

	public final StateMachine<GhostState> control;
	public Supplier<GhostState> stateToRestore;

	private final AbstractPacManApp app;
	private Color color;

	@Override
	public String toString() {
		return format("Ghost[name=%s,row=%d, col=%d]", getName(), getRow(), getCol());
	}

	public Ghost(AbstractPacManApp app, Board board, String name, Tile home) {
		super(board, home);
		this.app = app;
		setName(name);
		color = Color.WHITE;
		control = new StateMachine<>("Ghost " + name, new EnumMap<>(GhostState.class));
		stateToRestore = () -> control.stateID();
	}

	@Override
	public void init() {
		route = emptyList();
		setAnimated(false);
		placeAt(getHome());
		control.changeTo(Initialized);
	}

	@Override
	public void update() {
		control.update();
	}

	public void setWaitingTime(int frames) {
		control.state(Waiting).setDuration(frames);
	}

	// Events

	public void beginScattering() {
		if (control.inState(Frightened, Dead) || control.inState(Waiting) && !control.state().isTerminated()) {
			return;
		}
		control.changeTo(Scattering);
	}

	public void beginChasing() {
		if (control.inState(Frightened, Dead) || control.inState(Waiting) && !control.state().isTerminated()) {
			return;
		}
		control.changeTo(Chasing);
	}

	public void beginBeingFrightened(int frames) {
		if (control.inState(Dead)) {
			return;
		}
		control.state(Frightened).setDuration(frames);
		control.changeTo(Frightened);
	}

	public void endBeingFrightened() {
		if (control.inState(Dead)) {
			return;
		}
		control.changeTo(stateToRestore.get());
	}

	public void killed() {
		control.changeTo(Dead);
	}

	// --- Look ---

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public Sprite currentSprite() {
		if (insideGhostHouse()) {
			return app.getTheme().getGhostNormalSprite(getName(), moveDir);
		}
		if (control.inState(Frightened)) {
			if (control.state().getRemaining() < control.state().getDuration() / 3) {
				return app.getTheme().getGhostRecoveringSprite();
			}
			return app.getTheme().getGhostFrightenedSprite();
		}
		if (control.inState(Recovering)) {
			return app.getTheme().getGhostRecoveringSprite();
		}
		if (control.inState(Dead)) {
			return app.getTheme().getGhostDeadSprite(moveDir);
		}
		return app.getTheme().getGhostNormalSprite(getName(), moveDir);
	}

	@Override
	public void setAnimated(boolean animated) {
		Top4.INSTANCE.dirs().forEach(dir -> {
			app.getTheme().getGhostNormalSprite(getName(), dir).setAnimated(animated);
			app.getTheme().getGhostDeadSprite(dir).setAnimated(animated);
		});
		app.getTheme().getGhostFrightenedSprite().setAnimated(animated);
		app.getTheme().getGhostRecoveringSprite().setAnimated(animated);
	}

	// -- Movement

	public boolean insideGhostHouse() {
		return board.contains(currentTile(), GhostHouse);
	}

	@Override
	public boolean canEnter(Tile tile) {
		if (board.contains(tile, Wall)) {
			return false;
		}
		if (board.contains(tile, Door)) {
			if (control.inState(Dead)) {
				// dead ghost (eyes) can pass through door
				return true;
			} else if (control.inState(Waiting)) {
				// while waiting inside ghost house, ghost cannot pass through door
				return false;
			} else {
				// when inside ghost house or already in door, ghost can walk through
				return insideGhostHouse() || board.contains(currentTile(), Door);
			}
		}
		return true;
	}

	// --- Drawing ---

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		if (app.settings.getBool("drawInternals")) {
			drawState(g);
		}
		if (app.settings.getBool("drawRoute")) {
			drawRoute(g, color);
		}
		if (app.settings.getBool("drawGrid")) {
			drawCollisionBox(g, isExactlyOverTile() ? Color.GREEN : Color.LIGHT_GRAY);
		}
	}

	private void drawState(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, TILE_SIZE * 9 / 10));
		State state = control.state();
		StringBuilder text = new StringBuilder();
		text.append(getName()).append(" (").append(control.stateID());
		if (state.getDuration() != State.FOREVER) {
			text.append(state.getRemaining()).append("|").append(state.getDuration());
		}
		text.append(")");
		g.drawString(text.toString(), tr.getX(), tr.getY() - 10);
	}

}
