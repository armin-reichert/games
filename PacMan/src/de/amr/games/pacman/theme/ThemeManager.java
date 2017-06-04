package de.amr.games.pacman.theme;

import java.util.Arrays;
import java.util.List;

public class ThemeManager {
	private final List<PacManTheme> themes;
	private int themeIndex;

	public ThemeManager(PacManTheme... themes) {
		this.themes = Arrays.asList(themes);
	}

	public void selectTheme(Class<? extends PacManTheme> themeClass) {
		if (themeClass == ModernTheme.class) {
			themeIndex = 1;
		} else if (themeClass == ClassicTheme.class) {
			themeIndex = 0;
		} else
			throw new IllegalArgumentException("Unknown theme class: " + themeClass.getName());
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