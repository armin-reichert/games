package de.amr.games.pong;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pong.model.Game;
import de.amr.games.pong.ui.menu.MenuScreen;
import de.amr.games.pong.ui.play.PlayScreen;

/**
 * The classic "Pong" game with different play modes.
 * 
 * @author Armin Reichert & Anna Schillo
 */
public class PongGameApp extends Application {

	public static void main(String[] args) {
		launch(new PongGameApp());
	}

	public Game game;
	public MenuScreen menuScreen;
	public PlayScreen playScreen;

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
		menuScreen = new MenuScreen(this);
		playScreen = new PlayScreen(this);
		setController(menuScreen);
	}

	private void loadSounds() {
		Assets.sound("plop.mp3");
		Assets.sound("plip.mp3");
		Assets.sound("out.mp3");
	}
}