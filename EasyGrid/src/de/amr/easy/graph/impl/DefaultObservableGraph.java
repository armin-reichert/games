package de.amr.easy.graph.impl;

import java.util.HashSet;
import java.util.Set;

import de.amr.easy.graph.api.Edge;
import de.amr.easy.graph.api.ObservableGraph;
import de.amr.easy.graph.event.GraphListener;

/**
 * Adjacency set based implementation of an undirected, observable graph.
 * 
 * @author Armin Reichert
 * 
 * @param <V>
 *          vertex type
 * 
 * @param <E>
 *          edge type
 */
public class DefaultObservableGraph<V, E extends Edge<V>> extends DefaultGraph<V, E> implements ObservableGraph<V, E> {

	private Set<GraphListener<V, E>> listeners = new HashSet<>();
	private boolean listeningSuspended = false;

	public DefaultObservableGraph() {
		this.listeningSuspended = false;
	}

	@Override
	public void addGraphListener(GraphListener<V, E> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeGraphListener(GraphListener<V, E> listener) {
		listeners.remove(listener);
	}

	@Override
	public void setEventsEnabled(boolean enabled) {
		listeningSuspended = enabled;
	}

	@Override
	public void fireVertexChange(V vertex, Object oldValue, Object newValue) {
		if (!listeningSuspended) {
			for (GraphListener<V, E> listener : listeners) {
				listener.vertexChanged(vertex, oldValue, newValue);
			}
		}
	}

	@Override
	public void fireEdgeChange(E edge, Object oldValue, Object newValue) {
		if (!listeningSuspended) {
			for (GraphListener<V, E> listener : listeners) {
				listener.edgeChanged(edge, oldValue, newValue);
			}
		}
	}

	@Override
	public void fireGraphChange(ObservableGraph<V, E> graph) {
		if (!listeningSuspended) {
			for (GraphListener<V, E> listener : listeners) {
				listener.graphChanged(graph);
			}
		}
	}

	@Override
	public void addVertex(V vertex) {
		super.addVertex(vertex);
		fireVertexChange(vertex, null, vertex);
	}

	@Override
	public void addEdge(E edge) {
		super.addEdge(edge);
		fireEdgeChange(edge, null, edge);
	}

	@Override
	public void removeEdge(E edge) {
		super.removeEdge(edge);
		fireEdgeChange(edge, edge, null);
	}

	@Override
	public void removeEdges() {
		super.removeEdges();
		fireGraphChange(this);
	}
}
