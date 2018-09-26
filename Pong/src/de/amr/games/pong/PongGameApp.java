package de.amr.games.pong;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pong.model.Game;
import de.amr.games.pong.model.Game.PlayMode;
import de.amr.games.pong.ui.ScreenManager;
import de.amr.games.pong.ui.menu.MenuScreen;
import de.amr.games.pong.ui.play.PlayScreen;

/**
 * The classic "Pong" game with different play modes.
 * 
 * @author Armin Reichert & Anna Schillo
 */
public class PongGameApp extends Application implements ScreenManager {

	public static void main(String[] args) {
		launch(new PongGameApp(), args);
	}

	private Game game;
	private MenuScreen menuScreen;
	private PlayScreen playScreen;

	public PongGameApp() {
		settings.title = "Pong";
		settings.width = 640;
		settings.height = 480;
		settings.fullScreenMode = new DisplayMode(640, 480, 32, DisplayMode.REFRESH_RATE_UNKNOWN);
	}

	@Override
	public void init() {
		game = new Game();
		loadSounds();
		selectMenuScreen();
	}

	@Override
	public void selectMenuScreen() {
		if (menuScreen == null) {
			Dimension size = new Dimension(settings.width, settings.height);
			menuScreen = new MenuScreen(this, size);
			menuScreen.setBgColor(Color.BLACK);
			menuScreen.setBgColorSelected(Color.GRAY);
			menuScreen.setHilightColor(Color.YELLOW);
			menuScreen.init();
		}
		setController(menuScreen, false);
	}

	@Override
	public void selectPlayScreen(PlayMode playMode) {
		game.playMode = playMode;
		if (playScreen == null) {
			Dimension size = new Dimension(settings.width, settings.height);
			playScreen = new PlayScreen(this, game, size);
		}
		setController(playScreen);
	}

	private void loadSounds() {
		Assets.sound("plop.mp3");
		Assets.sound("plip.mp3");
		Assets.sound("out.mp3");
	}
}