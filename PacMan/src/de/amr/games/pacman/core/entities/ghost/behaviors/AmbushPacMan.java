package de.amr.games.pacman.core.entities.ghost.behaviors;

import static de.amr.easy.grid.impl.Top4.Top4;
import static de.amr.games.pacman.core.board.TileContent.GhostHouse;

import de.amr.easy.statemachine.State;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.Tile;
import de.amr.games.pacman.core.entities.BoardMover;
import de.amr.games.pacman.play.PlayScene;

/**
 * A chasing state targeting the tile which is <code>ahead</code> tiles ahead of PacMan's current
 * location.
 * 
 * @author Armin Reichert
 */
public class AmbushPacMan extends State {

	public AmbushPacMan(BoardMover chaser, BoardMover hunted, int ahead) {
		update = state -> {
			Board board = chaser.getBoard();
			// leave ghost house first before chasing:
			if (board.contains(chaser.currentTile(), GhostHouse)) {
				chaser.follow(PlayScene.GHOST_HOUSE_ENTRY);
				return;
			}
			// find valid tile ahead of Pac-Man's current position
			for (int n = ahead; n > 0; --n) {
				Tile tile = hunted.currentTile().translate(n * Top4.dy(hunted.getMoveDir()), n * Top4.dx(hunted.getMoveDir()));
				if (board.isBoardTile(tile) && chaser.canEnterTile.apply(tile)) {
					chaser.follow(tile);
					return;
				}
			}
			chaser.follow(hunted.currentTile());
		};
	}
}
