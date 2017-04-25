package de.amr.games.pacman.entities.ghost.behaviors;

import static de.amr.games.pacman.PacManGame.Data;

import de.amr.easy.grid.api.Topology;
import de.amr.games.pacman.data.Tile;
import de.amr.games.pacman.data.TileContent;
import de.amr.games.pacman.entities.PacMan;
import de.amr.games.pacman.entities.ghost.Ghost;
import de.amr.games.pacman.fsm.State;

public class ProactiveChasing extends State {

	private final Ghost ghost;
	private final PacMan pacMan;
	private final int maxLookAhead;

	public ProactiveChasing(Ghost ghost, PacMan pacMan, int maxLookAhead) {
		this.ghost = ghost;
		this.pacMan = pacMan;
		this.maxLookAhead = maxLookAhead;
		update = state -> chase();
	}

	private void chase() {
		Topology top = Data.board.topology;
		Tile pacManPosition = pacMan.currentTile();
		for (int tiles = maxLookAhead; tiles >= 0; --tiles) {
			Tile target = new Tile(pacManPosition).translate(tiles * top.dx(pacMan.moveDir), tiles * top.dy(pacMan.moveDir));
			if (Data.board.isTileValid(target) && !Data.board.has(TileContent.GhostHouse, target) && ghost.canEnter(target)) {
				ghost.followRoute(target);
				return;
			}
		}
	}
}
