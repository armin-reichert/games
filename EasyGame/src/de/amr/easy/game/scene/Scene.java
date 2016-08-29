package de.amr.easy.game.scene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.Application;
import de.amr.easy.game.view.View;

public abstract class Scene<App extends Application> implements View {

	private final App app;
	private Image bgImage;
	private Color bgColor;

	public Scene(App app) {
		this.app = app;
		bgColor = Color.BLACK;
	}

	public App getApp() {
		return app;
	}

	public int getWidth() {
		return app.getWidth();
	}

	public int getHeight() {
		return app.getHeight();
	}

	public Color getBgColor() {
		return bgColor;
	}

	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public Image getBgImage() {
		return bgImage;
	}

	public void setBgImage(Image bgImage) {
		this.bgImage = bgImage;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (bgImage != null) {
			g.drawImage(bgImage, 0, 0, bgImage.getWidth(null), bgImage.getHeight(null), null);
		}
	}
}
