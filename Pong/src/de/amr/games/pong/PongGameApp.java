package de.amr.games.pong;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pong.model.Game;
import de.amr.games.pong.model.Game.PlayMode;
import de.amr.games.pong.view.menu.MenuScreen;
import de.amr.games.pong.view.play.PlayScreen;

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
	public MenuScreen menuViewController;
	public PlayScreen playViewController;

	public PongGameApp() {
		settings.title = "Pong";
		settings.width = 640;
		settings.height = 480;
		settings.fullScreenMode = FullScreen.Mode(640, 480, 32);
	}

	@Override
	public void init() {
		game = new Game();
		game.scoreLeft = 11;
		game.scoreRight = 11;
		game.playMode = PlayMode.Player1_Player2;
		loadSounds();
		menuViewController = new MenuScreen(this);
		playViewController = new PlayScreen(this);
		setController(menuViewController);
	}

	private void loadSounds() {
		Assets.sound("plop.mp3");
		Assets.sound("plip.mp3");
		Assets.sound("out.mp3");
	}
}