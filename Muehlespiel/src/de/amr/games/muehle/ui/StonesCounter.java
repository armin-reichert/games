package de.amr.games.muehle.ui;

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

	private Font textFont;
	private boolean selected;
	private IntSupplier stoneCountSupplier;

	public StonesCounter(StoneType stoneType, IntSupplier stoneCountSupplier) {
		super(stoneType);
		this.stoneCountSupplier = stoneCountSupplier;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	@Override
	public void init() {
		textFont = new Font(Font.MONOSPACED, Font.BOLD, 2 * Stone.radius);
	}

	@Override
	public void draw(Graphics2D g) {
		int numStones = stoneCountSupplier.getAsInt();
		String text = String.valueOf(numStones);
		for (int i = numStones - 1; i >= 0; --i) {
			g.translate(5 * i, -5 * i);
			super.draw(g);
			g.translate(-5 * i, 5 * i);
		}
		if (numStones > 1) {
			g.translate(tf.getX(), tf.getY());
			g.setColor(selected ? Color.RED : Color.DARK_GRAY);
			g.setFont(textFont);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(text, 2 * radius, radius);
			g.translate(-tf.getX(), -tf.getY());
		}
	}
}