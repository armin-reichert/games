package de.amr.games.muehle.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.IntSupplier;

import de.amr.games.muehle.board.StoneType;

/**
 * Visual indicator of a number of stones.
 * 
 * @author Armin Reichert
 */
public class StonesCounter extends Stone {

	private boolean highlighted;
	private IntSupplier stoneCountSupplier;

	public StonesCounter(StoneType stoneType, IntSupplier stoneCountSupplier) {
		super(stoneType);
		this.stoneCountSupplier = stoneCountSupplier;
	}

	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		int numStones = stoneCountSupplier.getAsInt();
		int delta = 5;
		int xOffset = delta * (numStones - 1), yOffset = -delta * (numStones - 1);
		for (int i = 0; i < numStones - 1; ++i) {
			g.translate(xOffset, yOffset);
			super.draw(g);
			g.translate(-xOffset, -yOffset);
			xOffset -= delta;
			yOffset += delta;
		}

		super.draw(g);
		g.translate(tf.getX(), tf.getY());
		g.setColor(getType() == StoneType.WHITE ? Color.BLACK : Color.WHITE);
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, getHeight() * 3 / 4));
		String text = String.valueOf(numStones);
		int width = g.getFontMetrics().stringWidth(text);
		g.drawString(text, -width / 2, getHeight() / 4);
		if (highlighted) {
			g.setColor(Color.GREEN);
			g.setStroke(new BasicStroke(4));
			g.drawOval(-radius, -radius, 2 * radius, 2 * radius);
		}
		g.translate(-tf.getX(), -tf.getY());
	}
}