package de.amr.games.muehle.ui;

import static de.amr.games.muehle.board.StoneType.WHITE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.IntSupplier;

import de.amr.easy.game.assets.Assets;
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
		textFont = Assets.storeFont("stone-counter-font", "fonts/Cookie-Regular.ttf", getHeight(), Font.BOLD);
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
		g.setColor(selected ? Color.RED : getType() == WHITE ? Color.BLACK : Color.WHITE);
		g.setFont(textFont);
		String text = String.valueOf(numStones);
		int width = g.getFontMetrics().stringWidth(text);
		g.drawString(text, -width / 2, getHeight() / 4);
		g.translate(-tf.getX(), -tf.getY());
	}
}