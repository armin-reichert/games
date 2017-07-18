package de.amr.games.pacman.core.entities.ghost;

import static de.amr.easy.grid.impl.Top4.Top4;
import static de.amr.games.pacman.core.board.TileContent.GhostHouse;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Initialized;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.function.Supplier;
import java.util.logging.Logger;

import de.amr.easy.game.Application;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.BoardMover;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * A ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost extends BoardMover {

	public StateMachine<GhostState, GhostEvent> control;
	public Runnable resume;
	public Color color;

	private final Application app;
	private final Supplier<PacManTheme> theme;

	public Ghost(Application app, Board board, String name, Supplier<PacManTheme> theme) {
		super(board);
		setName(name);
		this.app = app;
		this.theme = theme;
		this.control = new StateMachine<>(name, GhostState.class, Initialized);
		color = Color.WHITE;
		resume = () -> {
		};
		canEnterTile = this::defaultCanEnterTileCondition;
	}

	private boolean defaultCanEnterTileCondition(Tile tile) {
		if (!board.isBoardTile(tile)) {
			return false;
		}
		switch (board.getContent(tile)) {
		case Wall:
			return false;
		case Door:
			return control.is(Dead) || insideGhostHouse() && control.is(Scattering, Chasing);
		default:
			return true;
		}
	}

	@Override
	public void init() {
		super.init();
		setAnimated(false);
		control.init();
	}

	@Override
	public void update() {
		control.update();
	}

	public void setLogger(Logger logger) {
		control.setLogger(logger);
		control.setFrequency(app.pulse.getFrequency());
	}

	public void receiveEvent(GhostEvent event) {
		control.addInput(event);
	}

	public boolean insideGhostHouse() {
		return board.contains(currentTile(), GhostHouse);
	}

	@Override
	public Sprite currentSprite() {
		switch (control.stateID()) {
		case Dead:
			return theme.get().getGhostDeadSprite(moveDir);
		case Recovering:
			return theme.get().getGhostRecoveringSprite();
		case Frightened:
			return control.state().getRemaining() < control.state().getDuration() / 3 ? theme.get().getGhostRecoveringSprite()
					: theme.get().getGhostFrightenedSprite();
		default:
			return theme.get().getGhostNormalSprite(getName(), moveDir);
		}
	}

	@Override
	public void setAnimated(boolean animated) {
		Top4.dirs().forEach(dir -> {
			theme.get().getGhostNormalSprite(getName(), dir).setAnimationEnabled(animated);
			theme.get().getGhostDeadSprite(dir).setAnimationEnabled(animated);
		});
		theme.get().getGhostFrightenedSprite().setAnimationEnabled(animated);
		theme.get().getGhostRecoveringSprite().setAnimationEnabled(animated);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(xOffset.getAsInt(), 0);
		super.draw(g);
		g.translate(-xOffset.getAsInt(), 0);
		if (app.settings.getAsBoolean("drawInternals")) {
			drawState(g);
		}
		if (app.settings.getAsBoolean("drawRoute")) {
			drawRoute(g, color);
		}
		if (app.settings.getAsBoolean("drawGrid")) {
			g.setColor(isAdjusted() ? Color.GREEN : Color.LIGHT_GRAY);
			g.draw(getCollisionBox());
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
		g.drawString(text.toString(), tf.getX(), tf.getY() - 10);
	}
}