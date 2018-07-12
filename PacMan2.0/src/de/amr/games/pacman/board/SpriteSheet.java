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

	private static final SpriteSheet SHEET = new SpriteSheet();

	public static SpriteSheet get() {
		return SHEET;
	}

	private BufferedImage sheet = Assets.readImage("sprites.png");
	private BufferedImage maze;
	private Map<Character, BufferedImage> bonusImages = new HashMap<>();
	private BufferedImage pacManStanding;
	private BufferedImage[][] pacManWalking = new BufferedImage[4][2]; // E, W, N, S

	private SpriteSheet() {
		maze = sheet.getSubimage(228, 0, 224, 248);
		
		int x = 488, y = 48;
		for (char bonus : Arrays.asList(BONUS_CHERRIES, BONUS_STRAWBERRY, BONUS_PEACH, BONUS_APPLE, BONUS_GRAPES,
				BONUS_GALAXIAN, BONUS_BELL, BONUS_KEY)) {
			bonusImages.put(bonus, sheet.getSubimage(x, y, 16, 16));
			x += 16;
		}

		pacManStanding = sheet.getSubimage(488, 0, 15, 15);
		pacManWalking[Top4.E] = new BufferedImage[] { sheet.getSubimage(456, 0, 15, 15),
				sheet.getSubimage(472, 0, 15, 15) };
		pacManWalking[Top4.W] = new BufferedImage[] { sheet.getSubimage(456, 16, 15, 15),
				sheet.getSubimage(472, 16, 15, 15) };
		pacManWalking[Top4.N] = new BufferedImage[] { sheet.getSubimage(456, 32, 15, 15),
				sheet.getSubimage(472, 32, 15, 15) };
		pacManWalking[Top4.S] = new BufferedImage[] { sheet.getSubimage(456, 48, 15, 15),
				sheet.getSubimage(472, 48, 15, 15) };
	}

	public BufferedImage getMazeImage() {
		return maze;
	}

	public BufferedImage getBonusImage(char bonus) {
		return bonusImages.get(bonus);
	}

	public BufferedImage getPacManStanding() {
		return pacManStanding;
	}

	public BufferedImage[] getPacManWalking(int direction) {
		return pacManWalking[direction];
	}
}
