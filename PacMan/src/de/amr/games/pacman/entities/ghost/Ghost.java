package de.amr.games.pacman.entities.ghost;

import static de.amr.easy.game.Application.Log;
import static de.amr.games.pacman.PacManGame.Game;
import static de.amr.games.pacman.data.TileContent.Door;
import static de.amr.games.pacman.data.TileContent.GhostHouse;
import static de.amr.games.pacman.data.TileContent.Wormhole;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Frightened;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Waiting;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;
import static java.util.Collections.emptyList;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.data.TileContent;
import de.amr.games.pacman.entities.PacManGameEntity;
import de.amr.games.pacman.entities.ghost.behaviors.GhostMessage;
import de.amr.games.pacman.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.entities.ghost.behaviors.LoopAroundWalls;
import de.amr.games.pacman.fsm.State;
import de.amr.games.pacman.fsm.StateMachine;

/**
 * A ghost.
 */
public class Ghost extends PacManGameEntity {

	public final StateMachine<GhostState> control;
	public Supplier<GhostState> stateAfterFrightened;
	private Color color;
	private GhostMessage message;

	@Override
	public String toString() {
		return String.format("Ghost[name=%s,row=%d, col=%d]", getName(), getRow(), getCol());
	}

	public Ghost(Board board, Tile home) {
		super(board, home);
		this.color = Color.WHITE;
		control = new StateMachine<>("Ghost", new EnumMap<>(GhostState.class));
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
			return getTheme().getGhostNormal(getName(), moveDir);
		}
		if (control.inState(Frightened)) {
			return /* pacMan.isFrighteningEnding() ? getTheme().getGhostRecovering() : */getTheme().getGhostFrightened();
		}
		if (control.inState(Recovering)) {
			return getTheme().getGhostRecovering();
		}
		if (control.inState(Dead)) {
			return getTheme().getGhostDead(moveDir);
		}
		return getTheme().getGhostNormal(getName(), moveDir);
	}

	@Override
	public void setAnimated(boolean animated) {
		board.topology.dirs().forEach(dir -> {
			getTheme().getGhostNormal(getName(), dir).setAnimated(animated);
			getTheme().getGhostDead(dir).setAnimated(animated);
		});
		getTheme().getGhostFrightened().setAnimated(animated);
		getTheme().getGhostRecovering().setAnimated(animated);
	}

	// -- Movement

	public void moveBackAndForth() {
		if (!move()) {
			changeMoveDir(board.topology.inv(moveDir));
		}
	}

	public void moveRandomly() {
		move();
		if (!isExactlyOverTile()) {
			return;
		}
		List<Integer> dirsPermuted = board.topology.dirsPermuted().boxed().collect(Collectors.toList());
		for (int dir : dirsPermuted) {
			Tile targetTile = currentTile().neighbor(dir);
			if (targetTile.getCol() < 0) {
				continue; // TODO
			}
			if (board.contains(targetTile, Wormhole)) {
				moveDir = board.topology.inv(moveDir);
				return;
			}
			if (dir == board.topology.inv(moveDir)) {
				return;
			}
			if (canEnter(targetTile)) {
				changeMoveDir(dir);
				break;
			}
		}
	}

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
		if (Game.settings.getBool("drawInternals")) {
			drawState(g);
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
		if (control.inState(Frightened) || control.inState(Waiting)) {
			return;
		}
		if (route.isEmpty()) {
			return;
		}
		g.setColor(color);
		g.setStroke(new BasicStroke(1f));
		Tile tile = currentTile();
		if (control.inState(Scattering)) {
			LoopAroundWalls state = (LoopAroundWalls) control.state();
			if (state.hasLoopStarted()) {
				tile = new Tile(state.getLoopStart());
			}
		}
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
