package de.amr.games.pacman.ui;

import static de.amr.easy.game.Application.Assets;
import static de.amr.easy.game.sprite.AnimationMode.BACK_AND_FORTH;
import static de.amr.easy.game.sprite.AnimationMode.CYCLIC;
import static de.amr.easy.grid.api.Direction.E;
import static de.amr.easy.grid.api.Direction.N;
import static de.amr.easy.grid.api.Direction.S;
import static de.amr.easy.grid.api.Direction.W;
import static de.amr.games.pacman.entities.ghost.GhostName.Blinky;
import static de.amr.games.pacman.entities.ghost.GhostName.Clyde;
import static de.amr.games.pacman.entities.ghost.GhostName.Inky;
import static de.amr.games.pacman.entities.ghost.GhostName.Pinky;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.api.Direction;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.Bonus;
import de.amr.games.pacman.entities.ghost.GhostName;

public class ClassicUI extends PacManUI {

	private final Sprite board;
	private final Map<Direction, Sprite> pacManRunning = new EnumMap<>(Direction.class);
	private final Sprite pacManStanding;
	private final Sprite pacManDying;
	private final Map<GhostName, Map<Direction, Sprite>> ghostNormal = new EnumMap<>(GhostName.class);
	private final Sprite ghostFrightened;
	private final Sprite ghostRecovering;
	private final Map<Direction, Sprite> ghostDead = new EnumMap<>(Direction.class);
	private final Map<Bonus, Sprite> bonusSprites = new EnumMap<>(Bonus.class);
	private final Sprite energizer;
	private final Sprite pellet;
	private final Sprite life;
	private final Font textFont;
	private final Color hudColor;

	private Image $(int row, int col) {
		BufferedImage sheet = Assets.image("pacman_original.png");
		return sheet.getSubimage(456 + col * 16, row * 16, 16, 16);
	}

	private Sprite createEnergizerSprite() {
		Image energizer = createOpaqueImage(TileSize, TileSize);
		Graphics2D g = (Graphics2D) energizer.getGraphics();
		g.setColor(Color.PINK);
		g.fillOval(0, 0, TileSize, TileSize);
		Sprite sprite = new Sprite(energizer, EmptyImage);
		sprite.createAnimation(AnimationMode.BACK_AND_FORTH, 333);
		return sprite;
	}

	private Sprite createPelletSprite() {
		int size = TileSize / 4, offset = TileSize * 3 / 8;
		Image pellet = createOpaqueImage(TileSize, TileSize);
		Graphics2D g = (Graphics2D) pellet.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.PINK);
		g.fillRect(offset, offset, size, size);
		return new Sprite(pellet);
	}

	public ClassicUI() {
		BufferedImage sheet = Assets.image("pacman_original.png");
		board = new Sprite(sheet.getSubimage(228, 0, 224, 248));
		board.scale(TileSize * Board.Cols, TileSize * (Board.Rows - 5));

		for (Bonus symbol : Bonus.values()) {
			Sprite sprite = new Sprite(sheet.getSubimage(488 + symbol.ordinal() * 16, 48, 16, 16));
			sprite.scale(SpriteSize, SpriteSize);
			bonusSprites.put(symbol, sprite);
		}

		List<Direction> dirs = Arrays.asList(E, W, N, S);
		List<GhostName> ghostNames = Arrays.asList(Blinky, Pinky, Inky, Clyde);

		// Pac-Man

		pacManStanding = new Sprite($(0, 2)).scale(SpriteSize, SpriteSize);

		for (int dir = 0; dir < dirs.size(); ++dir) {
			Sprite sprite = new Sprite($(dir, 1), $(dir, 0));
			sprite.scale(SpriteSize, SpriteSize);
			sprite.createAnimation(BACK_AND_FORTH, 120);
			pacManRunning.put(dirs.get(dir), sprite);
		}

		Image[] dyingAnimationFrames = new Image[11];
		for (int col = 3; col <= 13; ++col) {
			dyingAnimationFrames[col - 3] = $(0, col);
		}
		pacManDying = new Sprite(dyingAnimationFrames);
		pacManDying.scale(SpriteSize, SpriteSize);
		pacManDying.createAnimation(AnimationMode.LEFT_TO_RIGHT, 160);

		// Ghosts
		int ghostFrame = 333;

		for (int ghost = 0; ghost < ghostNames.size(); ++ghost) {
			ghostNormal.put(ghostNames.get(ghost), new EnumMap<>(Direction.class));
			for (int dir = 0; dir < dirs.size(); ++dir) {
				Sprite sprite = new Sprite($(4 + ghost, 2 * dir), $(4 + ghost, 2 * dir + 1));
				sprite.scale(SpriteSize, SpriteSize);
				sprite.createAnimation(CYCLIC, ghostFrame);
				ghostNormal.get(ghostNames.get(ghost)).put(dirs.get(dir), sprite);
			}
		}

		ghostFrightened = new Sprite($(4, 8), $(4, 9));
		ghostFrightened.scale(SpriteSize, SpriteSize);
		ghostFrightened.createAnimation(CYCLIC, ghostFrame);

		ghostRecovering = new Sprite($(4, 8), $(4, 10), $(4, 9), $(4, 11));
		ghostRecovering.scale(SpriteSize, SpriteSize);
		ghostRecovering.createAnimation(CYCLIC, ghostFrame);

		for (int dir = 0; dir < dirs.size(); ++dir) {
			Sprite sprite = new Sprite($(5, 8 + dir));
			sprite.scale(SpriteSize, SpriteSize);
			ghostDead.put(dirs.get(dir), sprite);
		}

		// Pills
		energizer = createEnergizerSprite();
		pellet = createPelletSprite();

		// Lives
		life = new Sprite($(1, 1));
		life.scale(SpriteSize, SpriteSize);

		// Text display
		Assets.storeFont("textFont", "fonts/arcadeclassic.ttf", TileSize * 1.5f, Font.PLAIN);
		textFont = Assets.font("textFont");
		hudColor = Color.YELLOW;
	}

	@Override
	public Sprite getBoard() {
		return board;
	}

	@Override
	public Sprite getPacManStanding(Direction dir) {
		return pacManStanding;
	}

	@Override
	public Sprite getPacManRunning(Direction dir) {
		return pacManRunning.get(dir);
	}

	@Override
	public Sprite getPacManDying() {
		return pacManDying;
	}

	@Override
	public Sprite getGhostNormal(GhostName ghost, Direction dir) {
		return ghostNormal.get(ghost).get(dir);
	}

	@Override
	public Sprite getGhostFrightened() {
		return ghostFrightened;
	}

	@Override
	public Sprite getGhostRecovering() {
		return ghostRecovering;
	}

	@Override
	public Sprite getGhostDead(Direction dir) {
		return ghostDead.get(dir);
	}

	@Override
	public Sprite getEnergizer() {
		return energizer;
	}

	@Override
	public Sprite getPellet() {
		return pellet;
	}

	@Override
	public Sprite getLife() {
		return life;
	}

	@Override
	public Sprite getBonus(Bonus bonus) {
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