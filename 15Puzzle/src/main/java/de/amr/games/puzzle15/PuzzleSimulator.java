package de.amr.games.puzzle15;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class PuzzleSimulator extends JFrame {

	public static void main(String[] args) {
		EventQueue.invokeLater(PuzzleSimulator::new);
	}

	private Puzzle puzzle;
	private PuzzleView view;

	public PuzzleSimulator() {
		puzzle = new Puzzle(4);
		view = new PuzzleView(puzzle, 100);
		setTitle("15-Puzzle");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		add(view);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

}
