package de.amr.games.pacman.ui;

import static de.amr.games.pacman.model.Tile.BONUS_APPLE;
import static de.amr.games.pacman.model.Tile.BONUS_BELL;
import static de.amr.games.pacman.model.Tile.BONUS_CHERRIES;
import static de.amr.games.pacman.model.Tile.BONUS_GALAXIAN;
import static de.amr.games.pacman.model.Tile.BONUS_GRAPES;
import static de.amr.games.pacman.model.Tile.BONUS_KEY;
import static de.amr.games.pacman.model.Tile.BONUS_PEACH;
import static de.amr.games.pacman.model.Tile.BONUS_STRAWBERRY;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.grid.impl.Top4;

public class Spritesheet {

	public static final int RED_GHOST = 0;
	public static final int PINK_GHOST = 1;
	public static final int BLUE_GHOST = 2;
	public static final int ORANGE_GHOST = 3;

	private static BufferedImage sheet = Assets.readImage("sprites.png");
	private static BufferedImage mazeImage;
	private static BufferedImage[] energizerImages = new BufferedImage[2];
	private static Map<Character, BufferedImage> bonusImages = new HashMap<>();
	private static BufferedImage pacManStanding;
	private static BufferedImage[][] pacManWalking = new BufferedImage[4][]; // E, W, N, S
	private static BufferedImage[] pacManDying = new BufferedImage[11];
	private static BufferedImage[] redGhostImages = new BufferedImage[8];
	private static BufferedImage[] pinkGhostImages = new BufferedImage[8];
	private static BufferedImage[] blueGhostImages = new BufferedImage[8];
	private static BufferedImage[] orangeGhostImages = new BufferedImage[8];
	private static BufferedImage[] frightenedGhostImages = new BufferedImage[4];
	private static BufferedImage[] deadGhostImages = new BufferedImage[4];

	static {
		// Maze
		mazeImage = region(228, 0, 224, 248);

		// Energizer
		energizerImages[0] = createEnergizerImage(true);
		energizerImages[1] = createEnergizerImage(false);

		// Boni
		int offset = 0;
		for (char bonus : Arrays.asList(BONUS_CHERRIES, BONUS_STRAWBERRY, BONUS_PEACH, BONUS_APPLE, BONUS_GRAPES,
				BONUS_GALAXIAN, BONUS_BELL, BONUS_KEY)) {
			bonusImages.put(bonus, sheet.getSubimage(488 + offset, 48, 16, 16));
			offset += 16;
		}

		// Pac-Man
		pacManWalking[Top4.E] = new BufferedImage[] { region(456, 0, 16, 16), region(472, 0, 16, 16) };
		pacManWalking[Top4.W] = new BufferedImage[] { region(456, 16, 16, 16), region(472, 16, 16, 16) };
		pacManWalking[Top4.N] = new BufferedImage[] { region(456, 32, 16, 16), region(472, 32, 16, 16) };
		pacManWalking[Top4.S] = new BufferedImage[] { region(456, 48, 16, 16), region(472, 48, 16, 16) };
		pacManStanding = region(488, 0, 16, 16);
		for (int i = 0; i < 11; ++i) {
			pacManDying[i] = region(504 + i * 16, 0, 16, 16);
		}

		// Ghosts
		for (int i = 0; i < 8; ++i) {
			redGhostImages[i] = region(456 + i * 16, 64, 16, 16);
		}
		for (int i = 0; i < 8; ++i) {
			pinkGhostImages[i] = region(456 + i * 16, 80, 16, 16);
		}
		for (int i = 0; i < 8; ++i) {
			blueGhostImages[i] = region(456 + i * 16, 96, 16, 16);
		}
		for (int i = 0; i < 8; ++i) {
			orangeGhostImages[i] = region(456 + i * 16, 112, 16, 16);
		}
		for (int i = 0; i < 4; ++i) {
			frightenedGhostImages[i] = region(584 + i * 16, 64, 16, 16);
		}
		for (int i = 0; i < 4; ++i) {
			deadGhostImages[i] = region(584 + i * 16, 80, 16, 16);
		}
	}

	private static BufferedImage region(int x, int y, int w, int h) {
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

	public static BufferedImage[] getEnergizerImages() {
		return energizerImages;
	}

	public static BufferedImage getMaze() {
		return mazeImage;
	}

	public static BufferedImage getBonus(char bonus) {
		return bonusImages.get(bonus);
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

	public static BufferedImage[] getNormalGhostImages(int ghostColor, int direction) {
		switch (direction) {
		case Top4.E:
			return Arrays.copyOfRange(getGhostImages(ghostColor), 0, 2);
		case Top4.W:
			return Arrays.copyOfRange(getGhostImages(ghostColor), 2, 4);
		case Top4.N:
			return Arrays.copyOfRange(getGhostImages(ghostColor), 4, 6);
		case Top4.S:
			return Arrays.copyOfRange(getGhostImages(ghostColor), 6, 8);
		}
		throw new IllegalArgumentException("Illegal direction: " + direction);
	}

	public static BufferedImage[] getGhostImages(int ghostColor) {
		switch (ghostColor) {
		case RED_GHOST:
			return redGhostImages;
		case PINK_GHOST:
			return pinkGhostImages;
		case BLUE_GHOST:
			return blueGhostImages;
		case ORANGE_GHOST:
			return orangeGhostImages;
		}
		throw new IllegalArgumentException("Illegal ghost color: " + ghostColor);
	}

	public static BufferedImage[] getFrightenedGhostImages() {
		return frightenedGhostImages;
	}

	public static BufferedImage getDeadGhostImage(int direction) {
		switch (direction) {
		case Top4.E:
			return deadGhostImages[0];
		case Top4.W:
			return deadGhostImages[1];
		case Top4.N:
			return deadGhostImages[2];
		case Top4.S:
			return deadGhostImages[3];
		}
		throw new IllegalArgumentException("Illegal direction: " + direction);
	}
}