package de.amr.games.pacman;

import static de.amr.games.pacman.data.Board.NUM_COLS;
import static de.amr.games.pacman.data.Board.NUM_ROWS;
import static de.amr.games.pacman.ui.PacManUI.TILE_SIZE;

import java.util.Arrays;
import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.data.PacManGameData;
import de.amr.games.pacman.scenes.PlayScene;
import de.amr.games.pacman.ui.ClassicUI;
import de.amr.games.pacman.ui.ModernUI;

/**
 * The Pac-Man game.
 * 
 * Represents the entry point into the game.
 * 
 * @author Armin Reichert
 *
 */
public class PacManGame extends Application {

	/**
	 * The game data.
	 */
	public static final PacManGameData Data = new PacManGameData();

	public static void main(String... args) {
		Settings.title = "Armin's Pac-Man";
		Settings.width = NUM_COLS * TILE_SIZE;
		Settings.height = NUM_ROWS * TILE_SIZE;
		Settings.scale = args.length > 0 ? Float.valueOf(args[0]) / Settings.height : 1f;
		Settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
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