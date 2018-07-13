package de.amr.games.pacman.board;

import static de.amr.games.pacman.board.Tile.BONUS_APPLE;
import static de.amr.games.pacman.board.Tile.BONUS_BELL;
import static de.amr.games.pacman.board.Tile.BONUS_CHERRIES;
import static de.amr.games.pacman.board.Tile.BONUS_GALAXIAN;
import static de.amr.games.pacman.board.Tile.BONUS_GRAPES;
import static de.amr.games.pacman.board.Tile.BONUS_KEY;
import static de.amr.games.pacman.board.Tile.BONUS_PEACH;
import static de.amr.games.pacman.board.Tile.BONUS_STRAWBERRY;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.grid.impl.Top4;

public class SpriteSheet {

	public static final int RED = 0;
	public static final int PINK = 1;
	public static final int BLUE = 2;
	public static final int ORANGE = 3;

	private static BufferedImage sheet = Assets.readImage("sprites.png");
	private static BufferedImage maze;
	private static Map<Character, BufferedImage> bonusImages = new HashMap<>();
	private static BufferedImage pacManStanding;
	private static BufferedImage[][] pacManWalking = new BufferedImage[4][]; // E, W, N, S
	private static BufferedImage[] redGhostImages = new BufferedImage[8];
	private static BufferedImage[] pinkGhostImages = new BufferedImage[8];
	private static BufferedImage[] blueGhostImages = new BufferedImage[8];
	private static BufferedImage[] orangeGhostImages = new BufferedImage[8];
	private static BufferedImage[] frightenedGhostImages = new BufferedImage[4];
	private static BufferedImage[] deadGhostImages = new BufferedImage[4];

	static {
		// Maze:
		maze = sheet.getSubimage(228, 0, 224, 248);
		// Bonus:
		int x = 488, y = 48;
		for (char bonus : Arrays.asList(BONUS_CHERRIES, BONUS_STRAWBERRY, BONUS_PEACH, BONUS_APPLE, BONUS_GRAPES,
				BONUS_GALAXIAN, BONUS_BELL, BONUS_KEY)) {
			bonusImages.put(bonus, sheet.getSubimage(x, y, 16, 16));
			x += 16;
		}

		// PacMan:
		pacManStanding = sheet.getSubimage(488, 0, 15, 15);
		pacManWalking[Top4.E] = new BufferedImage[] { sheet.getSubimage(456, 0, 15, 15),
				sheet.getSubimage(472, 0, 15, 15) };
		pacManWalking[Top4.W] = new BufferedImage[] { sheet.getSubimage(456, 16, 15, 15),
				sheet.getSubimage(472, 16, 15, 15) };
		pacManWalking[Top4.N] = new BufferedImage[] { sheet.getSubimage(456, 32, 15, 15),
				sheet.getSubimage(472, 32, 15, 15) };
		pacManWalking[Top4.S] = new BufferedImage[] { sheet.getSubimage(456, 48, 15, 15),
				sheet.getSubimage(472, 48, 15, 15) };

		// Ghosts:
		BufferedImage ghostImages = sheet.getSubimage(456, 64, 128, 64);
		for (int i = 0; i < 8; ++i) {
			redGhostImages[i] = ghostImages.getSubimage(i * 16, 0, 16, 16);
		}
		for (int i = 0; i < 8; ++i) {
			pinkGhostImages[i] = ghostImages.getSubimage(i * 16, 16, 16, 16);
		}
		for (int i = 0; i < 8; ++i) {
			blueGhostImages[i] = ghostImages.getSubimage(i * 16, 32, 16, 16);
		}
		for (int i = 0; i < 8; ++i) {
			orangeGhostImages[i] = ghostImages.getSubimage(i * 16, 48, 16, 16);
		}
		for (int i = 0; i < 4; ++i) {
			frightenedGhostImages[i] = sheet.getSubimage(584 + i * 16, 64, 16, 16);
		}
		for (int i = 0; i < 4; ++i) {
			deadGhostImages[i] = sheet.getSubimage(584 + i * 16, 80, 16, 16);
		}
	}

	public static BufferedImage getMazeImage() {
		return maze;
	}

	public static BufferedImage getBonusImage(char bonus) {
		return bonusImages.get(bonus);
	}

	public static BufferedImage getPacManStanding() {
		return pacManStanding;
	}

	public static BufferedImage[] getPacManWalking(int direction) {
		return pacManWalking[direction];
	}

	public static BufferedImage[] getGhostImagesByDirection(int ghostColor, int direction) {
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
		case RED:
			return redGhostImages;
		case PINK:
			return pinkGhostImages;
		case BLUE:
			return blueGhostImages;
		case ORANGE:
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
