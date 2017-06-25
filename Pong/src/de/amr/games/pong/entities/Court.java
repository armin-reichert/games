package de.amr.games.pong.entities;

import static de.amr.games.pong.PongGlobals.COURT_BACKGROUND;
import static de.amr.games.pong.PongGlobals.COURT_LINES_COLOR;
import static de.amr.games.pong.PongGlobals.COURT_LINE_WIDTH;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.pong.PongGame;

public class Court extends GameEntity {

	private final PongGame game;

	public Court(PongGame game) {
		this.game = game;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(COURT_BACKGROUND);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(COURT_LINES_COLOR);
		g.fillRect(0, 0, getWidth(), COURT_LINE_WIDTH);
		g.fillRect(0, getHeight() - COURT_LINE_WIDTH, getWidth(), COURT_LINE_WIDTH);
		g.fillRect(0, 0, COURT_LINE_WIDTH, getHeight());
		g.fillRect(getWidth() - COURT_LINE_WIDTH, 0, COURT_LINE_WIDTH, getHeight());
		g.fillRect(getWidth() / 2 - COURT_LINE_WIDTH / 2, 0, COURT_LINE_WIDTH, getHeight());
	}

	@Override
	public int getWidth() {
		return game.getWidth();
	}

	@Override
	public int getHeight() {
		return game.getHeight();
	}
}
