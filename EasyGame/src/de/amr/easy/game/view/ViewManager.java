package de.amr.easy.game.view;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.amr.easy.game.Application;

public class ViewManager {

	private final Set<View> views = new HashSet<>();
	private final Deque<View> viewStack = new LinkedList<>();
	private View defaultView;

	public View getDefaultView() {
		return defaultView;
	}

	public void setDefaultView(View defaultView) {
		this.defaultView = defaultView;
	}

	public <V extends View> V add(V view) {
		views.add(view);
		return view;
	}

	@SuppressWarnings("unchecked")
	public <V extends View> V find(Class<V> viewClass) {
		for (View view : views) {
			if (viewClass.isAssignableFrom(view.getClass())) {
				return (V) view;
			}
		}
		throw new IllegalArgumentException("No view with class '" + viewClass.getName() + "' exists");
	}

	@SuppressWarnings("unchecked")
	public <V extends View> V current() {
		View view = viewStack.peek();
		return (V) view;
	}

	public <V extends View> void show(Class<V> viewClass) {
		View view = find(viewClass);
		if (view == null) {
			view = defaultView;
		}
		if (!viewStack.isEmpty()) {
			viewStack.pop();
		}
		viewStack.push(view);
		view.init();
		Application.LOG.info("Current view: " + view);
	}

	public void show(View view) {
		if (!viewStack.isEmpty()) {
			viewStack.pop();
		}
		viewStack.push(view);
		view.init();
		Application.LOG.info("Current view: " + view);
	}
}