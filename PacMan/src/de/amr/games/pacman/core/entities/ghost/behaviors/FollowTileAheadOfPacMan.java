package de.amr.games.pacman.core.entities.ghost.behaviors;

import static de.amr.games.pacman.core.board.TileContent.GhostHouse;

import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.BoardMover;
import de.amr.games.pacman.core.statemachine.State;

/**
 * A state in which the given ghost targets the tile which is located the given number of tiles
 * ahead of PacMan's current move direction.
 * 
 * @author Armin Reichert
 */
public class FollowTileAheadOfPacMan extends State {

	public FollowTileAheadOfPacMan(BoardMover chaser, BoardMover target, int numTilesAhead) {
		update = state -> {
			Board board = chaser.getBoard();
			int dir = target.getMoveDir();
			for (int numTiles = numTilesAhead; numTiles >= 0; --numTiles) {
				Tile targetTile = target.currentTile().translate(numTiles * board.topology.dx(dir),
						numTiles * board.topology.dy(dir));
				if (board.isTileValid(targetTile) && !board.contains(targetTile, GhostHouse) && chaser.canEnter(targetTile)) {
					chaser.follow(targetTile);
					return;
				}
			}
		};
	}
}
