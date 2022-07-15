package de.amr.schule.gameoflife;

import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;

import de.amr.easy.game.Application;
import de.amr.easy.game.config.AppSettings;
import de.amr.schule.gameoflife.scenes.DiamondScene;
import de.amr.schule.gameoflife.scenes.FiguresScene;
import de.amr.schule.gameoflife.scenes.GameOfLifeScene;
import de.amr.schule.gameoflife.scenes.RandomFillScene;

/**
 * Game of life.
 * 
 * @author Armin Reichert & Anna Schillo
 */
public class GameOfLifeApp extends Application {

	public static void main(String[] args) {
		launch(GameOfLifeApp.class, args);
	}

	private int current;
	private GameOfLifeScene[] scenes;

	@Override
	protected void configure(AppSettings settings) {
		settings.title = "Game of Life";
		settings.width = 1000;
		settings.height = 700;
	}

	@Override
	public void init() {
		clock().setTargetFrameRate(20);
		scenes = new GameOfLifeScene[] { new FiguresScene(this), new DiamondScene(this), new RandomFillScene(this) };
		setController(scenes[0]);
	}

	public void handleNavigationKeys() {
		if (keyPressedOnce(VK_RIGHT)) {
			nextScene();
		} else if (keyPressedOnce(VK_LEFT)) {
			prevScene();
		}
	}

	public void nextScene() {
		current = current == scenes.length - 1 ? 0 : current + 1;
		setController(scenes[current]);
	}

	public void prevScene() {
		current = current == 0 ? scenes.length - 1 : current - 1;
		setController(scenes[current]);
	}
}