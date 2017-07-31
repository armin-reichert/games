package de.amr.games.muehle.board;

import static java.lang.String.format;

public class Mill {

	public int p, q, r;
	public boolean horizontal;

	public Mill(int p, int q, int r, boolean horizontal) {
		this.p = p;
		this.q = q;
		this.r = r;
		this.horizontal = horizontal;
	}

	@Override
	public String toString() {
		return format("(%d %d %d) (%s)", p, q, r, horizontal ? "horizontal" : "vertikal");
	}
}