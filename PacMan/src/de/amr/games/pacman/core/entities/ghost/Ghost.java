package de.amr.games.pacman.core.entities.ghost;

import static de.amr.easy.grid.impl.Top4.Top4;
import static de.amr.games.pacman.core.board.TileContent.GhostHouse;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Frightened;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Initialized;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Waiting;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.amr.easy.game.Application;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.entities.BoardMover;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.core.statemachine.State;
import de.amr.games.pacman.core.statemachine.StateMachine;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends BoardMover {

	private final Application app;
	private final Supplier<PacManTheme> theme;
	private final StateMachine<GhostState> control;

	public Supplier<GhostState> stateToRestore;

	private Color color;

	public Ghost(Application app, Board board, String name, Supplier<PacManTheme> theme) {
		super(board);
		setName(name);

		this.app = app;
		this.theme = theme;
		this.control = new StateMachine<>(name, new EnumMap<>(GhostState.class));

		color = Color.WHITE;

		stateToRestore = () -> control.stateID();

		canEnterTile = tile -> {
			if (!board.isTileValid(tile)) {
				return false;
			}
			switch (board.getContent(tile)) {
			case Wall:
				return false;
			case Door:
				return control.inState(Dead) || insideGhostHouse() && control.inState(Scattering, Chasing);
			default:
				return true;
			}
		};
	}

	@Override
	public void init() {
		super.init();
		setAnimated(false);
		control.changeTo(Initialized);
	}

	@Override
	public void update() {
		control.update();
	}

	public void setLogger(Logger logger) {
		control.setLogger(logger, app.motor.getFrequency());
	}

	public GhostState state() {
		return control.stateID();
	}

	public State state(GhostState stateID) {
		return control.state(stateID);
	}

	public State state(GhostState stateID, State state) {
		return control.state(stateID, state);
	}

	// Events

	public void beginWaiting(int frames) {
		setAnimated(true);
		control.state(Waiting).setDuration(frames);
		control.changeTo(Waiting);
	}

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
		restoreState();
	}

	public void beginRecovering(int frames) {
		if (control.stateID() != Dead) {
			throw new IllegalStateException("Attempt to recover when not dead");
		}
		control.state(Recovering).setDuration(frames);
		control.changeTo(Recovering);
	}

	public void killed() {
		control.changeTo(Dead);
	}

	public void restoreState() {
		control.changeTo(stateToRestore.get());
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
		if (control.inState(Dead)) {
			return theme.get().getGhostDeadSprite(moveDir);
		}
		if (control.inState(Recovering)) {
			return theme.get().getGhostRecoveringSprite();
		}
		if (insideGhostHouse()) {
			return theme.get().getGhostNormalSprite(getName(), moveDir);
		}
		if (control.inState(Frightened)) {
			return control.state().getRemaining() < control.state().getDuration() / 3 ? theme.get().getGhostRecoveringSprite()
					: theme.get().getGhostFrightenedSprite();
		}
		return theme.get().getGhostNormalSprite(getName(), moveDir);
	}

	@Override
	public void setAnimated(boolean animated) {
		Top4.dirs().forEach(dir -> {
			theme.get().getGhostNormalSprite(getName(), dir).setAnimated(animated);
			theme.get().getGhostDeadSprite(dir).setAnimated(animated);
		});
		theme.get().getGhostFrightenedSprite().setAnimated(animated);
		theme.get().getGhostRecoveringSprite().setAnimated(animated);
	}

	public boolean insideGhostHouse() {
		return board.contains(currentTile(), GhostHouse);
	}

	// --- Drawing ---

	@Override
	public void draw(Graphics2D g) {
		g.translate(xOffset.getAsInt(), 0);
		super.draw(g);
		g.translate(-xOffset.getAsInt(), 0);
		if (app.settings.getBool("drawInternals")) {
			drawState(g);
		}
		if (app.settings.getBool("drawRoute")) {
			drawRoute(g, color);
		}
		if (app.settings.getBool("drawGrid")) {
			drawCollisionBox(g, isAdjusted() ? Color.GREEN : Color.LIGHT_GRAY);
		}
	}

	private void drawState(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, TILE_SIZE * 9 / 10));
		State state = control.state();
		StringBuilder text = new StringBuilder();
		text.append(getName()).append(" (").append(control.stateID());
		if (state.getDuration() != State.FOREVER) {
			text.append(":" + state.getRemaining()).append("|").append(state.getDuration());
		}
		text.append(")");
		g.drawString(text.toString(), tr.getX(), tr.getY() - 10);
	}
}