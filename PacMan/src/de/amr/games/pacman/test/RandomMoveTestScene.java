package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.misc.SceneHelper.drawSprite;
import static de.amr.games.pacman.play.PlayScene.GHOST_HOUSE_ENTRY;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import java.awt.Graphics2D;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.scene.Scene;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.entities.ghost.Ghost;

/**
 * 
 * @author Armin Reichert
 */
public class RandomMoveTestScene extends Scene<RandomMoveTestApp> {

	private Random rand = new Random();
	private Board board;
	private Ghost[] ghosts;

	public RandomMoveTestScene(RandomMoveTestApp app) {
		super(app);
	}

	@Override
	public void init() {
		board = new Board(app.assets.text("board.txt").split("\n"));
		ghosts = new Ghost[5 + rand.nextInt(15)];
		for (int i = 0; i < ghosts.length; ++i) {
			Ghost ghost = new Ghost(app, board, "Pinky", GHOST_HOUSE_ENTRY);
			ghost.control.state(Scattering).update = state -> ghost.moveRandomly();
			ghost.setAnimated(true);
			ghost.setSpeed(8 * TILE_SIZE / app.settings.fps * (0.5f + rand.nextFloat()));
			ghost.control.changeTo(Scattering);
			ghosts[i] = ghost;
		}
	};

	@Override
	public void update() {
		Stream.of(ghosts).forEach(Ghost::update);
	}

	@Override
	public void draw(Graphics2D g) {
		drawSprite(g, 3, 0, app.getTheme().getBoardSprite());
		Stream.of(ghosts).forEach(ghost -> ghost.draw(g));
	}
}