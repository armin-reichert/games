package de.amr.easy.game.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.ScrollingText;

/**
 * This view is displayed for an application if no view is selected.
 * 
 * @author Armin Reichert
 */
public class DefaultView implements View {

	private final Application app;
	private final ScrollingText text;

	public DefaultView(Application app) {
		this.app = app;
		text = new ScrollingText();
	}

	@Override
	public void init() {
		StringBuilder sb = new StringBuilder();
		sb.append(app.getClass().getSimpleName()).append("\n\n");
		sb.append("title = " + app.settings.title).append("\n");
		sb.append("width = " + app.settings.width).append("\n");
		sb.append("height = " + app.settings.height).append("\n");
		sb.append("scale = " + app.settings.scale).append("\n");
		sb.append("fullScreenMode = " + app.settings.fullScreenMode).append("\n");
		sb.append("bgColor = " + app.settings.bgColor).append("\n");
		app.settings.keys().forEach(key -> {
			sb.append(key + " = " + app.settings.getAsString(key)).append("\n");
		});
		text.setText(sb.toString());
		text.tf.setY(app.settings.height);
		text.setScrollSpeed(-0.5f);
		text.setColor(Color.WHITE);
		text.setFont(new Font("Monospaced", Font.BOLD, 16));
	}

	@Override
	public void update() {
		text.update();
		if (text.tf.getY() < -text.getHeight()) {
			text.tf.setY(app.settings.height);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, app.settings.width, app.settings.height);
		text.hCenter(app.settings.width);
		text.draw(g);
	}
}