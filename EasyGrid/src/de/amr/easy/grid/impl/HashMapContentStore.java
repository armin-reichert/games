package de.amr.easy.grid.impl;

import java.util.HashMap;
import java.util.Map;

import de.amr.easy.grid.api.GridContentStore;

class HashMapContentStore<Cell, Content> implements GridContentStore<Cell, Content> {

	private final Map<Cell, Content> contentMap = new HashMap<>();
	private Content defaultContent;

	public HashMapContentStore() {
	}

	@Override
	public Content getContent(Cell cell) {
		Content content = contentMap.get(cell);
		return content != null ? content : defaultContent;
	}

	@Override
	public void setContent(Cell cell, Content content) {
		contentMap.put(cell, content);
	}

	@Override
	public void clearContent() {
		contentMap.clear();
	}

	@Override
	public void setDefaultContent(Content content) {
		defaultContent = content;
	}
}
