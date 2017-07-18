package de.amr.games.pacman.play;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.ModernTheme;
import de.amr.games.pacman.theme.ThemeManager;

/**
 * The Pac-Man game application.
 * 
 * @see <a href="http://www.gamasutra.com/view/feature/3938/the_pacman_dossier.php">The Pac-Man
 *      Dossier</a>.
 * 
 * @author Armin Reichert
 */
public class PacManGame extends Application {

	private ThemeManager themeManager;

	public static void main(String... args) {
		PacManGame game = new PacManGame();
		game.themeManager = new ThemeManager(new ClassicTheme(game.assets), new ModernTheme(game.assets));
		game.settings.title = "Armin's Pac-Man";
		game.settings.width = 448;
		game.settings.height = 576;
		game.settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		game.settings.set("drawInternals", false);
		launch(game);
	}

	@Override
	public void init() {
		views.add(new PlayScene(this));
		views.select(PlayScene.class);
	}

	public ThemeManager getThemeManager() {
		return themeManager;
	}
}