package de.amr.games.pacman.core.entities.ghost.behaviors;

import static de.amr.games.pacman.core.board.TileContent.GhostHouse;

import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.statemachine.State;

/**
 * A state in which the given ghost targets the tile which is located the given number of tiles
 * ahead of PacMan's current move direction.
 * 
 * @author Armin Reichert
 */
public class TargetAtTileAheadOfPacMan extends State {

	public TargetAtTileAheadOfPacMan(Ghost hunter, PacMan pacMan, int numTilesAhead) {
		final Board board = hunter.getBoard();
		final int dir = pacMan.getMoveDir();
		update = state -> {
			for (int numTiles = numTilesAhead; numTiles >= 0; --numTiles) {
				Tile target = pacMan.currentTile();
				target.translate(numTiles * board.topology.dx(dir), numTiles * board.topology.dy(dir));
				if (board.isTileValid(target) && !board.contains(target, GhostHouse) && hunter.canEnter(target)) {
					hunter.followRoute(target);
					return;
				}
			}
		};
	}
}