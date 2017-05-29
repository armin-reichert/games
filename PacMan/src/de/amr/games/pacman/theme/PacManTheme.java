package de.amr.games.pacman.theme;

import static de.amr.easy.game.sprite.AnimationMode.CYCLIC;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.core.board.BonusSymbol;

/**
 * Base class for Pac-Man themes.
 * 
 * @author Armin Reichert
 */
public abstract class PacManTheme {

	/** Pixel size of a tile. */
	public static int TILE_SIZE = 16;

	/** Pixel size of sprite images. */
	public static int SPRITE_SIZE = 32;

	/** A 1x1 opaque image. */
	public static final Image EmptyImage = createOpaqueImage(1, 1);

	/** Creates a buffered image which is able to show pictures with transparent areas. */
	public static Image createTransparentImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/** Creates a buffered image for showing opaque pictures. */
	public static Image createOpaqueImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	/**
	 * Creates a text sprite with optional blinking effect.
	 * 
	 * @param text
	 *          the text to display
	 * @param fontSize
	 *          the text's font size
	 * @param color
	 *          the text color
	 * @param blinkTime
	 *          the blink time in milliseconds
	 */
	protected Sprite createTextSprite(String text, float fontSize, Color color, int blinkTime) {

		// create temporary image for computing the text width
		Image img = createOpaqueImage(Toolkit.getDefaultToolkit().getScreenSize().width, (int) fontSize);
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(color);
		g.setFont(getTextFont().deriveFont(fontSize));
		int textWidth = g.getFontMetrics().stringWidth(text);
		img.flush();

		Image scaled = createOpaqueImage(textWidth, (int) fontSize);
		Graphics2D g2 = (Graphics2D) scaled.getGraphics();
		g2.setColor(g.getColor());
		g2.setFont(g.getFont());
		g2.drawString(text, 0, (int) fontSize - g2.getFontMetrics().getDescent());

		if (blinkTime > 0) {
			Sprite sprite = new Sprite(scaled, EmptyImage);
			sprite.createAnimation(CYCLIC, blinkTime);
			return sprite;
		} else {
			return new Sprite(scaled);
		}
	}

	/** Returns the board as a "sprite". */
	public abstract Sprite getBoardSprite();

	/** Returns the sprite for Pac-Man when standing and looking into the given direction. */
	public abstract Sprite getPacManStandingSprite(int dir);

	/** Returns the sprite for Pac-Man when running into given direction. */
	public abstract Sprite getPacManRunningSprite(int dir);

	/** Returns the sprite for Pac-Man when dying. */
	public abstract Sprite getPacManDyingSprite();

	/** Returns the sprite for the specified ghost when looking into the given direction. */
	public abstract Sprite getGhostNormalSprite(String ghostName, int dir);

	/** Returns the sprite for a frightened ghost. */
	public abstract Sprite getGhostFrightenedSprite();

	/** Returns the sprite for a recovering ghost. */
	public abstract Sprite getGhostRecoveringSprite();

	/** Returns the sprite for a dead ghost when looking into the given direction. */
	public abstract Sprite getGhostDeadSprite(int dir);

	/** Returns the sprite for an energizer pellet. */
	public abstract Sprite getEnergizerSprite();

	/** Returns the sprite for a pellet. */
	public abstract Sprite getPelletSprite();

	/** Returns the sprite for a single life in the life counter. */
	public abstract Sprite getLifeSprite();

	/** Returns the sprite for the specified bonus symbol. */
	public abstract Sprite getBonusSprite(BonusSymbol symbol);

	/** Returns the font for drawing texts inside the board. */
	public abstract Font getTextFont();

	/** Returns the color of the HUD (score, high score, level number). */
	public abstract Color getHUDColor();
}