package de.amr.games.pong;

import java.awt.Dimension;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.FullScreen;
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
		launch(new PongGameApp());
	}

	private Game game;
	private MenuScreen menuScreen;
	private PlayScreen playScreen;

	public PongGameApp() {
		settings.title = "Pong";
		settings.width = 640;
		settings.height = 480;
		settings.fullScreenMode = FullScreen.Mode(640, 480, 32);
	}

	@Override
	public void init() {
		game = new Game();
		loadSounds();
		Dimension size = new Dimension(settings.width, settings.height);
		menuScreen = new MenuScreen(this, size);
		playScreen = new PlayScreen(this, game, size);
		setController(menuScreen);
	}

	@Override
	public void selectMenuScreen() {
		setController(menuScreen);
	}

	@Override
	public void selectPlayScreen(PlayMode playMode) {
		game.playMode = playMode;
		setController(playScreen);
	}

	private void loadSounds() {
		Assets.sound("plop.mp3");
		Assets.sound("plip.mp3");
		Assets.sound("out.mp3");
	}
}