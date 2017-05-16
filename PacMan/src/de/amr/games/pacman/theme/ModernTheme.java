package de.amr.games.pacman.theme;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.Board.NUM_COLS;
import static de.amr.games.pacman.core.board.Board.NUM_ROWS;
import static de.amr.games.pacman.play.PacManGame.Game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.core.board.BonusSymbol;

/**
 * A Pac-Man UI with smoother sprites.
 * 
 * @author Armin Reichert
 *
 */
public class ModernTheme extends PacManTheme {

	private final Sprite board;
	private final Map<Integer, Sprite> pacManRunning = new HashMap<>();
	private final Sprite pacManStanding;
	private final Map<String, Map<Integer, Sprite>> ghostNormal = new HashMap<>();
	private final Sprite ghostFrightened;
	private final Sprite ghostRecovering;
	private final Sprite ghostDead;
	private final Map<BonusSymbol, Sprite> bonusSymbols = new EnumMap<>(BonusSymbol.class);
	private final Sprite energizer;
	private final Sprite pill;
	private final Sprite life;
	private final Font textFont;
	private final Color hudColor;

	private Image $(BufferedImage sheet, int row, int col) {
		return sheet.getSubimage(col * 32, row * 32, 32, 32).getScaledInstance(SPRITE_SIZE, SPRITE_SIZE,
				Image.SCALE_SMOOTH);
	}

	private Image $(int row, int col) {
		BufferedImage sheet = Game.assets.image("chompersprites.png");
		return $(sheet, row, col);
	}

	public ModernTheme() {

		BufferedImage sheet = Game.assets.image("pacman_original.png");
		board = new Sprite(sheet.getSubimage(228, 0, 224, 248)).scale(NUM_COLS * TILE_SIZE, (NUM_ROWS - 5) * TILE_SIZE);

		int size = TILE_SIZE * 15 / 10;
		for (BonusSymbol symbol : BonusSymbol.values()) {
			bonusSymbols.put(symbol,
					new Sprite(sheet.getSubimage(488 + symbol.ordinal() * 16, 48, 16, 16)).scale(size, size));
		}

		List<Integer> dirs = Arrays.asList(E, S, W, N);
		List<String> ghostNames = Arrays.asList("Blinky", "Clyde", "Pinky", "Stinky", "Inky");

		pacManStanding = new Sprite($(2, 11));

		for (int dir = 0; dir < dirs.size(); ++dir) {
			Sprite sprite = new Sprite($(dir, 11), $(dir, 10));
			sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
			sprite.createAnimation(AnimationMode.CYCLIC, 100);
			pacManRunning.put(dirs.get(dir), sprite);
		}

		int ghostFrame = 333;

		for (int ghost = 0; ghost < ghostNames.size(); ghost++) {
			ghostNormal.put(ghostNames.get(ghost), new HashMap<>());
			for (int dir = 0; dir < dirs.size(); ++dir) {
				Sprite sprite = new Sprite($(dir, 2 * ghost), $(dir, 2 * ghost + 1));
				sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
				sprite.createAnimation(AnimationMode.CYCLIC, ghostFrame);
				ghostNormal.get(ghostNames.get(ghost)).put(dirs.get(dir), sprite);
			}
		}

		ghostFrightened = new Sprite($(0, 12), $(0, 13)).scale(SPRITE_SIZE, SPRITE_SIZE);
		ghostFrightened.createAnimation(AnimationMode.CYCLIC, ghostFrame);

		ghostRecovering = new Sprite($(0, 12), $(1, 12), $(0, 13), $(1, 13)).scale(SPRITE_SIZE, SPRITE_SIZE);
		ghostRecovering.createAnimation(AnimationMode.CYCLIC, ghostFrame);

		ghostDead = new Sprite($(2, 12), $(2, 13)).scale(SPRITE_SIZE, SPRITE_SIZE);
		ghostDead.createAnimation(AnimationMode.CYCLIC, ghostFrame);

		life = new Sprite($(2, 11)).scale(SPRITE_SIZE, SPRITE_SIZE);
		BufferedImage sheet2 = Game.assets.image("" + "chompermazetiles.png");

		energizer = new Sprite($(sheet2, 2, 7), $(sheet2, 2, 8)).scale(TILE_SIZE, TILE_SIZE);
		energizer.createAnimation(AnimationMode.BACK_AND_FORTH, 333);

		pill = new Sprite($(sheet2, 2, 9)).scale(TILE_SIZE, TILE_SIZE);

		// Text display
		Game.assets.storeFont("textFont", "fonts/arcadeclassic.ttf", TILE_SIZE * 1.5f, Font.PLAIN);
		textFont = Game.assets.font("textFont");
		hudColor = Color.YELLOW;
	}

	@Override
	public Sprite getBoardSprite() {
		return board;
	}

	@Override
	public Sprite getPacManStandingSprite(int dir) {
		return pacManStanding;
	}

	@Override
	public Sprite getPacManRunningSprite(int dir) {
		return pacManRunning.get(dir);
	}

	@Override
	public Sprite getPacManDyingSprite() {
		return null;
	}

	@Override
	public Sprite getGhostNormalSprite(String ghostName, int dir) {
		return ghostNormal.get(ghostName).get(dir);
	}

	@Override
	public Sprite getGhostFrightenedSprite() {
		return ghostFrightened;
	}

	@Override
	public Sprite getGhostRecoveringSprite() {
		return ghostRecovering;
	}

	@Override
	public Sprite getGhostDeadSprite(int dir) {
		return ghostDead;
	}

	@Override
	public Sprite getEnergizerSprite() {
		return energizer;
	}

	@Override
	public Sprite getPelletSprite() {
		return pill;
	}

	@Override
	public Sprite getLifeSprite() {
		return life;
	}

	@Override
	public Sprite getBonusSprite(BonusSymbol symbol) {
		return bonusSymbols.get(symbol);
	}

	@Override
	public Font getTextFont() {
		return textFont;
	}

	@Override
	public Color getHUDColor() {
		return hudColor;
	}
}
