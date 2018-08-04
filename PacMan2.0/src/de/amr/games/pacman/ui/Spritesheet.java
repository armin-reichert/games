package de.amr.games.pacman.ui;

import static de.amr.easy.game.sprite.AnimationType.BACK_AND_FORTH;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.sprite.AnimationType;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.BonusSymbol;

public class Spritesheet {

	/** Tile size. */
	public static final int TS = 16;

	public static final int RED_GHOST = 0;
	public static final int PINK_GHOST = 1;
	public static final int TURQUOISE_GHOST = 2;
	public static final int ORANGE_GHOST = 3;

	private static BufferedImage sheet = Assets.readImage("sprites.png");
	private static BufferedImage maze;
	private static BufferedImage mazeWhite;
	private static Map<BonusSymbol, BufferedImage> symbolMap = new HashMap<>();
	private static BufferedImage pacManStanding;
	private static BufferedImage[][] pacManWalking = new BufferedImage[4][]; // E, W, N, S
	private static BufferedImage[] pacManDying = new BufferedImage[11];
	private static BufferedImage[][] ghostNormal = new BufferedImage[4][8];
	private static BufferedImage[] ghostAwed = new BufferedImage[2];
	private static BufferedImage[] ghostBlinking = new BufferedImage[4];
	private static BufferedImage[] ghostEyes = new BufferedImage[4];
	private static BufferedImage[] greenNumber = new BufferedImage[4];
	private static BufferedImage[] pinkNumber = new BufferedImage[8];

	static { // Extract frames

		// Maze
		maze = $(228, 0, 224, 248);
		mazeWhite = Assets.image("maze_white.png");

		// Symbols for boni
		int offset = 0;
		for (BonusSymbol symbol : BonusSymbol.values()) {
			symbolMap.put(symbol, $(488 + offset, 48));
			offset += TS;
		}

		// Pac-Man
		pacManWalking[Top4.E] = new BufferedImage[] { $(456, 0), $(472, 0), $(488, 0) };
		pacManWalking[Top4.W] = new BufferedImage[] { $(456, TS), $(472, TS), $(488, 0) };
		pacManWalking[Top4.N] = new BufferedImage[] { $(456, 2 * TS), $(472, 2 * TS), $(488, 0) };
		pacManWalking[Top4.S] = new BufferedImage[] { $(456, 3 * TS), $(472, 3 * TS), $(488, 0) };
		pacManStanding = $(488, 0);
		for (int i = 0; i < 11; ++i) {
			pacManDying[i] = $(504 + i * TS, 0);
		}

		// Ghosts
		for (int color = 0; color < 4; ++color) {
			for (int i = 0; i < 8; ++i) {
				ghostNormal[color][i] = $(456 + i * TS, 64 + color * TS);
			}
		}
		for (int i = 0; i < 2; ++i) {
			ghostAwed[i] = $(584 + i * TS, 64);
		}
		for (int i = 0; i < 4; ++i) {
			ghostBlinking[i] = $(584 + i * TS, 64);
		}
		for (int i = 0; i < 4; ++i) {
			ghostEyes[i] = $(584 + i * TS, 80);
		}

		// Green numbers (200, 400, 800, 1600)
		for (int i = 0; i < 4; ++i) {
			greenNumber[i] = $(456 + i * TS, 128);
		}

		// Pink numbers (horizontal: 100, 300, 500, 700, vertikal: 1000, 2000, 3000, 5000)
		for (int i = 0; i < 4; ++i) {
			pinkNumber[i] = $(456 + i * TS, 144);
		}
		for (int j = 0; j < 4; ++j) {
			pinkNumber[4 + j] = $(520, 144 + j * TS);
		}
	}

	private static BufferedImage $(int x, int y, int w, int h) {
		return sheet.getSubimage(x, y, w, h);
	}

	private static BufferedImage $(int x, int y) {
		return $(x, y, TS, TS);
	}

	private static BufferedImage energizerImage(boolean visible) {
		BufferedImage img = new BufferedImage(TS, TS, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(Color.BLACK);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		if (visible) {
			g.setColor(Color.PINK);
			g.fillOval(0, 0, TS, TS);
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		return img;
	}

	public static Sprite energizer() {
		return new Sprite(energizerImage(false), energizerImage(true)).animate(BACK_AND_FORTH, 250);
	}

	public static Sprite maze() {
		return new Sprite(maze);
	}

	public static Sprite flashingMaze() {
		return new Sprite(maze, mazeWhite).animate(AnimationType.CYCLIC, 100);
	}

	public static Sprite symbol(BonusSymbol symbol) {
		return new Sprite(symbolMap.get(symbol));
	}

	public static Sprite pacManStanding() {
		return new Sprite(pacManStanding);
	}

	public static Sprite pacManWalking(int dir) {
		return new Sprite(pacManWalking[dir]).animate(AnimationType.BACK_AND_FORTH, 80);
	}

	public static Sprite pacManDying() {
		return new Sprite(pacManDying).animate(AnimationType.LINEAR, 100);
	}

	public static Sprite ghostColored(int color, int direction) {
		BufferedImage[] frames;
		switch (direction) {
		case Top4.E:
			frames = Arrays.copyOfRange(ghostNormal[color], 0, 2);
			break;
		case Top4.W:
			frames = Arrays.copyOfRange(ghostNormal[color], 2, 4);
			break;
		case Top4.N:
			frames = Arrays.copyOfRange(ghostNormal[color], 4, 6);
			break;
		case Top4.S:
			frames = Arrays.copyOfRange(ghostNormal[color], 6, 8);
			break;
		default:
			throw new IllegalArgumentException("Illegal direction: " + direction);
		}
		return new Sprite(frames).animate(AnimationType.BACK_AND_FORTH, 300);
	}

	public static Sprite ghostAwed() {
		return new Sprite(ghostAwed).animate(AnimationType.CYCLIC, 200);
	}

	public static Sprite ghostBlinking() {
		return new Sprite(ghostBlinking).animate(AnimationType.CYCLIC, 100);
	}

	public static Sprite ghostEyes(int dir) {
		switch (dir) {
		case Top4.E:
			return new Sprite(ghostEyes[0]);
		case Top4.W:
			return new Sprite(ghostEyes[1]);
		case Top4.N:
			return new Sprite(ghostEyes[2]);
		case Top4.S:
			return new Sprite(ghostEyes[3]);
		}
		throw new IllegalArgumentException("Illegal direction: " + dir);
	}

	public static Sprite greenNumber(int i) {
		return new Sprite(greenNumber[i]);
	}

	public static Sprite pinkNumber(int i) {
		return new Sprite(pinkNumber[i]);
	}
}