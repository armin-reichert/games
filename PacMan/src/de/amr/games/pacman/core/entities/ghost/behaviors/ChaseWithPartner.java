package de.amr.games.pacman.core.entities.ghost.behaviors;

import static de.amr.easy.grid.impl.Top4.Top4;

import de.amr.easy.statemachine.State;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.BoardMover;
import de.amr.games.pacman.play.PlayScene;

/**
 * @author Armin Reichert
 */
public class ChaseWithPartner extends State {

	public ChaseWithPartner(BoardMover chaser, BoardMover partner, BoardMover target) {
		update = state -> {
			Board board = chaser.getBoard();
			if (board.contains(chaser.currentTile(), TileContent.GhostHouse)) {
				chaser.follow(PlayScene.GHOST_HOUSE_ENTRY);
				return;
			}
			Tile middle = new Tile(target.getRow() + 2 * Top4.dy(target.getMoveDir()),
					target.getCol() + 2 * Top4.dx(target.getMoveDir()));
			Tile partnerTile = partner.currentTile();
			int dx = middle.col - partnerTile.col;
			int dy = middle.row - partnerTile.row;
			Tile targetTile = new Tile(partnerTile.row + 2 * dy, partnerTile.col + 2 * dx);
			if (board.isBoardTile(targetTile)) {
				chaser.setRoute(board.shortestRoute(chaser.currentTile(), targetTile));
				if (!chaser.getRoute().isEmpty()) {
					int chaseDir = chaser.getRoute().get(0);
					if (chaseDir == Top4.inv(chaser.getMoveDir())) {
						chaser.move();
						return;
					}
					if (chaser.canEnterTileTowards(chaseDir)) {
						chaser.moveAlongRoute();
						return;
					}
				}
			}
			chaser.moveRandomly();
		};
	}
}
