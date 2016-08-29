package de.amr.easy.grid.rendering;

import java.awt.Color;
import java.awt.Font;

import de.amr.easy.grid.api.Direction;

/**
 * Grid rendering model providing sensible default values.
 * 
 * @param Cell
 *          the grid cell type
 * 
 * @author Armin Reichert
 */
public class DefaultGridRenderingModel<Cell> implements GridRenderingModel<Cell> {

	private static final Font FONT = new Font("Dialog", Font.PLAIN, 10);

	@Override
	public int getCellSize() {
		return 4;
	}

	@Override
	public Color getGridBgColor() {
		return Color.BLACK;
	}

	@Override
	public Color getPassageColor(Cell p, Direction dir) {
		return getCellBgColor(p);
	}

	@Override
	public int getPassageThickness() {
		return getCellSize() / 2;
	}

	@Override
	public Color getCellBgColor(Cell p) {
		return Color.WHITE;
	}

	@Override
	public String getCellText(Cell p) {
		return "";
	}

	@Override
	public Color getCellTextColor() {
		return Color.BLACK;
	}

	@Override
	public Font getCellTextFont() {
		return FONT;
	}
}
