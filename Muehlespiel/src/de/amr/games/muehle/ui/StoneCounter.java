package de.amr.games.muehle.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

import de.amr.games.muehle.board.StoneColor;

/**
 * Visual representation of a stack of stones.
 * 
 * @author Armin Reichert
 */
public class StoneCounter extends Stone {

	private Font font;
	private BooleanSupplier selectedSupplier;
	private IntSupplier stoneCountSupplier;

	public StoneCounter(StoneColor color, int radius, IntSupplier stoneCountSupplier, BooleanSupplier selectedSupplier) {
		super(color, radius);
		this.font = new Font(Font.MONOSPACED, Font.BOLD, 2 * radius);
		this.stoneCountSupplier = stoneCountSupplier;
		this.selectedSupplier = selectedSupplier;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public boolean isSelected() {
		return selectedSupplier.getAsBoolean();
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
			g.setColor(isSelected() ? Color.RED : Color.DARK_GRAY);
			g.setFont(font);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.drawString(text, 2 * radius, radius);
			g.translate(-tf.getX(), -tf.getY());
		}
	}
}