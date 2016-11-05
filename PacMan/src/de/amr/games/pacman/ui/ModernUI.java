package de.amr.games.pacman.ui;

import static de.amr.easy.game.Application.Assets;
import static de.amr.easy.grid.api.Dir4.E;
import static de.amr.easy.grid.api.Dir4.N;
import static de.amr.easy.grid.api.Dir4.S;
import static de.amr.easy.grid.api.Dir4.W;
import static de.amr.games.pacman.data.Board.Cols;
import static de.amr.games.pacman.data.Board.Rows;
import static de.amr.games.pacman.entities.ghost.GhostName.Blinky;
import static de.amr.games.pacman.entities.ghost.GhostName.Clyde;
import static de.amr.games.pacman.entities.ghost.GhostName.Inky;
import static de.amr.games.pacman.entities.ghost.GhostName.Pinky;
import static de.amr.games.pacman.entities.ghost.GhostName.Stinky;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.api.Dir4;
import de.amr.games.pacman.data.Bonus;
import de.amr.games.pacman.entities.ghost.GhostName;

public class ModernUI extends PacManUI {

	private final Sprite board;
	private final Map<Dir4, Sprite> pacManRunning = new EnumMap<>(Dir4.class);
	private final Sprite pacManStanding;
	private final Map<GhostName, Map<Dir4, Sprite>> ghostNormal = new EnumMap<>(GhostName.class);
	private final Sprite ghostFrightened;
	private final Sprite ghostRecovering;
	private final Sprite ghostDead;
	private final Map<Bonus, Sprite> bonusSymbols = new EnumMap<>(Bonus.class);
	private final Sprite energizer;
	private final Sprite pill;
	private final Sprite life;
	private final Font textFont;
	private final Color hudColor;

	private Image $(BufferedImage sheet, int row, int col) {
		return sheet.getSubimage(col * 32, row * 32, 32, 32).getScaledInstance(SpriteSize, SpriteSize,
				Image.SCALE_SMOOTH);
	}

	private Image $(int row, int col) {
		BufferedImage sheet = Assets.image("chompersprites.png");
		return $(sheet, row, col);
	}

	public ModernUI() {

		BufferedImage sheet = Assets.image("pacman_original.png");
		board = new Sprite(sheet.getSubimage(228, 0, 224, 248)).scale(Cols * TileSize,
				(Rows - 5) * TileSize);

		int size = TileSize * 15 / 10;
		for (Bonus symbol : Bonus.values()) {
			bonusSymbols.put(symbol,
					new Sprite(sheet.getSubimage(488 + symbol.ordinal() * 16, 48, 16, 16)).scale(size, size));
		}

		List<Dir4> dirs = Arrays.asList(E, S, W, N);
		List<GhostName> ghostNames = Arrays.asList(Blinky, Clyde, Pinky, Stinky, Inky);

		pacManStanding = new Sprite($(2, 11));

		for (int dir = 0; dir < dirs.size(); ++dir) {
			Sprite sprite = new Sprite($(dir, 11), $(dir, 10));
			sprite.scale(SpriteSize, SpriteSize);
			sprite.createAnimation(AnimationMode.CYCLIC, 100);
			pacManRunning.put(dirs.get(dir), sprite);
		}

		int ghostFrame = 333;

		for (int ghost = 0; ghost < ghostNames.size(); ghost++) {
			ghostNormal.put(ghostNames.get(ghost), new EnumMap<>(Dir4.class));
			for (int dir = 0; dir < dirs.size(); ++dir) {
				Sprite sprite = new Sprite($(dir, 2 * ghost), $(dir, 2 * ghost + 1));
				sprite.scale(SpriteSize, SpriteSize);
				sprite.createAnimation(AnimationMode.CYCLIC, ghostFrame);
				ghostNormal.get(ghostNames.get(ghost)).put(dirs.get(dir), sprite);
			}
		}

		ghostFrightened = new Sprite($(0, 12), $(0, 13)).scale(SpriteSize, SpriteSize);
		ghostFrightened.createAnimation(AnimationMode.CYCLIC, ghostFrame);

		ghostRecovering = new Sprite($(0, 12), $(1, 12), $(0, 13), $(1, 13)).scale(SpriteSize,
				SpriteSize);
		ghostRecovering.createAnimation(AnimationMode.CYCLIC, ghostFrame);

		ghostDead = new Sprite($(2, 12), $(2, 13)).scale(SpriteSize, SpriteSize);
		ghostDead.createAnimation(AnimationMode.CYCLIC, ghostFrame);

		life = new Sprite($(2, 11)).scale(SpriteSize, SpriteSize);
		BufferedImage sheet2 = Assets.image("" + "chompermazetiles.png");

		energizer = new Sprite($(sheet2, 2, 7), $(sheet2, 2, 8)).scale(TileSize, TileSize);
		energizer.createAnimation(AnimationMode.BACK_AND_FORTH, 333);

		pill = new Sprite($(sheet2, 2, 9)).scale(TileSize, TileSize);

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
	public Sprite getPacManStanding(Dir4 dir) {
		return pacManStanding;
	}

	@Override
	public Sprite getPacManRunning(Dir4 dir) {
		return pacManRunning.get(dir);
	}

	@Override
	public Sprite getPacManDying() {
		return null;
	}

	@Override
	public Sprite getGhostNormal(GhostName ghost, Dir4 dir) {
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
	public Sprite getGhostDead(Dir4 dir) {
		return ghostDead;
	}

	@Override
	public Sprite getEnergizer() {
		return energizer;
	}

	@Override
	public Sprite getPellet() {
		return pill;
	}

	@Override
	public Sprite getLife() {
		return life;
	}

	@Override
	public Sprite getBonus(Bonus symbol) {
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
