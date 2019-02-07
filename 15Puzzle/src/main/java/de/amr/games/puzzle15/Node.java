package de.amr.games.puzzle15;

public class Node {

	private Puzzle15 puzzle;
	private Dir dir;
	private Node parent;
	private int score;

	public Node(Puzzle15 puzzle, Dir dir, Node parent) {
		this.puzzle = puzzle;
		this.dir = dir;
		this.parent = parent;
	}

	public Puzzle15 getPuzzle() {
		return puzzle;
	}

	public Dir getDir() {
		return dir;
	}

	public Node getParent() {
		return parent;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		if (dir != null) {
			sb.append("dir:").append(dir).append("\n");
		}
		sb.append("score:").append(score).append("\n");
		sb.append(puzzle);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dir == null) ? 0 : dir.hashCode());
		result = prime * result + ((puzzle == null) ? 0 : puzzle.hashCode());
		return result;
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
		if (dir != other.dir)
			return false;
		if (puzzle == null) {
			if (other.puzzle != null)
				return false;
		} else if (!puzzle.equals(other.puzzle))
			return false;
		return true;
	}
}