package de.amr.easy.grid.impl;

import de.amr.easy.grid.api.GridContentStore;

class ArrayContentStore<Cell, Content> implements GridContentStore<Cell, Content> {

	private Object[] contentArray;
	private Object defaultContent;

	public ArrayContentStore(int size) {
		contentArray = new Object[size];
	}

	private Integer asInteger(Cell cell) {
		return (Integer) cell;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Content getContent(Cell cell) {
		Content content = (Content) contentArray[asInteger(cell)];
		return content != null ? content : (Content) defaultContent;
	}

	@Override
	public void setContent(Cell cell, Content content) {
		contentArray[asInteger(cell)] = content;
	}

	@Override
	public void clearContent() {
		contentArray = new Object[contentArray.length];
	}

	@Override
	public void setDefaultContent(Content content) {
		defaultContent = content;
	}
}