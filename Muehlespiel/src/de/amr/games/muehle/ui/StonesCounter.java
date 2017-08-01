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
		super.draw(g);
		g.translate(tf.getX(), tf.getY());
		g.setColor(getColor() == StoneType.WHITE ? Color.BLACK : Color.WHITE);
		int fontSize = getHeight() / 2;
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
		String text = String.valueOf(stoneCountSupplier.getAsInt());
		g.drawString(text, -fontSize / 4, fontSize / 4);
		if (highlighted) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.GREEN);
			g.setStroke(new BasicStroke(4));
			int radius = Stone.radius;
			g.drawOval(-radius, -radius, 2 * radius, 2 * radius);
		}
		g.translate(-tf.getX(), -tf.getY());
	}
}