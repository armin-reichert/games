package de.amr.games.muehle.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.function.IntSupplier;

import de.amr.games.muehle.board.StoneType;

/**
 * Visual indicator of placed stones.
 * 
 * @author Armin Reichert
 */
public class StonesPlacedCounter extends Stone {

	private int numStonesToPlace;
	private IntSupplier stonesPlacedSupplier;

	public StonesPlacedCounter(StoneType stoneType, int numStonesToPlace, IntSupplier stonesPlacedSupplier) {
		super(stoneType);
		this.numStonesToPlace = numStonesToPlace;
		this.stonesPlacedSupplier = stonesPlacedSupplier;
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		g.translate(tf.getX(), tf.getY());
		g.setColor(getColor() == StoneType.WHITE ? Color.BLACK : Color.WHITE);
		int fontSize = getHeight() / 2;
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
		int stonesLeft = numStonesToPlace - stonesPlacedSupplier.getAsInt();
		String text = String.valueOf(stonesLeft);
		g.drawString(text, -fontSize / 4, fontSize / 4);
		g.translate(-tf.getX(), -tf.getY());
	}
}