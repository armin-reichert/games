package de.amr.games.pacman.entities.ghost.behaviors;

import static de.amr.games.pacman.PacManGame.Game;
import static de.amr.games.pacman.data.Board.TOPOLOGY;

import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.entities.PacMan;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.fsm.State;

/**
 * @author Armin Reichert
 *
 */
public class DirectOrProactiveChasing extends State {

	private final Ghost chasingGhost;
	private final Ghost helperGhost;
	private final PacMan pacMan;

	public DirectOrProactiveChasing(Ghost chasingGhost, Ghost helperGhost, PacMan pacMan) {
		this.chasingGhost = chasingGhost;
		this.helperGhost = helperGhost;
		this.pacMan = pacMan;
		update = state -> chase();
	}

	private void chase() {
		Tile middle = new Tile(pacMan.getRow() + 2 * TOPOLOGY.dy(pacMan.moveDir),
				pacMan.getCol() + 2 * TOPOLOGY.dx(pacMan.moveDir));
		Tile helperGhostTile = helperGhost.currentTile();
		int dx = middle.getCol() - helperGhostTile.getCol();
		int dy = middle.getRow() - helperGhostTile.getRow();
		Tile targetTile = new Tile(helperGhostTile.getRow() + 2 * dy, helperGhostTile.getCol() + 2 * dx);
		if (Game.board.isTileValid(targetTile)) {
			chasingGhost.computeRoute(targetTile);
			if (!chasingGhost.route.isEmpty()) {
				int chaseDir = chasingGhost.route.get(0);
				if (chaseDir == TOPOLOGY.inv(chasingGhost.moveDir)) {
					chasingGhost.move();
					return;
				}
				if (chasingGhost.canMoveTowards(chaseDir)) {
					chasingGhost.followRoute();
					return;
				}
			}
		}
		chasingGhost.moveRandomly();
	}
}
