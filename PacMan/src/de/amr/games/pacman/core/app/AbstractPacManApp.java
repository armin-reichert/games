package de.amr.games.pacman.core.app;

import static java.util.Arrays.asList;

import java.util.List;

import de.amr.easy.game.Application;
import de.amr.games.pacman.theme.ClassicTheme;
import de.amr.games.pacman.theme.ModernTheme;
import de.amr.games.pacman.theme.PacManTheme;

/**
 * Base class for Pac-Man applications.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractPacManApp extends Application {

	private List<PacManTheme> themes;
	private int themeIndex;

	@Override
	protected void init() {
		themes = asList(new ClassicTheme(), new ModernTheme());
	}

	public PacManTheme selectedTheme() {
		return themes.get(themeIndex);
	}

	public void selectNextTheme() {
		if (++themeIndex == themes.size()) {
			themeIndex = 0;
		}
		selectedTheme().getEnergizerSprite().setAnimated(false);
	}
}