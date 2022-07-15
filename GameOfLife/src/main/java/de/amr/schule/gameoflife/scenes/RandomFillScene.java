package de.amr.schule.gameoflife.scenes;

import java.util.Random;

import de.amr.schule.gameoflife.GameOfLifeApp;

/**
 * Game of life scene.
 * 
 * @author Armin Reichert & Anna Schillo
 */
public class RandomFillScene extends GameOfLifeScene {

	public RandomFillScene(GameOfLifeApp app) {
		super(app);
	}

	@Override
	protected void reset() {
		world.reset();
		Random rand = new Random();
		for (int row = 0; row < world.getGridSize(); row += 1) {
			for (int col = 0; col < world.getGridSize(); col += 1) {
				if (rand.nextBoolean()) {
					world.set(row, col);
				}
			}
		}
	}
}