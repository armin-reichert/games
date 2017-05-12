package de.amr.games.pacman.ui;

import static de.amr.easy.game.sprite.AnimationMode.CYCLIC;
import static de.amr.games.pacman.data.Board.NUM_COLS;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.data.BonusSymbol;

/**
 * Base class of the different Pac-Man UI themes.
 * 
 * @author Armin Reichert
 *
 */
public abstract class PacManUI {

	/** Pixel size of a tile. */
	public static int TILE_SIZE = 8 * 2;

	/** Pixel size of the sprites for Pac-Man and ghosts. */
	public static int SPRITE_SIZE = 16 * 2;

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
		Image img = createOpaqueImage(TILE_SIZE * NUM_COLS, (int) fontSize);
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
	public abstract Sprite getBoard();

	/** Returns the sprite for Pac-Man when standing and looking into the given direction. */
	public abstract Sprite getPacManStanding(int dir);

	/** Returns the sprite for Pac-Man when running into given direction. */
	public abstract Sprite getPacManRunning(int dir);

	/** Returns the sprite for Pac-Man when dying. */
	public abstract Sprite getPacManDying();

	/** Returns the sprite for the specified ghost when looking into the given direction. */
	public abstract Sprite getGhostNormal(String ghostName, int dir);

	/** Returns the sprite for a frightened ghost. */
	public abstract Sprite getGhostFrightened();

	/** Returns the sprite for a recovering ghost. */
	public abstract Sprite getGhostRecovering();

	/** Returns the sprite for a dead ghost when looking into the given direction. */
	public abstract Sprite getGhostDead(int dir);

	/** Returns the sprite for an energizer pellet. */
	public abstract Sprite getEnergizer();

	/** Returns the sprite for a pellet. */
	public abstract Sprite getPellet();

	/** Returns the sprite for a single life in the life counter. */
	public abstract Sprite getLife();

	/** Returns the sprite for the specified bonus symbol. */
	public abstract Sprite getBonus(BonusSymbol symbol);

	/** Returns the font for drawing texts inside the board. */
	public abstract Font getTextFont();

	/** Returns the color of the HUD (score, high score, level number). */
	public abstract Color getHUDColor();
}