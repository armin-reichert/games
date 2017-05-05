package de.amr.games.pacman.entities.ghost.behaviors;

import static de.amr.games.pacman.PacManGame.Game;

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
		for (int tiles = maxLookAhead; tiles >= 0; --tiles) {
			Tile target = pacMan.currentTile();
			target.translate(tiles * Game.board.topology.dx(pacMan.moveDir), tiles * Game.board.topology.dy(pacMan.moveDir));
			if (Game.board.isTileValid(target) && !Game.board.contains(target, TileContent.GhostHouse)
					&& ghost.canEnter(target)) {
				ghost.followRoute(target);
				return;
			}
		}
	}
}
