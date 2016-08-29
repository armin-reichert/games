package de.amr.easy.grid.iterators.shapes;

import de.amr.easy.grid.api.Grid2D;

/**
 * Iterates over cells forming a circle around a center cell.
 * 
 * @author Armin Reichert
 *
 * @param <Cell>
 *          grid cell type
 */
public class Circle<Cell> extends Shape<Cell> {

	private final Cell center;
	private final int radius;

	public Circle(Grid2D<Cell, ?> grid, Cell center, int radius) {
		super(grid);
		this.center = center;
		this.radius = radius;

		int x = grid.col(center), y = grid.row(center);
		int dx = -1, dy = -(radius + 1);

		for (int i = 0; i <= radius; ++i) {
			// right downwards
			++dx;
			++dy;
			addCell(x + dx, y + dy);
		}
		for (int i = 0; i < radius; ++i) {
			// left downwards
			--dx;
			++dy;
			addCell(x + dx, y + dy);
		}
		for (int i = 0; i < radius; ++i) {
			// left upwards
			--dx;
			--dy;
			addCell(x + dx, y + dy);
		}
		for (int i = 0; i < radius - 1; ++i) {
			// right upwards
			++dx;
			--dy;
			addCell(x + dx, y + dy);
		}
	}

	public Cell getCenter() {
		return center;
	}

	public int getRadius() {
		return radius;
	}
}
