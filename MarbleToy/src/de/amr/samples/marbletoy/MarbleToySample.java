package de.amr.samples.marbletoy;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.amr.samples.marbletoy.entities.MarbleToy;
import de.amr.samples.marbletoy.fsm.LeverControl;
import de.amr.samples.marbletoy.fsm.LeverControlMealyMachine;

/**
 * Exercise 2.3 from Hopcroft/Ullman: "Introduction to Automata Theory, Languages and Computation".
 */
public class MarbleToySample {

	public static void main(String[] args) {
		MarbleToySample sample = new MarbleToySample(args.length == 0 ? 4 : Integer.parseInt(args[0]));
		System.out.println(sample.leverControlFA.toGraphViz());
	}

	private final Set<String> inputs;
	private final LeverControlMealyMachine mealy = new LeverControlMealyMachine();
	private final LeverControl leverControlFA = new LeverControl(new MarbleToy());
	private final Set<String> acceptedByMealy = new LinkedHashSet<>();
	private final Set<String> acceptedByFA = new LinkedHashSet<>();

	public MarbleToySample(int maxInputLength) {
		inputs = createWordsIncludingLength(maxInputLength, 'A', 'B');
		for (String input : inputs) {
			if (mealy.accepts(input)) {
				acceptedByMealy.add(input);
			}
			if (leverControlFA.accepts(input)) {
				acceptedByFA.add(input);
			}
		}
		for (String input : inputs) {
			System.out.print(input);
			System.out.print(" ");
			System.out.print(acceptedByMealy.contains(input));
			System.out.print(" ");
			System.out.print(acceptedByFA.contains(input));
			if (acceptedByMealy.contains(input) != acceptedByFA.contains(input)) {
				System.out.print(" ERROR!");
			}
			System.out.println();
		}
	}

	private static Set<String> createWordsIncludingLength(int n, char... alphabet) {
		Set<String> words = new LinkedHashSet<>();
		List<String> w1 = new ArrayList<>();
		List<String> w2;
		w1.add("");
		for (int i = 1; i <= n; ++i) {
			w2 = new ArrayList<>();
			for (int k = 0; k < w1.size(); ++k) {
				String word = w1.get(k);
				for (char c : alphabet) {
					w2.add(word + c);
				}
			}
			words.addAll(w2);
			w1 = w2;
		}
		return words;
	}
}