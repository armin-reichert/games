package de.amr.games.pacman.entities.ghost.behaviors;

import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.entities.PacMan;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.fsm.State;

/**
 * @author Armin Reichert
 */
public class DirectOrProactiveChasing extends State {

	public DirectOrProactiveChasing(Ghost hunter, Ghost helper, PacMan pacMan) {
		final Board board = hunter.getBoard();
		final Tile middle = new Tile(pacMan.getRow() + 2 * board.topology.dy(pacMan.getMoveDir()),
				pacMan.getCol() + 2 * board.topology.dx(pacMan.getMoveDir()));
		final Tile helperGhostTile = helper.currentTile();
		final int dx = middle.getCol() - helperGhostTile.getCol();
		final int dy = middle.getRow() - helperGhostTile.getRow();
		final Tile targetTile = new Tile(helperGhostTile.getRow() + 2 * dy, helperGhostTile.getCol() + 2 * dx);
		update = state -> {
			if (board.isTileValid(targetTile)) {
				hunter.route = board.shortestRoute(hunter.currentTile(), targetTile);
				if (!hunter.route.isEmpty()) {
					int chaseDir = hunter.route.get(0);
					if (chaseDir == board.topology.inv(hunter.getMoveDir())) {
						hunter.move();
						return;
					}
					if (hunter.canMoveTowards(chaseDir)) {
						hunter.moveAlongRoute();
						return;
					}
				}
			}
			hunter.moveRandomly();
		};
	}
}
