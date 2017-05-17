package de.amr.games.pacman.core.entities.ghost;

import static de.amr.easy.game.Application.Log;
import static de.amr.games.pacman.core.board.TileContent.Door;
import static de.amr.games.pacman.core.board.TileContent.GhostHouse;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Frightened;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Waiting;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;
import static java.util.Collections.emptyList;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.function.Supplier;

import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.core.app.AbstractPacManApp;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.PacManEntity;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostMessage;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.core.statemachine.State;
import de.amr.games.pacman.core.statemachine.StateMachine;

/**
 * A ghost.
 */
public class Ghost extends PacManEntity {

	public final StateMachine<GhostState> control;
	public Supplier<GhostState> stateAfterFrightened;
	private Color color;
	private GhostMessage message;

	@Override
	public String toString() {
		return String.format("Ghost[name=%s,row=%d, col=%d]", getName(), getRow(), getCol());
	}

	public Ghost(AbstractPacManApp app, Board board, String name, Tile home) {
		super(app, board, home);
		setName(name);
		this.color = Color.WHITE;
		control = new StateMachine<>("Ghost " + name, new EnumMap<>(GhostState.class));
		stateAfterFrightened = () -> control.stateID();
	}

	@Override
	public void init() {
		message = null;
		route = emptyList();
		setAnimated(false);
		control.changeTo(Waiting);
	}

	@Override
	public void update() {
		control.update();
		if (!control.state().isTerminated()) {
			return;
		}
		if (message != null) {
			processMessage();
		}
	}

	public void receive(GhostMessage message) {
		this.message = message;
		if (!control.inState(Dead, Recovering)) {
			control.state().terminate();
		}
	}

	private void processMessage() {
		Log.info(getName() + " handles message: " + message);
		switch (message) {
		case StartChasing:
			if (control.stateID() != Frightened) {
				control.changeTo(Chasing);
			}
			break;
		case StartScattering:
			if (control.stateID() != Frightened) {
				control.changeTo(Scattering);
			}
			break;
		case StartBeingFrightened:
			control.changeTo(Frightened);
			break;
		case EndBeingFrightened:
			control.changeTo(stateAfterFrightened.get());
			break;
		case Die:
			control.changeTo(Dead);
			break;
		}
		message = null;
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
			return /* pacMan.isFrighteningEnding() ? app.getTheme().getGhostRecovering() : */app.getTheme()
					.getGhostFrightenedSprite();
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
		board.topology.dirs().forEach(dir -> {
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
	public boolean canEnter(Tile targetTile) {
		if (board.contains(targetTile, Door)) {
			if (control.inState(Dead))
				return true; // eyes can pass through door
			if (control.inState(Waiting))
				return false; // while waiting door is closed

			// when inside ghost house or already in door, ghost can walk through
			return insideGhostHouse() || board.contains(currentTile(), Door);
		} else if (board.contains(targetTile, TileContent.Wall)) {
			return false;
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
			drawRoute(g);
		}
	}

	private void drawState(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, TILE_SIZE * 9 / 10));
		State state = control.state();
		StringBuilder text = new StringBuilder();
		text.append(getName()).append(" (").append(control.stateID());
		if (state.getDuration() != State.FOREVER) {
			text.append(state.getTimer()).append("|").append(state.getDuration());
		}
		text.append(")");
		g.drawString(text.toString(), tr.getX(), tr.getY() - 10);
	}

	private void drawRoute(Graphics2D g) {
		if (route.isEmpty()) {
			return;
		}
		g.setColor(color);
		g.setStroke(new BasicStroke(1f));
		Tile tile = currentTile();
		int offset = TILE_SIZE / 2;
		for (int dir : route) {
			Tile nextTile = tile.neighbor(dir);
			g.drawLine(tile.getCol() * TILE_SIZE + offset, tile.getRow() * TILE_SIZE + offset,
					nextTile.getCol() * TILE_SIZE + offset, nextTile.getRow() * TILE_SIZE + offset);
			tile = nextTile;
		}
		g.fillRect(tile.getCol() * TILE_SIZE + TILE_SIZE / 4, tile.getRow() * TILE_SIZE + TILE_SIZE / 4, TILE_SIZE / 2,
				TILE_SIZE / 2);
	}
}
