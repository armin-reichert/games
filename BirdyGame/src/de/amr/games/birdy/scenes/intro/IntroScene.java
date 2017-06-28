package de.amr.games.birdy.scenes.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import de.amr.easy.game.common.PumpingImage;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.scenes.start.StartScene;

/**
 * Intro scene.
 * 
 * @author Armin Reichert
 */
public class IntroScene extends Scene<BirdyGame> {

	private class IntroSceneControl extends StateMachine<String, String> {

		public IntroSceneControl() {
			super("Intro Scene Control", String.class, "MovingText");
			state("MovingText").entry = s -> textY = getHeight();
			state("MovingText").update = s -> textY -= textSpeed;
			change("MovingText", "Waiting", this::endPositionReached);
			state("Waiting").entry = s -> s.setDuration(app.motor.secToTicks(2));
			changeOnTimeout("Waiting", "StartingGame");
			state("StartingGame").entry = s -> s.setDuration(app.motor.secToTicks(3));
			state("StartingGame").exit = s -> app.views.show(StartScene.class);
			changeOnTimeout("StartingGame", "ExitScene");
		}

		private boolean endPositionReached() {
			return textBounds != null && textY < (getHeight() - textBounds.getHeight()) / 2;
		}
	}

	private final IntroSceneControl control = new IntroSceneControl();

	private String[] textLines;
	private float lineSpacing = 1.5f;
	private Color textColor;
	private Font textFont;
	private float textY;
	private float textSpeed = .75f;
	private Rectangle2D textBounds;

	private City city;
	private PumpingImage titleImage;

	public IntroScene(BirdyGame app) {
		super(app);
		textFont = new Font("Sans", Font.PLAIN, 40);
		textColor = Color.WHITE;
	}

	public void setText(String text) {
		this.textLines = text.split("\n");
	}

	public void setTextFont(Font textFont) {
		this.textFont = textFont;
	}

	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	@Override
	public void init() {
		city = app.entities.findAny(City.class);
		city.setWidth(getWidth());
		city.setNight(new Random().nextBoolean());

		titleImage = new PumpingImage(app.assets.image("title"));
		titleImage.setScale(3);

		setText("Anna proudly presents" + "\nin cooperation with" + "\nProf. Zwickmann" + "\nGeräteschuppen Software 2017");
		setTextFont(app.assets.font("Pacifico-Regular"));
		setTextColor(city.isNight() ? Color.WHITE : Color.DARK_GRAY);

		control.init();
	}

	@Override
	public void update() {
		control.update();
	}

	@Override
	public void draw(Graphics2D pen) {
		city.draw(pen);
		if (control.is("MovingText", "Waiting")) {
			drawTextLines(pen);
		} else if (control.stateID() == "StartingGame") {
			titleImage.center(getWidth(), getHeight());
			titleImage.draw(pen);
		}
	}

	private void drawTextLines(Graphics2D pen) {
		pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		pen.setFont(textFont);
		pen.setColor(textColor);
		float y = textY;
		double textHeight = 0;
		double textWidth = 0;
		for (String line : textLines) {
			Rectangle2D lineBounds = pen.getFontMetrics().getStringBounds(line, pen);
			float x = (float) (getWidth() - lineBounds.getWidth()) / 2;
			pen.drawString(line, x, y);
			y += lineSpacing * lineBounds.getHeight();
			textHeight += lineBounds.getHeight();
			textWidth = Math.max(textWidth, lineBounds.getWidth());
		}
		textBounds = new Rectangle2D.Double(0, 0, textWidth, textHeight);
	}
}