package de.amr.easy.grid.impl;

import java.util.HashSet;
import java.util.Set;

import de.amr.easy.graph.api.ObservableGraph;
import de.amr.easy.graph.event.GraphListener;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableGrid2D;

/**
 * A grid which can be observed by graph listeners.
 * 
 * @author Armin Reichert
 */
public class ObservableRawGrid extends RawGrid implements ObservableGrid2D<Integer, DefaultEdge<Integer>> {

	private final Set<GraphListener<Integer, DefaultEdge<Integer>>> listeners = new HashSet<>();
	private boolean eventsEnabled;

	public ObservableRawGrid(int numCols, int numRows) {
		super(numCols, numRows);
		eventsEnabled = true;
	}

	@Override
	public void addEdge(DefaultEdge<Integer> edge) {
		super.addEdge(edge);
		if (eventsEnabled) {
			for (GraphListener<Integer, DefaultEdge<Integer>> listener : listeners) {
				listener.edgeAdded(edge);
			}
		}
	}

	@Override
	public void removeEdge(DefaultEdge<Integer> edge) {
		super.removeEdge(edge);
		if (eventsEnabled) {
			for (GraphListener<Integer, DefaultEdge<Integer>> listener : listeners) {
				listener.edgeRemoved(edge);
			}
		}
	}

	@Override
	public void removeEdges() {
		super.removeEdges();
		if (eventsEnabled) {
			for (GraphListener<Integer, DefaultEdge<Integer>> listener : listeners) {
				listener.graphChanged(this);
			}
		}
	}

	/* {@link ObservableGraph} interface */

	@Override
	public void addGraphListener(GraphListener<Integer, DefaultEdge<Integer>> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeGraphListener(GraphListener<Integer, DefaultEdge<Integer>> listener) {
		listeners.remove(listener);
	}

	@Override
	public void setEventsEnabled(boolean enabled) {
		eventsEnabled = enabled;
	}

	@Override
	public void fireVertexChange(Integer vertex, Object oldValue, Object newValue) {
		if (eventsEnabled) {
			for (GraphListener<Integer, DefaultEdge<Integer>> listener : listeners) {
				listener.vertexChanged(vertex, oldValue, newValue);
			}
		}
	}

	@Override
	public void fireEdgeChange(DefaultEdge<Integer> event, Object oldValue, Object newValue) {
		if (eventsEnabled) {
			for (GraphListener<Integer, DefaultEdge<Integer>> listener : listeners) {
				listener.edgeChanged(event, oldValue, newValue);
			}
		}
	}

	@Override
	public void fireGraphChange(ObservableGraph<Integer, DefaultEdge<Integer>> graph) {
		if (eventsEnabled) {
			for (GraphListener<Integer, DefaultEdge<Integer>> listener : listeners) {
				listener.graphChanged(graph);
			}
		}
	}
}
