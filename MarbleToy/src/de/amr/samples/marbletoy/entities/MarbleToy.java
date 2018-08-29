package de.amr.samples.marbletoy.entities;

import static de.amr.samples.marbletoy.router.RoutingPoint.C;
import static de.amr.samples.marbletoy.router.RoutingPoint.D;
import static de.amr.samples.marbletoy.router.RoutingPoint.E;
import static de.amr.samples.marbletoy.router.RoutingPoint.F;
import static de.amr.samples.marbletoy.router.RoutingPoint.G;
import static de.amr.samples.marbletoy.router.RoutingPoint.H;
import static de.amr.samples.marbletoy.router.RoutingPoint.Initial;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumSet;
import java.util.Random;
import java.util.logging.Logger;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;
import de.amr.samples.marbletoy.fsm.LeverControl;
import de.amr.samples.marbletoy.router.MarbleRouter;
import de.amr.samples.marbletoy.router.RoutingPoint;

public class MarbleToy extends GameEntityUsingSprites {

	private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 18);

	public final Lever[] levers = new Lever[3];
	private final EnumSet<RoutingPoint> auxPoints = EnumSet.of(E, F, G, H);
	private final GameEntity marble;
	private LeverControl leverControl;
	private MarbleRouter router;

	public MarbleToy(Sprite sprite, Marble marble) {
		addSprite("s_toy", sprite);
		setCurrentSprite("s_toy");
		this.marble = marble;
		marble.tf().moveTo(-marble.getWidth(), -marble.getHeight());
		levers[0] = new Lever(178, 82);
		levers[1] = new Lever(424, 82);
		levers[2] = new Lever(301, 204);
		router = new MarbleRouter(this);
		router.setLogger(Logger.getGlobal());
	}

	public void setLeverControl(LeverControl leverControl) {
		this.leverControl = leverControl;
		leverControl.setLogger(Logger.getGlobal());
	}

	@Override
	public void init() {
		leverControl.init();
		router.init();
		router.addInput('A');
	}

	@Override
	public void update() {
		if (router.is(C, D)) {
			Character nextSlot = new Random().nextBoolean() ? 'A' : 'B';
			router.init();
			router.addInput(nextSlot);
			leverControl.addInput(nextSlot);
		}
		leverControl.update();
		router.update();
	}

	public GameEntity getMarble() {
		return marble;
	}

	public Lever getLever(int index) {
		return levers[index];
	}

	public boolean isLeverRoutingLeft(int index) {
		return leverControl.isRoutingLeft(index);
	}

	public void updateLevers() {
		for (int i = 0; i < levers.length; ++i) {
			levers[i].setPointsLeft(isLeverRoutingLeft(i));
		}
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		for (Lever lever : levers) {
			lever.draw(g);
		}
		drawRoutingPointLabels(g);
		marble.draw(g);
	}

	private void drawRoutingPointLabels(Graphics2D g) {
		g.setFont(LABEL_FONT);
		FontMetrics fm = g.getFontMetrics(LABEL_FONT);
		for (RoutingPoint rp : RoutingPoint.values()) {
			if (rp == Initial) {
				continue;
			}
			g.setColor(auxPoints.contains(rp) ? Color.GRAY : Color.BLACK);
			Rectangle2D bounds = fm.getStringBounds(rp.name(), g);
			g.drawString(rp.name(), rp.getLocation().roundedX() - (int) bounds.getWidth() / 2,
					rp.getLocation().roundedY() + fm.getDescent());
		}
	}
}