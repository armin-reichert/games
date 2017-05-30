package de.amr.games.pacman.core.entities.ghost.behaviors;

import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.BoardMover;
import de.amr.games.pacman.core.statemachine.State;

/**
 * @author Armin Reichert
 */
public class ChaseWithPartner extends State {

	public ChaseWithPartner(BoardMover chaser, BoardMover partner, BoardMover target) {
		update = state -> {
			Board board = chaser.getBoard();
			Tile middle = new Tile(target.getRow() + 2 * Top4.INSTANCE.dy(target.getMoveDir()),
					target.getCol() + 2 * Top4.INSTANCE.dx(target.getMoveDir()));
			Tile partnerTile = partner.currentTile();
			int dx = middle.getCol() - partnerTile.getCol();
			int dy = middle.getRow() - partnerTile.getRow();
			Tile targetTile = new Tile(partnerTile.getRow() + 2 * dy, partnerTile.getCol() + 2 * dx);
			if (board.isTileValid(targetTile)) {
				chaser.setRoute(board.shortestRoute(chaser.currentTile(), targetTile));
				if (!chaser.getRoute().isEmpty()) {
					int chaseDir = chaser.getRoute().get(0);
					if (chaseDir == Top4.INSTANCE.inv(chaser.getMoveDir())) {
						chaser.move();
						return;
					}
					if (chaser.canMoveTowards(chaseDir)) {
						chaser.moveAlongRoute();
						return;
					}
				}
			}
			chaser.moveRandomly();
		};
	}
}
