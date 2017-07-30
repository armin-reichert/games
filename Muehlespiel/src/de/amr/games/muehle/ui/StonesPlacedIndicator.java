package de.amr.games.muehle.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.function.IntSupplier;

import de.amr.games.muehle.board.Stone;
import de.amr.games.muehle.board.StoneColor;

public class StonesPlacedIndicator extends Stone {

	private int totalStoneCount;
	private IntSupplier stonesPlacedSupplier;

	public StonesPlacedIndicator(StoneColor stoneColor, int totalStoneCount, IntSupplier stonesPlacedSupplier) {
		super(stoneColor);
		this.totalStoneCount = totalStoneCount;
		this.stonesPlacedSupplier = stonesPlacedSupplier;
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		g.translate(tf.getX(), tf.getY());
		g.setColor(getColor() == StoneColor.WHITE ? Color.BLACK : Color.WHITE);
		int fontSize = getHeight() / 2;
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
		int stonesLeft = totalStoneCount - stonesPlacedSupplier.getAsInt();
		String text = String.valueOf(stonesLeft);
		g.drawString(text, -fontSize / 4, fontSize / 4);
		g.translate(-tf.getX(), -tf.getY());
	}
}