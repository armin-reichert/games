package de.amr.easy.statemachine;

/**
 * Action which may be attached to a transition triggered by some input / event.
 * 
 * @author Armin Reichert
 *
 * @param <I>
 *          input / event type
 */
@FunctionalInterface
public interface InputTransitionAction<I> extends TransitionAction {

	/**
	 * Performs this action when transition from state s to state t and input/event input occurs.
	 * 
	 * @param input
	 *          input / event that triggered this transition
	 * @param s
	 *          source state
	 * @param t
	 *          target state
	 */
	void accept(I input, State s, State t);
}
