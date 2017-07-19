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
		launch(new PacManGame());
	}

	public PacManGame() {
		themeManager = new ThemeManager(new ClassicTheme(assets), new ModernTheme(assets));
		settings.title = "Armin's Pac-Man";
		settings.width = 448;
		settings.height = 576;
		settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		settings.set("drawInternals", false);
	}

	@Override
	public void init() {
		addView(new PlayScene(this));
		selectView(PlayScene.class);
	}

	public ThemeManager getThemeManager() {
		return themeManager;
	}
}