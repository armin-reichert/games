package de.amr.games.pacman.play;

import static de.amr.games.pacman.core.board.Board.NUM_COLS;
import static de.amr.games.pacman.core.board.Board.NUM_ROWS;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.core.app.AbstractPacManApp;

/**
 * The Pac-Man game application.
 * 
 * @see <a href="http://www.gamasutra.com/view/feature/3938/the_pacman_dossier.php">The Pac-Man
 *      Dossier</a>.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends AbstractPacManApp {

	public static void main(String... args) {
		PacManGame Game = new PacManGame();
		Game.settings.title = "Armin's Pac-Man";
		Game.settings.width = NUM_COLS * TILE_SIZE;
		Game.settings.height = NUM_ROWS * TILE_SIZE;
		Game.settings.scale = args.length > 0 ? Float.valueOf(args[0]) / Game.settings.height : 1;
		Game.settings.fullScreenOnStart = false;
		Game.settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		Game.gameLoop.log = false;
		Game.gameLoop.setTargetFrameRate(60);
		launch(Game);
	}

	@Override
	protected void init() {
		super.init();
		views.add(new PlayScene(this));
		views.show(PlayScene.class);
	}
}