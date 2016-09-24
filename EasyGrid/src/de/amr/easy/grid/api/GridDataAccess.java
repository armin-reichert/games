package de.amr.easy.grid.api;

/**
 * Interface for accessing content stored in a grid.
 *
 * @param <Cell>
 *          grid cell type
 * @param <Content>
 *          grid cell content type
 */
public interface GridDataAccess<Cell, Content> {

	public Content get(Cell cell);

	public void set(Cell cell, Content data);

	public void clear();

	public void setDefault(Content data);
}