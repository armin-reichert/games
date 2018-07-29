package de.amr.easy.game.tests;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.scene.ActiveScene;

public class DrawTestScene implements ActiveScene<Graphics2D> {

	private DrawTestApp app;
	private BufferedImage drawArea;
	private Graphics2D pen;
	private int penWidth;
	private boolean randomColor;

	public DrawTestScene(DrawTestApp app) {
		this.app = app;
	}

	@Override
	public int getWidth() {
		return app.getWidth();
	}

	@Override
	public int getHeight() {
		return app.getHeight();
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(drawArea, 0, 0, null);
	}

	@Override
	public void init() {
		drawArea = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		pen = drawArea.createGraphics();
		penWidth = 4;
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			pen.setColor(Color.BLACK);
			pen.clearRect(0, 0, getWidth(), getHeight());
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_PLUS)) {
			if (penWidth < 60)
				penWidth *= 2;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_MINUS)) {
			if (penWidth > 4)
				penWidth /= 2;
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_R)) {
			randomColor = !randomColor;
		}
		if (Mouse.pressed() || Mouse.dragged()) {
			pen.setColor(randomColor ? randomColor() : Color.RED);
			pen.fillOval(Mouse.getX(), Mouse.getY(), penWidth, penWidth);
		}
	}

	private Color randomColor() {
		Random rand = new Random();
		return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
	}

}
