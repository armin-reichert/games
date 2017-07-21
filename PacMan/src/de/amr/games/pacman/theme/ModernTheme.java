package de.amr.games.pacman.theme;

import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.core.board.BonusSymbol;

/**
 * A Pac-Man theme with smoother sprites.
 * 
 * @author Armin Reichert
 */
public class ModernTheme extends PacManTheme {

	private final BufferedImage originalSheet;
	private final BufferedImage modernSpriteSheet;
	private final BufferedImage modernTileSheet;
	private final Sprite boardSprite;
	private final Map<Integer, Sprite> pacManRunningSprites = new HashMap<>();
	private final Sprite pacManStandingSprite;
	private final Map<String, Map<Integer, Sprite>> ghostNormalSprites = new HashMap<>();
	private final Sprite ghostFrightenedSprite;
	private final Sprite ghostRecoveringSprite;
	private final Sprite ghostDeadSprite;
	private final Map<BonusSymbol, Sprite> bonusSymbolSprites = new EnumMap<>(BonusSymbol.class);
	private final Sprite energizerSprite;
	private final Sprite pillSprite;
	private final Sprite lifeSprite;
	private final Font textFont;
	private final Color hudColor;

	private Image tile(BufferedImage sheet, int row, int col) {
		return sheet.getSubimage(col * 32, row * 32, 32, 32).getScaledInstance(SPRITE_SIZE, SPRITE_SIZE,
				Image.SCALE_SMOOTH);
	}

	private Image tile(int row, int col) {
		return tile(modernSpriteSheet, row, col);
	}

	public ModernTheme() {

		originalSheet = Assets.image("pacman_original.png");
		modernSpriteSheet = Assets.image("chompersprites.png");
		modernTileSheet = Assets.image("chompermazetiles.png");

		boardSprite = new Sprite(originalSheet.getSubimage(228, 0, 224, 248)).scale(28 * TILE_SIZE, (36 - 5) * TILE_SIZE);

		int size = TILE_SIZE * 15 / 10;
		for (BonusSymbol symbol : BonusSymbol.values()) {
			bonusSymbolSprites.put(symbol,
					new Sprite(originalSheet.getSubimage(488 + symbol.ordinal() * 16, 48, 16, 16)).scale(size, size));
		}

		List<Integer> dirs = Arrays.asList(E, S, W, N);
		List<String> ghostNames = Arrays.asList("Blinky", "Clyde", "Pinky", "Stinky", "Inky");

		pacManStandingSprite = new Sprite(tile(2, 11));

		for (int dir = 0; dir < dirs.size(); ++dir) {
			Sprite sprite = new Sprite(tile(dir, 11), tile(dir, 10));
			sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
			sprite.makeAnimated(AnimationMode.CYCLIC, 100);
			pacManRunningSprites.put(dirs.get(dir), sprite);
		}

		int ghostFrame = 333;

		for (int ghost = 0; ghost < ghostNames.size(); ghost++) {
			ghostNormalSprites.put(ghostNames.get(ghost), new HashMap<>());
			for (int dir = 0; dir < dirs.size(); ++dir) {
				Sprite sprite = new Sprite(tile(dir, 2 * ghost), tile(dir, 2 * ghost + 1));
				sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
				sprite.makeAnimated(AnimationMode.CYCLIC, ghostFrame);
				ghostNormalSprites.get(ghostNames.get(ghost)).put(dirs.get(dir), sprite);
			}
		}

		ghostFrightenedSprite = new Sprite(tile(0, 12), tile(0, 13)).scale(SPRITE_SIZE, SPRITE_SIZE);
		ghostFrightenedSprite.makeAnimated(AnimationMode.CYCLIC, ghostFrame);

		ghostRecoveringSprite = new Sprite(tile(0, 12), tile(1, 12), tile(0, 13), tile(1, 13)).scale(SPRITE_SIZE,
				SPRITE_SIZE);
		ghostRecoveringSprite.makeAnimated(AnimationMode.CYCLIC, ghostFrame);

		ghostDeadSprite = new Sprite(tile(2, 12), tile(2, 13)).scale(SPRITE_SIZE, SPRITE_SIZE);
		ghostDeadSprite.makeAnimated(AnimationMode.CYCLIC, ghostFrame);

		lifeSprite = new Sprite(tile(2, 11)).scale(SPRITE_SIZE, SPRITE_SIZE);

		energizerSprite = new Sprite(tile(modernTileSheet, 2, 7), tile(modernTileSheet, 2, 8)).scale(TILE_SIZE, TILE_SIZE);
		energizerSprite.makeAnimated(AnimationMode.BACK_AND_FORTH, 333);

		pillSprite = new Sprite(tile(modernTileSheet, 2, 9)).scale(TILE_SIZE, TILE_SIZE);

		// Text display
		Assets.storeFont("textFont", "fonts/arcadeclassic.ttf", TILE_SIZE * 1.5f, Font.PLAIN);
		textFont = Assets.font("textFont");
		hudColor = Color.YELLOW;
	}

	@Override
	public Sprite getBoardSprite() {
		return boardSprite;
	}

	@Override
	public Sprite getPacManStandingSprite(int dir) {
		return pacManStandingSprite;
	}

	@Override
	public Sprite getPacManRunningSprite(int dir) {
		return pacManRunningSprites.get(dir);
	}

	@Override
	public Sprite getPacManDyingSprite() {
		return null;
	}

	@Override
	public Sprite getGhostNormalSprite(String ghostName, int dir) {
		return ghostNormalSprites.get(ghostName).get(dir);
	}

	@Override
	public Sprite getGhostFrightenedSprite() {
		return ghostFrightenedSprite;
	}

	@Override
	public Sprite getGhostRecoveringSprite() {
		return ghostRecoveringSprite;
	}

	@Override
	public Sprite getGhostDeadSprite(int dir) {
		return ghostDeadSprite;
	}

	@Override
	public Sprite getEnergizerSprite() {
		return energizerSprite;
	}

	@Override
	public Sprite getPelletSprite() {
		return pillSprite;
	}

	@Override
	public Sprite getLifeSprite() {
		return lifeSprite;
	}

	@Override
	public Sprite getBonusSprite(BonusSymbol symbol) {
		return bonusSymbolSprites.get(symbol);
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
