package de.amr.games.pacman;

import static de.amr.games.pacman.data.Board.Cols;
import static de.amr.games.pacman.data.Board.Rows;
import static de.amr.games.pacman.ui.PacManUI.TileSize;

import java.util.Arrays;
import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.data.PacManGameData;
import de.amr.games.pacman.scenes.PlayScene;
import de.amr.games.pacman.ui.ClassicUI;
import de.amr.games.pacman.ui.ModernUI;

public class PacManGame extends Application {

	public static final PacManGameData Data = new PacManGameData();

	public static void main(String... args) {
		Settings.title = "Zwick-Man";
		Settings.width = Cols * TileSize;
		Settings.height = Rows * TileSize;
		Settings.scale = args.length > 0 ? Float.valueOf(args[0]) / Settings.height : 1f;
		Settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		Settings.set("themes", Arrays.asList(new ClassicUI(), new ModernUI()));
		Settings.set("drawInternals", false);
		Settings.set("drawGrid", false);
		Log.setLevel(Level.ALL);
		launch(new PacManGame());
	}

	@Override
	protected void init() {
		Views.add(new PlayScene(this));
		Views.show(PlayScene.class);
	}
}