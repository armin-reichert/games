package de.amr.easy.game.view;

import java.util.HashSet;
import java.util.Set;

import de.amr.easy.game.Application;

/**
 * Stores the views of an application and provides methods for finding and selecting views.
 * 
 * @author Armin Reichert
 */
public class ViewManager {

	private final Set<View> views;
	private final View defaultView;
	private View selected;

	/**
	 * Creates a new view manager.
	 * 
	 * @param defaultView
	 *          view to be used as default view
	 */
	public ViewManager(View defaultView) {
		views = new HashSet<>();
		this.defaultView = defaultView;
		selected = defaultView;
	}

	/**
	 * Returns the default view which is displayed in case no view has been created so far or no view
	 * is selected.
	 * 
	 * @return the default view
	 */
	public View getDefaultView() {
		return defaultView;
	}

	/**
	 * Adds a view to the set of views.
	 * 
	 * @param view
	 *          view to be added
	 * @return view that was added
	 */
	public <V extends View> V add(V view) {
		if (view == null) {
			throw new IllegalArgumentException("Cannot add null view");
		}
		views.add(view);
		return view;
	}

	/**
	 * Finds a view by its class. Only one view of any class should be added.
	 * 
	 * @param viewClass
	 *          class of view to be found
	 * @return view of given class
	 */
	@SuppressWarnings("unchecked")
	public <V extends View> V find(Class<V> viewClass) {
		for (View view : views) {
			if (viewClass.isAssignableFrom(view.getClass())) {
				return (V) view;
			}
		}
		throw new IllegalArgumentException("No view with class '" + viewClass.getName() + "' exists");
	}

	/**
	 * The current view.
	 * 
	 * @return the current view
	 */
	@SuppressWarnings("unchecked")
	public <V extends View> V current() {
		return (V) selected;
	}

	/**
	 * Selects the view of the given class as the current view.
	 * 
	 * @param viewClass
	 *          class of view to be selected
	 */
	public <V extends View> void select(Class<V> viewClass) {
		select(find(viewClass));
	}

	/**
	 * Selects the given view and displays it.
	 * 
	 * @param view
	 *          the view to be displayed
	 */
	public void select(View view) {
		selected = view == null ? defaultView : view;
		selected.init();
		views.add(selected);
		Application.LOG.info("Current view: " + selected);
	}
}