package de.amr.games.pacman.theme;

import static de.amr.easy.game.sprite.AnimationMode.BACK_AND_FORTH;
import static de.amr.easy.game.sprite.AnimationMode.CYCLIC;
import static de.amr.easy.grid.impl.Top4.E;
import static de.amr.easy.grid.impl.Top4.N;
import static de.amr.easy.grid.impl.Top4.S;
import static de.amr.easy.grid.impl.Top4.W;
import static de.amr.games.pacman.core.board.Board.NUM_COLS;
import static de.amr.games.pacman.core.board.Board.NUM_ROWS;

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

	private final Assets assets;
	private final Sprite board;
	private final Map<Integer, Sprite> pacManRunning = new HashMap<>();
	private final Sprite pacManStanding;
	private final Sprite pacManDying;
	private final Map<String, Map<Integer, Sprite>> ghostNormal = new HashMap<>();
	private final Sprite ghostFrightened;
	private final Sprite ghostRecovering;
	private final Map<Integer, Sprite> ghostDead = new HashMap<>();
	private final Map<BonusSymbol, Sprite> bonusSprites = new EnumMap<>(BonusSymbol.class);
	private final Sprite energizer;
	private final Sprite pellet;
	private final Sprite life;
	private final Font textFont;
	private final Color hudColor;

	private Image $(int row, int col) {
		BufferedImage sheet = assets.image("pacman_original.png");
		return sheet.getSubimage(456 + col * 16, row * 16, 16, 16);
	}

	private Sprite createEnergizerSprite() {
		Image energizer = createOpaqueImage(TILE_SIZE, TILE_SIZE);
		Graphics2D g = (Graphics2D) energizer.getGraphics();
		g.setColor(Color.PINK);
		g.fillOval(0, 0, TILE_SIZE, TILE_SIZE);
		Sprite sprite = new Sprite(energizer, EmptyImage);
		sprite.createAnimation(AnimationMode.BACK_AND_FORTH, 333);
		return sprite;
	}

	private Sprite createPelletSprite() {
		int size = TILE_SIZE / 4, offset = TILE_SIZE * 3 / 8;
		Image pellet = createOpaqueImage(TILE_SIZE, TILE_SIZE);
		Graphics2D g = (Graphics2D) pellet.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.PINK);
		g.fillRect(offset, offset, size, size);
		return new Sprite(pellet);
	}

	public ClassicTheme(Assets assets) {
		this.assets = assets;
		BufferedImage sheet = assets.image("pacman_original.png");
		board = new Sprite(sheet.getSubimage(228, 0, 224, 248));
		board.scale(TILE_SIZE * NUM_COLS, TILE_SIZE * (NUM_ROWS - 5));

		for (BonusSymbol symbol : BonusSymbol.values()) {
			Sprite sprite = new Sprite(sheet.getSubimage(488 + symbol.ordinal() * 16, 48, 16, 16));
			sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
			bonusSprites.put(symbol, sprite);
		}

		List<Integer> dirs = Arrays.asList(E, W, N, S);
		List<String> ghostNames = Arrays.asList("Blinky", "Pinky", "Inky", "Clyde");

		// Pac-Man

		pacManStanding = new Sprite($(0, 2)).scale(SPRITE_SIZE, SPRITE_SIZE);

		for (int dir = 0; dir < dirs.size(); ++dir) {
			Sprite sprite = new Sprite($(0, 2), $(dir, 1), $(dir, 0));
			sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
			sprite.createAnimation(BACK_AND_FORTH, 80);
			pacManRunning.put(dirs.get(dir), sprite);
		}

		Image[] dyingAnimationFrames = new Image[11];
		for (int col = 3; col <= 13; ++col) {
			dyingAnimationFrames[col - 3] = $(0, col);
		}
		pacManDying = new Sprite(dyingAnimationFrames);
		pacManDying.scale(SPRITE_SIZE, SPRITE_SIZE);
		pacManDying.createAnimation(AnimationMode.LEFT_TO_RIGHT, 160);

		// Ghosts
		int ghostFrame = 333;

		for (int ghost = 0; ghost < ghostNames.size(); ++ghost) {
			ghostNormal.put(ghostNames.get(ghost), new HashMap<>());
			for (int dir = 0; dir < dirs.size(); ++dir) {
				Sprite sprite = new Sprite($(4 + ghost, 2 * dir), $(4 + ghost, 2 * dir + 1));
				sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
				sprite.createAnimation(CYCLIC, ghostFrame);
				ghostNormal.get(ghostNames.get(ghost)).put(dirs.get(dir), sprite);
			}
		}

		ghostFrightened = new Sprite($(4, 8), $(4, 9));
		ghostFrightened.scale(SPRITE_SIZE, SPRITE_SIZE);
		ghostFrightened.createAnimation(CYCLIC, ghostFrame);

		ghostRecovering = new Sprite($(4, 8), $(4, 10), $(4, 9), $(4, 11));
		ghostRecovering.scale(SPRITE_SIZE, SPRITE_SIZE);
		ghostRecovering.createAnimation(CYCLIC, ghostFrame);

		for (int dir = 0; dir < dirs.size(); ++dir) {
			Sprite sprite = new Sprite($(5, 8 + dir));
			sprite.scale(SPRITE_SIZE, SPRITE_SIZE);
			ghostDead.put(dirs.get(dir), sprite);
		}

		// Pills
		energizer = createEnergizerSprite();
		pellet = createPelletSprite();

		// Lives
		life = new Sprite($(1, 1));
		life.scale(SPRITE_SIZE, SPRITE_SIZE);

		// Text display
		assets.storeFont("textFont", "fonts/arcadeclassic.ttf", TILE_SIZE * 1.5f, Font.PLAIN);
		textFont = assets.font("textFont");
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
		return pacManDying;
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
		return ghostDead.get(dir);
	}

	@Override
	public Sprite getEnergizerSprite() {
		return energizer;
	}

	@Override
	public Sprite getPelletSprite() {
		return pellet;
	}

	@Override
	public Sprite getLifeSprite() {
		return life;
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