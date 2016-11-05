package de.amr.games.pacman.ui;

import static de.amr.easy.game.sprite.AnimationMode.CYCLIC;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.api.Dir4;
import de.amr.games.pacman.data.Board;
import de.amr.games.pacman.data.Bonus;
import de.amr.games.pacman.entities.ghost.GhostName;

public abstract class PacManUI {

	public static int TileSize = 8 * 2;
	public static int SpriteSize = 16 * 2;

	public static Image createTransparentImage(int width, int height) {
		// GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
		// .getDefaultScreenDevice().getDefaultConfiguration();
		// return gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	public static Image createOpaqueImage(int width, int height) {
		// GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
		// .getDefaultScreenDevice().getDefaultConfiguration();
		// return gc.createCompatibleImage(width, height, Transparency.OPAQUE);
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	protected static final Image EmptyImage = createOpaqueImage(1, 1);

	protected Sprite createTextSprite(String text, float fontSize, Color color, int blinkTime) {

		Image img = createOpaqueImage(TileSize * Board.Cols, (int) fontSize);
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(color);
		g.setFont(getTextFont().deriveFont(fontSize));
		int textWidth = g.getFontMetrics().stringWidth(text);

		Image scaled = createOpaqueImage(textWidth, (int) fontSize);
		Graphics2D g2 = (Graphics2D) scaled.getGraphics();
		g2.setColor(g.getColor());
		g2.setFont(g.getFont());
		g2.drawString(text, 0, (int) fontSize - g2.getFontMetrics().getDescent());
		img.flush();

		if (blinkTime > 0) {
			Sprite sprite = new Sprite(scaled, EmptyImage);
			sprite.createAnimation(CYCLIC, blinkTime);
			return sprite;
		} else {
			return new Sprite(scaled);
		}
	}

	public abstract Sprite getBoard();
	
	public abstract Sprite getPacManStanding(Dir4 dir);

	public abstract Sprite getPacManRunning(Dir4 dir);

	public abstract Sprite getPacManDying();

	public abstract Sprite getGhostNormal(GhostName ghost, Dir4 dir);

	public abstract Sprite getGhostFrightened();

	public abstract Sprite getGhostRecovering();

	public abstract Sprite getGhostDead(Dir4 dir);

	public abstract Sprite getEnergizer();

	public abstract Sprite getPellet();

	public abstract Sprite getLife();

	public abstract Sprite getBonus(Bonus symbol);

	public abstract Font getTextFont();

	public abstract Color getHUDColor();
}
