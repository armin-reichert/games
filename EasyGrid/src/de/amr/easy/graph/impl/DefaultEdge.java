package de.amr.easy.graph.impl;

import de.amr.easy.graph.api.Edge;

/**
 * Edge of a undirected graph.
 * 
 * @author Armin Reichert
 * 
 * @param <V>
 *          graph vertex type
 */
public class DefaultEdge<V> implements Edge<V> {

	protected V u;
	protected V v;

	public DefaultEdge(V u, V v) {
		this.u = u;
		this.v = v;
	}

	public void setEither(V v) {
		this.u = v;
	}

	public void setOther(V tail) {
		this.v = tail;
	}

	@Override
	public V either() {
		return u;
	}

	@Override
	public V other(V v) {
		if (v == this.v) {
			return this.u;
		}
		if (v == this.u) {
			return this.v;
		}
		throw new IllegalStateException();
	}

	@Override
	public String toString() {
		return "{" + u + "," + v + "}";
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof DefaultEdge<?>) {
			DefaultEdge<?> edge = (DefaultEdge<?>) o;
			return edge.u == this.u && edge.v == this.v;
		}
		return false;
	}
}