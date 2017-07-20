package de.amr.easy.statemachine;

/**
 * Action which can be attached to a transition without input / event.
 * 
 * @author Armin Reichert
 */
@FunctionalInterface
public interface SimpleTransitionAction extends TransitionAction {

	/**
	 * Performs this action when transition from state s to state t occurs.
	 * 
	 * @param s
	 *          source state
	 * @param t
	 *          target state
	 */
	void accept(State s, State t);
}