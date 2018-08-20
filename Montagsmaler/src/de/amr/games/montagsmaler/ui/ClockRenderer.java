package de.amr.games.montagsmaler.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import de.amr.games.montagsmaler.Tools;

public class ClockRenderer {

	private static final int TICK_COUNT = 60;
	private static final int TICK_SIZE = 30;
	private static final int BULLET_SIZE = 10;
	private static final Image[] TICK_IMAGE = new Image[8];
	private static final int[] TICK_IMAGE_INDEX = new int[] { 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
			2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 0, 0, 0,
			0, 0 };

	static {
		int i = 0;
		for (String d : Arrays.asList("east", "south_east", "south", "south_west", "west", "north_west", "north",
				"north_east")) {
			TICK_IMAGE[i++] = Tools.loadImageIcon("images/pacman_" + d + ".jpg").getImage().getScaledInstance(TICK_SIZE,
					TICK_SIZE, Image.SCALE_SMOOTH);
		}
	}

	private static Image getTickImage(int t) {
		return TICK_IMAGE[TICK_IMAGE_INDEX[t]];
	}

	private final BufferedImage buffer;
	private final Graphics2D gfx;

	ClockRenderer(BufferedImage buffer) {
		this.buffer = buffer;
		gfx = buffer.createGraphics();
		gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	void drawClockTicks(int elapsed) {
		for (int i = elapsed - 1; i < TICK_COUNT; ++i) {
			if (i < 0) {
				continue;
			}
			final int x = computeTickX(i, buffer.getWidth() - TICK_SIZE, buffer.getHeight() - TICK_SIZE);
			final int y = computeTickY(i, buffer.getWidth() - TICK_SIZE, buffer.getHeight() - TICK_SIZE);
			switch (i - elapsed) {
			case -1:
				gfx.setColor(Color.BLACK);
				gfx.fillRect(x, y, TICK_SIZE, TICK_SIZE);
				break;
			case 0:
				gfx.drawImage(getTickImage(i), x, y, Color.BLACK, null);
				break;
			default:
				gfx.setColor(i % 5 == 0 ? Color.RED : Color.YELLOW);
				gfx.fillOval(x + BULLET_SIZE / 2, y + BULLET_SIZE / 2, BULLET_SIZE, BULLET_SIZE);
				break;
			}
		}
	}

	private int computeTickX(int t, int w, int h) {
		t = t % TICK_COUNT;
		if (t <= TICK_COUNT / 2) {
			return w / 2 + dx(t, w, h);
		} else {
			return w / 2 - dx(t - TICK_COUNT / 2, w, h);
		}
	}

	private int dx(int t, int w, int h) {
		if (t <= 9) {
			return t * (w / 18);
		}
		if (t <= 20) {
			return w / 2;
		}
		if (t <= 30) {
			return (30 - t) * (w / 18);
		}
		return 0;
	}

	private int computeTickY(int t, int w, int h) {
		t = t % TICK_COUNT;
		if (t <= TICK_COUNT / 2) {
			return dy(t, w, h);
		} else {
			return h - dy(t - TICK_COUNT / 2, w, h);
		}
	}

	private int dy(int t, int w, int h) {
		if (t <= 5) {
			return 0;
		}
		if (t <= 25) {
			return (t - 5) * (h / 20);
		}
		return h;
	}

}
