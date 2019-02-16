package de.amr.games.puzzle15.solver;

import java.util.Objects;
import java.util.stream.Stream;

import de.amr.games.puzzle15.model.Dir;
import de.amr.games.puzzle15.model.Puzzle15;

public class Node {

	private final Puzzle15 puzzle;

	// not used in equality test:
	private Dir dir;
	private Node parent;
	private int movesSoFar;
	private int score;
	private int depth;

	public Node(Puzzle15 puzzle) {
		this.puzzle = puzzle;
	}

	public Stream<Node> successors() {
		return puzzle.possibleMoveDirs().map(dir -> {
			Node successor = new Node(puzzle.move(dir));
			successor.dir = dir;
			successor.parent = this;
			successor.movesSoFar = movesSoFar + 1;
			return successor;
		});
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

	public int getMovesSoFar() {
		return movesSoFar;
	}

	public void setMovesSoFar(int n) {
		this.movesSoFar = n;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		if (dir != null) {
			sb.append("dir:").append(dir).append("\n");
		}
		sb.append("movesSoFar ('g'): ").append(movesSoFar).append("\n");
		sb.append("score      ('f'): ").append(score).append("\n");
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