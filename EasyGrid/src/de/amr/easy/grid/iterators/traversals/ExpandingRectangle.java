package de.amr.easy.grid.iterators.traversals;

import java.util.Iterator;

import de.amr.easy.grid.iterators.shapes.Rectangle;

/**
 * Traverses the grid like an expanding rectangle.
 * 
 * @author Armin Reichert
 *
 * @param <Cell>
 *          the grid cell type
 */
public class ExpandingRectangle<Cell> implements Iterable<Cell> {

	private final Rectangle<Cell> startRectangle;
	private boolean expandHorizontally;
	private boolean expandVertically;
	private int maxExpansion;
	private int expansionRate;

	public ExpandingRectangle(Rectangle<Cell> startRectangle) {
		this.startRectangle = startRectangle;
		this.expandHorizontally = true;
		this.expandVertically = true;
		this.maxExpansion = 0;
		this.expansionRate = 1;
	}

	public void setMaxExpansion(int maxExpansion) {
		this.maxExpansion = maxExpansion;
	}

	public void setExpandHorizontally(boolean expandHorizontally) {
		this.expandHorizontally = expandHorizontally;
	}

	public void setExpandVertically(boolean expandVertically) {
		this.expandVertically = expandVertically;
	}

	public void setExpansionRate(int expansionRate) {
		this.expansionRate = expansionRate;
	}

	@Override
	public Iterator<Cell> iterator() {
		return new Iterator<Cell>() {

			private Rectangle<Cell> currentRectangle = startRectangle;
			private Iterator<Cell> iterator = currentRectangle.iterator();
			private int expansion;

			@Override
			public boolean hasNext() {
				return expansion < maxExpansion || iterator.hasNext();
			}

			@Override
			public Cell next() {
				if (!iterator.hasNext()) {
					int width = currentRectangle.getWidth() + (expandHorizontally ? expansionRate : 0);
					int height = currentRectangle.getHeight() + (expandVertically ? expansionRate : 0);
					expansion += expansionRate;
					currentRectangle = new Rectangle<Cell>(currentRectangle.getGrid(), currentRectangle.getLeftUpperCorner(),
							width, height);
					iterator = currentRectangle.iterator();
				}
				return iterator.next();
			}
		};
	}
}
