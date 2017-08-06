package de.amr.easy.game.tests;

import static java.lang.String.format;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.common.ScrollingText;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.scene.Scene;

public class MouseTestScene extends Scene<MouseTestApp> {

	private ScrollingText messageDisplay;

	public MouseTestScene(MouseTestApp app) {
		super(app);
		setBgColor(Color.WHITE);
	}

	@Override
	public void init() {
		messageDisplay = new ScrollingText();
	}

	@Override
	public void update() {

		if (Mouse.clicked() || Mouse.pressed() || Mouse.released() || Mouse.moved() || Mouse.dragged()) {
			messageDisplay.setText("");
		}
		if (Mouse.clicked()) {
			info(format("Mouse clicked at (%d, %d), %s button", Mouse.getX(), Mouse.getY(), whichMouseButton()));
		}
		if (Mouse.pressed()) {
			info(format("Mouse pressed at (%d, %d), %s button", Mouse.getX(), Mouse.getY(), whichMouseButton()));
		}
		if (Mouse.released()) {
			info(format("Mouse released at (%d, %d), %s button", Mouse.getX(), Mouse.getY(), whichMouseButton()));
		}
		if (Mouse.moved()) {
			info(format("Mouse moved to (%d, %d), %s button", Mouse.getX(), Mouse.getY(), whichMouseButton()));
		}
		if (Mouse.dragged()) {
			info(format("Mouse dragged to (%d, %d), %s button", Mouse.getX(), Mouse.getY(), whichMouseButton()));
		}
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		messageDisplay.center(getWidth(), getHeight());
		messageDisplay.draw(g);
		g.translate(Mouse.getX(), Mouse.getY());
		g.setColor(Color.BLACK);
		g.drawLine(-10, 0, 10, 0);
		g.drawLine(0, -10, 0, 10);
		g.translate(-Mouse.getX(), -Mouse.getY());
	}

	private void info(String text) {
		messageDisplay.setText(messageDisplay.getText() + "\n" + text);
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