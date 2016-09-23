package de.amr.easy.grid.impl;

import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Grid2D;
import de.amr.easy.grid.api.GridContentStore;

/**
 * A grid with cell content.
 * 
 * @author Armin Reichert
 */
public class DataGrid<Content> extends RawGrid implements GridContentStore<Integer, Content> {

	private GridContentStore<Integer, Content> contentStore;

	public DataGrid(int numCols, int numRows, Content defaultContent, boolean sparse) {
		super(numCols, numRows);
		contentStore = sparse ? new HashMapContentStore<>() : new ArrayContentStore<>(numCols * numRows);
		contentStore.setDefaultContent(defaultContent);
	}

	public DataGrid(int numCols, int numRows, Content defaultContent) {
		this(numCols, numRows, defaultContent, true);
	}

	// --- {@link GridContentStore} interface ---

	@Override
	public void clearContent() {
		contentStore.clearContent();
	}

	@Override
	public void setDefaultContent(Content content) {
		contentStore.setDefaultContent(content);
	}

	@Override
	public Content getContent(Integer cell) {
		return contentStore.getContent(cell);
	}

	@Override
	public void setContent(Integer cell, Content content) {
		contentStore.setContent(cell, content);
	}
}
