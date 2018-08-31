package de.amr.games.pong.ui;

import de.amr.games.pong.model.Game.PlayMode;

public interface ScreenManager {

	void selectMenuScreen();

	void selectPlayScreen(PlayMode playMode);

}
