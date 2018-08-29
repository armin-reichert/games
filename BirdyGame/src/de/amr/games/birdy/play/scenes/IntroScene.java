package de.amr.games.birdy.play.scenes;

import static de.amr.easy.game.Application.PULSE;
import static de.amr.games.birdy.play.scenes.IntroScene.State.Finished;
import static de.amr.games.birdy.play.scenes.IntroScene.State.ShowCredits;
import static de.amr.games.birdy.play.scenes.IntroScene.State.ShowGameTitle;
import static de.amr.games.birdy.play.scenes.IntroScene.State.Wait;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.controls.PumpingImage;
import de.amr.easy.game.controls.TextArea;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGameApp;
import de.amr.games.birdy.entities.City;

/**
 * Intro scene.
 * 
 * @author Armin Reichert
 */
public class IntroScene implements View,Controller {

	private static final String CREDITS_TEXT = "Anna proudly presents\nin cooperation with\nProf. Zwickmann\nGeräteschuppen Software 2017";

	public enum State {
		ShowCredits, Wait, ShowGameTitle, Finished
	}

	private final BirdyGameApp app;
	private final int width;
	private final int height;
	private final StateMachine<State, Object> control;
	private City city;

	private PumpingImage logoAnimation;
	private TextArea textAnimation;

	public IntroScene(BirdyGameApp app) {
		this.app = app;
		this.width = app.settings.width;
		this.height = app.settings.height;

		control = new StateMachine<>("Intro Scene Control", State.class, ShowCredits);

		// ShowCredits
		control.state(ShowCredits).entry = s -> {
			textAnimation.tf().setY(height);
			textAnimation.setScrollSpeed(-.75f);
			logoAnimation.setVisible(false);
			Assets.sound("music/bgmusic.mp3").loop();
		};

		control.state(ShowCredits).update = s -> textAnimation.update();

		control.change(ShowCredits, Wait,
				() -> textAnimation.tf().getY() < (height - textAnimation.getHeight()) / 2,
				t -> t.to().setDuration(PULSE.secToTicks(2)));

		// Wait
		control.state(Wait).entry = s -> {
			logoAnimation.setVisible(true);
			textAnimation.setVisible(false);
		};

		control.changeOnTimeout(Wait, ShowGameTitle, t -> t.to().setDuration(PULSE.secToTicks(3)));

		// ShowGameTitle
		control.changeOnTimeout(ShowGameTitle, Finished);

		control.state(ShowGameTitle).exit = s -> app.setController(app.getStartScene());
	}

	@Override
	public void init() {
		city = app.entities.ofClass(City.class).findAny().get();
		city.setWidth(width);
		if (new Random().nextBoolean()) {
			city.letSunGoDown();
		} else {
			city.letSunGoUp();
		}

		logoAnimation = new PumpingImage(Assets.image("title"));
		logoAnimation.setScale(3);
		logoAnimation.center(width, height);

		textAnimation = new TextArea(CREDITS_TEXT);
		textAnimation.setFont(Assets.font("Pacifico-Regular"));
		textAnimation.setColor(city.isNight() ? Color.WHITE : Color.DARK_GRAY);
		textAnimation.centerHorizontally(width);

		control.setLogger(Application.LOGGER);
		control.init();
	}

	@Override
	public void update() {
		control.update();
	}

	@Override
	public void draw(Graphics2D g) {
		logoAnimation.center(width, height);
		Stream.of(city, logoAnimation, textAnimation).forEach(e -> e.draw(g));
	}
}