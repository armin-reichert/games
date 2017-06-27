package de.amr.games.birdy.scenes.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import de.amr.easy.game.common.PumpingText;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.scenes.start.StartScene;

public class IntroScene extends Scene<BirdyGame> {

	private class IntroSceneControl extends StateMachine<String, String> {

		public IntroSceneControl() {
			super("IntroScene Control", String.class, "Scrolling");

			state("Scrolling").entry = s -> textY = getHeight();
			state("Scrolling").update = s -> textY -= 0.5f;
			change("Scrolling", "Waiting", () -> textY < getHeight() / 2);

			state("Waiting").entry = s -> s.setDuration(app.motor.secToTicks(1));
			changeOnTimeout("Waiting", "StartingGame");

			state("StartingGame").entry = s -> s.setDuration(app.motor.secToTicks(3));
			state("StartingGame").exit = s -> app.views.show(StartScene.class);
			changeOnTimeout("StartingGame", "ChangingScene");
		}
	}

	private final IntroSceneControl control;
	private Color textColor;
	private String[] textLines;
	private Font textFont;
	private float textY;
	private City city;
	private PumpingText flappyBirdText;

	public IntroScene(BirdyGame app) {
		super(app);
		control = new IntroSceneControl();
		textLines = "Anna proudly presents".split("\n");
		textFont = new Font("Sans", Font.PLAIN, 40);
		textColor = Color.WHITE;
	}

	@Override
	public void init() {
		app.assets.storeFont("Pacifico-Regular", "fonts/Pacifico-Regular.ttf", 40, Font.BOLD);
		city = app.entities.findAny(City.class);
		city.setWidth(getWidth());
		city.setNight(new Random().nextBoolean());
		textFont = app.assets.font("Pacifico-Regular");
		textColor = city.isNight() ? Color.WHITE : Color.DARK_GRAY;
		flappyBirdText = new PumpingText(app, "title", .25f);
		flappyBirdText.tr.setY(getHeight());
		control.init();
	}

	@Override
	public void update() {
		control.update();
	}

	@Override
	public void draw(Graphics2D pen) {
		city.draw(pen);
		if (control.stateID() == "Scrolling") {
			pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			pen.setFont(textFont);
			pen.setColor(textColor);
			float y = textY;
			for (int line = 0; line < textLines.length; ++line) {
				String text = textLines[line];
				Rectangle2D bounds = pen.getFontMetrics().getStringBounds(text, pen);
				float x = (float) (getWidth() / 2 - bounds.getWidth() / 2);
				pen.drawString(text, x, y);
				y += 2 * bounds.getHeight();
			}
		} else if (control.stateID() == "StartingGame") {
			flappyBirdText.center(getWidth(), getHeight());
			flappyBirdText.draw(pen);
		}
	}

	public void setText(String text) {
		this.textLines = text.split("\n");
	}

	public void setTextFont(Font textFont) {
		this.textFont = textFont;
	}

}
