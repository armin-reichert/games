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
		themes = asList(new ClassicTheme(assets), new ModernTheme(assets));
	}
	
	public void setTheme(Class<? extends PacManTheme> themeClass) {
		if (themeClass == ModernTheme.class) {
			themeIndex = 1;
		} else if (themeClass == ClassicTheme.class) {
			themeIndex = 0;
		} else throw new IllegalArgumentException("Unknown theme class: " + themeClass.getName());
		
	}

	public PacManTheme getTheme() {
		return themes.get(themeIndex);
	}

	public void selectNextTheme() {
		if (++themeIndex == themes.size()) {
			themeIndex = 0;
		}
		getTheme().getEnergizerSprite().setAnimated(false);
	}
}