package de.amr.games.pong.entities;

import static de.amr.games.pong.PongGlobals.FONT;
import static de.amr.games.pong.PongGlobals.SCORE_COLOR;

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
		g.drawString("" + scoreLeft.points, (int) tf.getX() - 100, 50);
		g.drawString("" + scoreRight.points, (int) tf.getX() + 100, 50);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}
