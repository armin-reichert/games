package de.amr.games.pacman.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.BonusSymbol;

public class Spritesheet {

	public static final int RED_GHOST = 0;
	public static final int PINK_GHOST = 1;
	public static final int TURQUOISE_GHOST = 2;
	public static final int ORANGE_GHOST = 3;

	private static BufferedImage sheet = Assets.readImage("sprites.png");
	private static BufferedImage maze;
	private static BufferedImage mazeWhite;
	private static BufferedImage[] energizer = new BufferedImage[2];
	private static Map<BonusSymbol, BufferedImage> bonusMap = new HashMap<>();
	private static BufferedImage pacManStanding;
	private static BufferedImage[][] pacManWalking = new BufferedImage[4][]; // E, W, N, S
	private static BufferedImage[] pacManDying = new BufferedImage[11];
	private static BufferedImage[][] ghostNormal = new BufferedImage[4][8];
	private static BufferedImage[] ghostFrightened = new BufferedImage[4];
	private static BufferedImage[] ghostDead = new BufferedImage[4];
	private static BufferedImage[] greenNumber = new BufferedImage[4];
	private static BufferedImage[] pinkNumber = new BufferedImage[8];

	static {
		// Maze
		maze = $(228, 0, 224, 248);
		// maze = Assets.image("maze_blue.png");
		mazeWhite = Assets.image("maze_white.png");

		// Energizer
		energizer[0] = createEnergizerImage(true);
		energizer[1] = createEnergizerImage(false);

		// Boni
		int offset = 0;
		for (BonusSymbol symbol : BonusSymbol.values()) {
			bonusMap.put(symbol, sheet.getSubimage(488 + offset, 48, 16, 16));
			offset += 16;
		}

		// Pac-Man
		pacManWalking[Top4.E] = new BufferedImage[] { $(456, 0, 16, 16), $(472, 0, 16, 16),
				$(488, 0, 16, 16) };
		pacManWalking[Top4.W] = new BufferedImage[] { $(456, 16, 16, 16), $(472, 16, 16, 16),
				$(488, 0, 16, 16) };
		pacManWalking[Top4.N] = new BufferedImage[] { $(456, 32, 16, 16), $(472, 32, 16, 16),
				$(488, 0, 16, 16) };
		pacManWalking[Top4.S] = new BufferedImage[] { $(456, 48, 16, 16), $(472, 48, 16, 16),
				$(488, 0, 16, 16) };
		pacManStanding = $(488, 0, 16, 16);
		for (int i = 0; i < 11; ++i) {
			pacManDying[i] = $(504 + i * 16, 0, 16, 16);
		}

		// Ghosts
		for (int color = 0; color < 4; ++color) {
			for (int i = 0; i < 8; ++i) {
				ghostNormal[color][i] = $(456 + i * 16, 64 + color * 16, 16, 16);
			}
		}
		for (int i = 0; i < 4; ++i) {
			ghostFrightened[i] = $(584 + i * 16, 64, 16, 16);
		}
		for (int i = 0; i < 4; ++i) {
			ghostDead[i] = $(584 + i * 16, 80, 16, 16);
		}

		// Green numbers (200, 400, 800, 1600)
		for (int i = 0; i < 4; ++i) {
			greenNumber[i] = $(456 + i * 16, 128, 16, 16);
		}

		// Pink numbers (horizontal: 100, 300, 500, 700, vertikal: 1000, 2000, 3000, 5000)
		for (int i = 0; i < 4; ++i) {
			pinkNumber[i] = $(456 + i * 16, 144, 16, 16);
		}
		for (int j = 0; j < 4; ++j) {
			pinkNumber[4 + j] = $(520, 144 + j * 16, 16, 16);
		}
	}

	private static BufferedImage $(int x, int y, int w, int h) {
		return sheet.getSubimage(x, y, w, h);
	}

	private static BufferedImage createEnergizerImage(boolean visible) {
		BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		if (visible) {
			int size = 16;
			g.setColor(Color.PINK);
			g.fillOval((16 - size) / 2, (16 - size) / 2, size, size);
		}
		return img;
	}

	public static BufferedImage[] getEnergizer() {
		return energizer;
	}

	public static BufferedImage getMazeImage() {
		return maze;
	}

	public static BufferedImage getMazeImageWhite() {
		return mazeWhite;
	}

	public static BufferedImage getBonus(BonusSymbol bonus) {
		return bonusMap.get(bonus);
	}

	public static BufferedImage getPacManStanding() {
		return pacManStanding;
	}

	public static BufferedImage[] getPacManWalking(int direction) {
		return pacManWalking[direction];
	}

	public static BufferedImage[] getPacManDying() {
		return pacManDying;
	}

	public static BufferedImage[] getGhostNormal(int color, int direction) {
		switch (direction) {
		case Top4.E:
			return Arrays.copyOfRange(ghostNormal[color], 0, 2);
		case Top4.W:
			return Arrays.copyOfRange(ghostNormal[color], 2, 4);
		case Top4.N:
			return Arrays.copyOfRange(ghostNormal[color], 4, 6);
		case Top4.S:
			return Arrays.copyOfRange(ghostNormal[color], 6, 8);
		}
		throw new IllegalArgumentException("Illegal direction: " + direction);
	}

	public static BufferedImage[] getGhostFrightened() {
		return ghostFrightened;
	}

	public static BufferedImage getGhostDead(int direction) {
		switch (direction) {
		case Top4.E:
			return ghostDead[0];
		case Top4.W:
			return ghostDead[1];
		case Top4.N:
			return ghostDead[2];
		case Top4.S:
			return ghostDead[3];
		}
		throw new IllegalArgumentException("Illegal direction: " + direction);
	}

	public static BufferedImage getGreenNumber(int i) {
		return greenNumber[i];
	}

	public static BufferedImage getPinkNumber(int i) {
		return pinkNumber[i];
	}

}