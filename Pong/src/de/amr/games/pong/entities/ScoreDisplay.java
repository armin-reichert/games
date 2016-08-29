package de.amr.games.pong.entities;

import static de.amr.games.pong.Globals.FONT;
import static de.amr.games.pong.Globals.SCORE_COLOR;

import java.awt.Graphics2D;

import de.amr.easy.game.common.Score;
import de.amr.easy.game.entity.GameEntity;

public class ScoreDisplay extends GameEntity {

	private final Score scoreLeft;
	private final Score scoreRight;

	public ScoreDisplay(Score scoreLeft, Score scoreRight) {
		this.scoreLeft = scoreLeft;
		this.scoreRight = scoreRight;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(SCORE_COLOR);
		g.setFont(FONT);
		g.drawString("" + scoreLeft.points, (int) tr.getX() - 100, 50);
		g.drawString("" + scoreRight.points, (int) tr.getX() + 100, 50);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}
