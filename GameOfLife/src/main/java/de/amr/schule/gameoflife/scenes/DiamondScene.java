package de.amr.schule.gameoflife.scenes;

import de.amr.schule.gameoflife.GameOfLifeApp;

/**
 * Game of life scene.
 * 
 * @author Armin Reichert & Anna Schillo
 */
public class DiamondScene extends GameOfLifeScene {

	public DiamondScene(GameOfLifeApp app) {
		super(app);
	}

	@Override
	protected void reset() {
		world.reset();
		int a = world.getGridSize() / 4;
		int h = (int) (a * Math.sqrt(2) / 2);
		int topX = world.getGridSize() / 2;
		int topY = world.getGridSize() / 4 - h / 2;
		int xl = topX, xr = topX, y = topY;
		world.set(topY, topX);
		for (int i = 0; i < a; ++i) {
			world.set(y, xl);
			world.set(y, xr);
			xl--;
			xr++;
			y++;
		}
		xl++;
		xr--;
		for (int i = 0; i < a; ++i) {
			world.set(y, xl);
			world.set(y, xr);
			xr--;
			xl++;
			y++;
		}
	}
}