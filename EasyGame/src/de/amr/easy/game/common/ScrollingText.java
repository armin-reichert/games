package de.amr.easy.game.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

/**
 * A multi-line text that can be scrolled over the screen.
 * 
 * @author Armin Reichert
 */
public class ScrollingText extends GameEntity {

	private String[] lines;
	private float lineSpacing;
	private Color color;
	private Font font;
	private boolean imageNeedsUpdate;

	public ScrollingText(String text) {
		setText(text);
		setScrollSpeed(0);
		setFont(new Font("Sans", Font.PLAIN, 40));
		setColor(Color.WHITE);
		setLineSpacing(1.5f);
	}

	public ScrollingText() {
		this("");
	}

	public void setText(String text) {
		this.lines = text.split("\n");
		imageNeedsUpdate = true;
	}

	public void setScrollSpeed(float speed) {
		tf.setVelocityY(speed);
	}

	public void setFont(Font font) {
		this.font = font;
		imageNeedsUpdate = true;
	}

	public void setColor(Color color) {
		this.color = color;
		imageNeedsUpdate = true;
	}

	public void setLineSpacing(float lineSpacing) {
		this.lineSpacing = lineSpacing;
		imageNeedsUpdate = true;
	}

	@Override
	public void update() {
		tf.move();
	}

	private void updateImage() {

		// helper image for computing bounds
		BufferedImage helperImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D pen = helperImage.createGraphics();
		pen.setFont(font);
		pen.setColor(color);
		pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		double textWidth = 1;
		double textHeight = 1;
		for (int i = 0; i < lines.length; ++i) {
			String line = lines[i];
			Rectangle2D lineBounds = pen.getFontMetrics().getStringBounds(line, pen);
			textHeight += lineBounds.getHeight();
			if (i < lines.length - 1) {
				textHeight += lineSpacing;
			}
			textWidth = Math.max(textWidth, lineBounds.getWidth());
		}

		// correctly sized image which will be used as sprite
		BufferedImage image = new BufferedImage((int) Math.ceil(textWidth), (int) Math.ceil(textHeight),
				BufferedImage.TYPE_INT_ARGB);
		pen = image.createGraphics();
		pen.setFont(font);
		pen.setColor(color);
		pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		FontMetrics fm = pen.getFontMetrics();
		float y = 0;
		for (int i = 0; i < lines.length; ++i) {
			String line = lines[i];
			Rectangle2D lineBounds = fm.getStringBounds(line, pen);
			pen.drawString(line, (float) (textWidth - lineBounds.getWidth()) / 2, y + fm.getMaxAscent());
			y += lineBounds.getHeight();
			if (i < lines.length - 1) {
				y += lineSpacing;
			}
		}
		setSprites(new Sprite(image));
	}

	@Override
	public void draw(Graphics2D pen) {
		if (imageNeedsUpdate) {
			updateImage();
		}
		super.draw(pen);
	}
}