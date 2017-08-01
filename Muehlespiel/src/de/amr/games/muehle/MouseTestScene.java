package de.amr.games.muehle;

import static de.amr.easy.game.Application.LOG;
import static java.lang.String.format;

import java.awt.Color;

import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.scene.Scene;

public class MouseTestScene extends Scene<MillApp> {

	public MouseTestScene(MillApp app) {
		super(app);
		setBgColor(Color.WHITE);
	}

	@Override
	public void update() {

		if (Mouse.clicked()) {
			LOG.info(format("Mouse clicked at (%d, %d), %s button", Mouse.getX(), Mouse.getY(), whichMouseButton()));
		}
		if (Mouse.pressed()) {
			LOG.info(format("Mouse pressed at (%d, %d), %s button", Mouse.getX(), Mouse.getY(), whichMouseButton()));
		}
		if (Mouse.released()) {
			LOG.info(format("Mouse released at (%d, %d), %s button", Mouse.getX(), Mouse.getY(), whichMouseButton()));
		}
	}

	private String whichMouseButton() {
		if (Mouse.isLeftButton())
			return "left";
		else if (Mouse.isMiddleButton())
			return "middle";
		else if (Mouse.isRightButton())
			return "right";
		else
			return "no";
	}
}
