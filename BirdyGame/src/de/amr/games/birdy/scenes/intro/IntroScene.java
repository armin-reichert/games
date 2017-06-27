package de.amr.games.birdy.scenes.intro;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.scenes.start.StartScene;


public class IntroScene extends Scene<BirdyGame> {

	private final StateMachine<String, String> control;
	private String text;
	private Font textFont;
	private int textY;
	private City city;
	
	public IntroScene(BirdyGame app) {
		super(app);

		control = new StateMachine<>("IntroScene Control", String.class, "Running");
		
		control.state("Running").entry = s -> {
			s.setDuration(app.motor.secToTicks(5));
			textY = getHeight() / 2;
		};
		
		control.state("Running").update = s -> {
			textY -= 1;
			if (textY < 0) {
				textY = getHeight();
			}
		};
		
		control.changeOnTimeout("Running", "StartingGame");
		
		control.state("StartingGame").entry = s -> {
			app.views.show(StartScene.class);
		};
	}

	@Override
	public void init() {
		text = "Anna's Flappy Bird";
		textFont = new Font("Arial", Font.BOLD, 60);
		city = app.entities.findAny(City.class);
		city.setWidth(getWidth());
		city.setNight(new Random().nextBoolean());
		control.init();
	}
	
	@Override
	public void update() {
		control.update();
	}
	
	@Override
	public void draw(Graphics2D pen) {
		pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		pen.setFont(textFont);
		pen.setColor(Color.red);
		Rectangle2D bounds = pen.getFontMetrics().getStringBounds(text, pen);
		int x = (int) (getWidth() / 2 - bounds.getWidth() / 2);
		pen.drawString(text, x, textY);
	}

}
