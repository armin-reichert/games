package de.amr.games.pacman.theme;

import static de.amr.easy.game.sprite.AnimationMode.BACK_AND_FORTH;
import static de.amr.easy.game.sprite.AnimationMode.CYCLIC;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
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
 * A Pac-Man UI which tries to be close to the classic Arcade game.
 * 
 * @author Armin Reichert
 *
 */
public class ClassicTheme extends PacManTheme {

	private final BufferedImage sheet;
	private final Sprite boardSprite;
	private final Map<Integer, Sprite> pacManRunningSprites = new HashMap<>();
	private final Sprite pacManStandingSprite;
	private final Sprite pacManDyingSprite;
	private final Map<String, Map<Integer, Sprite>> ghostNormalSprites = new HashMap<>();
	private final Sprite ghostFrightenedSprite;
	private final Sprite ghostRecoveringSprite;
	private final Map<Integer, Sprite> ghostDeadSprites = new HashMap<>();
	private final Map<BonusSymbol, Sprite> bonusSprites = new EnumMap<>(BonusSymbol.class);
	private final Sprite energizerSprite;
	private final Sprite pelletSprite;
	private final Sprite lifeSprite;
	private final Font textFont;
	private final Color hudColor;

	private Image tile(int row, int col) {
		return sheet.getSubimage(456 + col * 16, row * 16, 16, 16);
	}

	private Sprite createEnergizerSprite() {
		Image energizerImage = createOpaqueImage(TILE_SIZE, TILE_SIZE);
		Graphics2D g = (Graphics2D) energizerImage.getGraphics();
		g.setColor(Color.PINK);
		g.fillOval(0, 0, TILE_SIZE, TILE_SIZE);
		Sprite sprite = new Sprite(energizerImage, EmptyImage);
		sprite.createAnimation(AnimationMode.BACK_AND_FORTH, 333);
		return sprite;
	}

	private Sprite createPelletSprite() {
		Image pelletImage = createOpaqueImage(TILE_SIZE, TILE_SIZE);
		int size = TILE_SIZE / 4, offset = TILE_SIZE * 3 / 8;
		Graphics2D g = (Graphics2D) pelletImage.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.PINK);
		g.fillOval(offset, offset, size, size);
		return new Sprite(pelletImage);
	}

	public ClassicTheme(Assets assets) {
		sheet = assets.image("pacman_original.png");

		boardSprite = new Sprite(sheet.getSubimage(228, 0, 224, 248));
		boardSprite.scale(TILE_SIZE * 28, TILE_SIZE * 31);

		for (BonusSymbol bonusSymbol : BonusSymbol.values()) {
			Sprite sprite = new Sprite(sheet.getSubimage(488 + bonusSymbol.ordinal() * 16, 48, 16, 16));
			sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
			bonusSprites.put(bonusSymbol, sprite);
		}

		List<Integer> dirs = Arrays.asList(E, W, N, S);
		List<String> ghostNames = Arrays.asList("Blinky", "Pinky", "Inky", "Clyde");

		// Pac-Man

		pacManStandingSprite = new Sprite(tile(0, 2)).scale(SPRITE_SIZE, SPRITE_SIZE);

		for (int dir = 0; dir < dirs.size(); ++dir) {
			Sprite sprite = new Sprite(tile(dir, 1), tile(dir, 0));
			sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
			sprite.createAnimation(BACK_AND_FORTH, 100);
			pacManRunningSprites.put(dirs.get(dir), sprite);
		}

		Image[] dyingAnimationFrames = new Image[11];
		for (int col = 3; col <= 13; ++col) {
			dyingAnimationFrames[col - 3] = tile(0, col);
		}
		pacManDyingSprite = new Sprite(dyingAnimationFrames);
		pacManDyingSprite.scale(SPRITE_SIZE, SPRITE_SIZE);
		pacManDyingSprite.createAnimation(AnimationMode.LEFT_TO_RIGHT, 160);

		// Ghosts
		int ghostFrameMillis = 333;
		for (int ghost = 0; ghost < ghostNames.size(); ++ghost) {
			ghostNormalSprites.put(ghostNames.get(ghost), new HashMap<>());
			for (int dir = 0; dir < dirs.size(); ++dir) {
				Sprite sprite = new Sprite(tile(4 + ghost, 2 * dir), tile(4 + ghost, 2 * dir + 1));
				sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
				sprite.createAnimation(CYCLIC, ghostFrameMillis);
				ghostNormalSprites.get(ghostNames.get(ghost)).put(dirs.get(dir), sprite);
			}
		}

		ghostFrightenedSprite = new Sprite(tile(4, 8), tile(4, 9));
		ghostFrightenedSprite.scale(SPRITE_SIZE, SPRITE_SIZE);
		ghostFrightenedSprite.createAnimation(CYCLIC, ghostFrameMillis);

		ghostRecoveringSprite = new Sprite(tile(4, 8), tile(4, 10), tile(4, 9), tile(4, 11));
		ghostRecoveringSprite.scale(SPRITE_SIZE, SPRITE_SIZE);
		ghostRecoveringSprite.createAnimation(CYCLIC, ghostFrameMillis);

		for (int dir = 0; dir < dirs.size(); ++dir) {
			Sprite sprite = new Sprite(tile(5, 8 + dir));
			sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
			ghostDeadSprites.put(dirs.get(dir), sprite);
		}

		// Pills
		energizerSprite = createEnergizerSprite();
		pelletSprite = createPelletSprite();

		// Lives
		lifeSprite = new Sprite(tile(1, 1));
		lifeSprite.scale(SPRITE_SIZE, SPRITE_SIZE);

		// Text display
		assets.storeFont("textFont", "fonts/arcadeclassic.ttf", TILE_SIZE * 1.5f, Font.PLAIN);
		textFont = assets.font("textFont");
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
		return pacManDyingSprite;
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
		return ghostDeadSprites.get(dir);
	}

	@Override
	public Sprite getEnergizerSprite() {
		return energizerSprite;
	}

	@Override
	public Sprite getPelletSprite() {
		return pelletSprite;
	}

	@Override
	public Sprite getLifeSprite() {
		return lifeSprite;
	}

	@Override
	public Sprite getBonusSprite(BonusSymbol bonus) {
		return bonusSprites.get(bonus);
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