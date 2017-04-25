package de.amr.games.pacman.entities.ghost;

import static de.amr.easy.game.Application.Entities;
import static de.amr.games.pacman.PacManGame.Data;
import static de.amr.games.pacman.data.Board.TOPOLOGY;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Dead;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Frightened;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Recovering;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.entities.ghost.behaviors.GhostState.Waiting;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.amr.easy.game.Application;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.data.TileContent;
import de.amr.games.pacman.entities.PacMan;
import de.amr.games.pacman.entities.PacManGameEntity;
import de.amr.games.pacman.entities.ghost.behaviors.GhostAction;
import de.amr.games.pacman.entities.ghost.behaviors.GhostLoopingAroundWalls;
import de.amr.games.pacman.entities.ghost.behaviors.GhostState;
import de.amr.games.pacman.fsm.State;
import de.amr.games.pacman.fsm.StateMachine;

/**
 * A ghost.
 */
public class Ghost extends PacManGameEntity {

	public final Color color;
	public final List<Integer> route = new LinkedList<>();
	public final StateMachine<GhostState> control;
	public Supplier<GhostState> stateAfterFrightened;

	private GhostAction action;

	@Override
	public String toString() {
		return String.format("Ghost[name=%s,row=%d, col=%d]", getName(), getRow(), getCol());
	}

	public Ghost(GhostName ghostName, Color color, float homeRow, float homeCol) {
		super(new Tile(homeRow, homeCol));
		this.color = color;
		setName(ghostName.name());
		control = new StateMachine<>(getName(), new EnumMap<>(GhostState.class));
	}

	@Override
	public void init() {
		route.clear();
		setAnimated(false);
		action = null;
		control.changeTo(Waiting);
	}

	// --- State machine ---

	@Override
	public void update() {
		control.update();
		if (!control.state().isTerminated()) {
			return;
		}
		if (action != null) {
			nextAction();
		}
	}

	public void perform(GhostAction action) {
		this.action = action;
		if (!control.inState(Dead, Recovering)) {
			control.state().terminate();
		}
	}

	private void nextAction() {
		Application.Log.info(getName() + " performs action: " + action);
		switch (action) {
		case Chase:
			if (control.stateID() != Frightened) {
				control.changeTo(Chasing);
			}
			break;
		case Scatter:
			if (control.stateID() != Frightened) {
				control.changeTo(Scattering);
			}
			break;
		case GetFrightened:
			control.changeTo(Frightened);
			break;
		case EndFrightened:
			control.changeTo(stateAfterFrightened.get());
			break;
		case Die:
			control.changeTo(Dead);
			break;
		}
		action = null;
	}

	// --- Look ---

	@Override
	public Sprite currentSprite() {
		if (insideGhostHouse()) {
			return getTheme().getGhostNormal(GhostName.valueOf(getName()), moveDir);
		}
		if (control.inState(Frightened)) {
			PacMan pacMan = Entities.findAny(PacMan.class);
			return pacMan.isFrighteningEnding() ? getTheme().getGhostRecovering() : getTheme().getGhostFrightened();
		}
		if (control.inState(Recovering)) {
			return getTheme().getGhostRecovering();
		}
		if (control.inState(Dead)) {
			return getTheme().getGhostDead(moveDir);
		}
		return getTheme().getGhostNormal(GhostName.valueOf(getName()), moveDir);
	}

	@Override
	public void setAnimated(boolean animated) {
		TOPOLOGY.dirs().forEach(dir -> {
			getTheme().getGhostNormal(GhostName.valueOf(getName()), dir).setAnimated(animated);
			getTheme().getGhostDead(dir).setAnimated(animated);
		});
		getTheme().getGhostFrightened().setAnimated(animated);
		getTheme().getGhostRecovering().setAnimated(animated);
	}

	// -- Movement

	public void moveBackAndForth() {
		if (!move()) {
			changeMoveDir(TOPOLOGY.inv(moveDir));
		}
	}

	public void moveRandomly() {
		move();
		if (!isExactlyOverTile()) {
			return;
		}
		List<Integer> dirsPermuted = TOPOLOGY.dirsPermuted().boxed().collect(Collectors.toList());
		for (int dir : dirsPermuted) {
			Tile targetTile = currentTile().translate(TOPOLOGY.dx(dir), TOPOLOGY.dy(dir));
			if (targetTile.getCol() < 0) {
				continue; // TODO
			}
			if (Data.board.contains(targetTile, TileContent.Wormhole)) {
				moveDir = TOPOLOGY.inv(moveDir);
				return;
			}
			if (dir == TOPOLOGY.inv(moveDir)) {
				return;
			}
			if (canEnter(targetTile)) {
				changeMoveDir(dir);
				break;
			}
		}
	}

	// --- Navigation ---

	public void computeRoute(Tile target) {
		route.clear();
		route.addAll(Data.routeMap.shortestRoute(currentTile(), target));
	}

	public void followRoute(Tile target) {
		computeRoute(target);
		followRoute();
	}

	public void followRoute() {
		if (!route.isEmpty()) {
			changeMoveDir(route.get(0));
		}
		move();
	}

	public void walkHome() {
		followRoute(home);
	}

	public void leaveGhostHouse() {
		followRoute(new Tile(14, 13));
	}

	@Override
	public boolean move() {
		if (Data.board.contains(currentTile(), TileContent.Tunnel)) {
			speed = Data.getGhostSpeedInTunnel();
		} else if (Data.board.contains(currentTile(), TileContent.GhostHouse)) {
			speed = Data.getGhostSpeedInHouse();
		} else if (control.inState(Frightened)) {
			speed = Data.getGhostSpeedWhenFrightened();
		} else {
			speed = Data.getGhostSpeedNormal();
		}
		return super.move();
	}

	// --- predicates

	public boolean insideGhostHouse() {
		return Data.board.contains(currentTile(), TileContent.GhostHouse);
	}

	@Override
	public boolean canEnter(Tile targetTile) {
		if (Data.board.contains(targetTile, TileContent.Door)) {
			if (control.inState(Dead))
				return true; // eyes can pass through door
			if (control.inState(Waiting))
				return false; // while waiting door is closed

			// when inside ghost house or already in door, ghost can walk through
			return insideGhostHouse() || Data.board.contains(currentTile(), TileContent.Door);
		} else if (Data.board.contains(targetTile, TileContent.Wall)) {
			return false;
		}
		return true;
	}

	// --- Drawing ---

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		if (Application.Settings.getBool("drawInternals")) {
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
			GhostLoopingAroundWalls state = (GhostLoopingAroundWalls) control.state();
			if (state.hasLoopStarted()) {
				tile = new Tile(state.getLoopStart());
			}
		}
		int offset = TILE_SIZE / 2;
		for (int dir : route) {
			Tile nextTile = new Tile(tile).translate(TOPOLOGY.dx(dir), TOPOLOGY.dy(dir));
			g.drawLine(tile.getCol() * TILE_SIZE + offset, tile.getRow() * TILE_SIZE + offset,
					nextTile.getCol() * TILE_SIZE + offset, nextTile.getRow() * TILE_SIZE + offset);
			tile = nextTile;
		}
	}
}
