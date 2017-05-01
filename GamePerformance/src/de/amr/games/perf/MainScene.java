package de.amr.games.perf;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.scene.Scene;

public class MainScene extends Scene<GamePerformanceApp> {

	private int sampleIndex;
	private int[] fpsValues;
	private int stepX = 20;

	public MainScene(GamePerformanceApp app) {
		super(app);
	}

	@Override
	public void init() {
		fpsValues = new int[getWidth()];
		getApp().gameLoop.addFPSListener(e -> {
			if ("fps".equals(e.getPropertyName())) {
				fpsValues[sampleIndex++] = (Integer) e.getNewValue();
				if (sampleIndex * stepX >= getWidth()) {
					sampleIndex = 0;
				}
			}
		});
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D g) {
		int xOffset = 50;
		int yScale = -1;
		g.setColor(Color.LIGHT_GRAY);
		for (int y = 0; y < getHeight(); y += 20) {
			g.drawLine(xOffset, getHeight() - y, getWidth(), getHeight() - y);
			g.drawString(String.valueOf(y), 0, getHeight() - y);
		}
		g.translate(0, getHeight());
		g.scale(1, yScale);
		g.setColor(Color.GREEN);
		for (int j = 0; j < sampleIndex - 1; ++j) {
			int x1 = xOffset + stepX * j;
			int y1 = fpsValues[j];
			int x2 = xOffset + stepX * (j + 1);
			int y2 = fpsValues[j + 1];
			g.drawLine(x1, y1, x2, y2);
			x1 = x2;
			y1 = y2;
		}
		g.scale(1, yScale);
		g.translate(0, getHeight());
	}
}
