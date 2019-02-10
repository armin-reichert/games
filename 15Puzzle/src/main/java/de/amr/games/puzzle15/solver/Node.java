package de.amr.games.puzzle15.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.amr.games.puzzle15.model.Dir;
import de.amr.games.puzzle15.model.Puzzle15;

public class Node {

	// key for equality of nodes
	private final Puzzle15 puzzle;

	// not used in equality test:
	private Dir dir;
	private Node parent;
	private int distFromSource;
	private int score;

	public Node(Puzzle15 puzzle) {
		this.puzzle = puzzle;
	}

	public List<Node> successors() {
		List<Node> successors = new ArrayList<>(4);
		for (Dir dir : puzzle.possibleMoveDirs()) {
			Node successor = new Node(puzzle.move(dir));
			successor.dir = dir;
			successor.parent = this;
			successor.distFromSource = distFromSource + 1;
			successors.add(successor);
		}
		return successors;
	}

	public Puzzle15 getPuzzle() {
		return puzzle;
	}

	public Dir getDir() {
		return dir;
	}

	public void setDir(Dir dir) {
		this.dir = dir;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public int getDistFromSource() {
		return distFromSource;
	}

	public void setDistFromSource(int distFromSource) {
		this.distFromSource = distFromSource;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		if (dir != null) {
			sb.append("dir:").append(dir).append("\n");
		}
		sb.append("distFromSource (g): ").append(distFromSource).append("\n");
		sb.append("score (f): ").append(score).append("\n");
		sb.append(puzzle);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(puzzle);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		return Objects.equals(puzzle, other.puzzle);
	}
}