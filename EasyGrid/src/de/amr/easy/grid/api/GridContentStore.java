package de.amr.easy.grid.api;

/**
 * Interface for accessing content stored in a grid.
 *
 * @param <Cell>
 *          the grid cell type
 * @param <Content>
 *          the grid cell content type
 */
public interface GridContentStore<Cell, Content> {

	public Content getContent(Cell cell);

	public void setContent(Cell cell, Content content);

	public void clearContent();

	public void setDefaultContent(Content content);

}
