package de.amr.games.pacman.play;

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
		PacManGame game = new PacManGame();
		game.settings.title = "Armin's Pac-Man";
		game.settings.width = 448;
		game.settings.height = 576;
		game.settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		game.settings.set("drawInternals", false);
		launch(game);
	}

	@Override
	protected void init() {
		super.init();
		views.add(new PlayScene(this));
		views.show(PlayScene.class);
	}
}