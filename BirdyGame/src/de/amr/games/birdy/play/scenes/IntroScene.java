package de.amr.games.birdy.play.scenes;

import static de.amr.easy.game.Application.PULSE;
import static de.amr.games.birdy.play.scenes.IntroScene.State.COMPLETE;
import static de.amr.games.birdy.play.scenes.IntroScene.State.CREDITS;
import static de.amr.games.birdy.play.scenes.IntroScene.State.TITLE;
import static de.amr.games.birdy.play.scenes.IntroScene.State.WAITING;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.controls.PumpingImage;
import de.amr.easy.game.controls.TextArea;
import de.amr.easy.game.entity.EntityMap;
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
public class IntroScene implements View, Controller {

	private static final String CREDITS_TEXT = "Anna proudly presents" + "\nin cooperation with" + "\nProf. Zwickmann"
			+ "\nGeräteschuppen Software 2017";

	public enum State {
		CREDITS, WAITING, TITLE, COMPLETE
	}

	private final int width;
	private final int height;
	private final StateMachine<State, Object> fsm;

	private final EntityMap entities;
	private City city;
	private PumpingImage logoAnimation;
	private TextArea textAnimation;

	public IntroScene(BirdyGameApp app) {

		this.width = app.settings.width;
		this.height = app.settings.height;
		this.entities = app.entities;

		fsm = new StateMachine<>("Intro Scene", State.class, CREDITS);

		// ShowCredits
		fsm.state(CREDITS).entry = s -> {
			textAnimation.tf.setY(height);
			textAnimation.setSpeedY(-.75f);
			logoAnimation.setVisible(false);
			Assets.sound("music/bgmusic.mp3").loop();
		};

		fsm.state(CREDITS).update = s -> textAnimation.update();

		fsm.change(CREDITS, WAITING, () -> textAnimation.tf.getY() < (height - textAnimation.getHeight()) / 2,
				t -> t.to().setDuration(PULSE.secToTicks(2)));

		// Wait
		fsm.state(WAITING).entry = s -> {
			logoAnimation.setVisible(true);
			textAnimation.setVisible(false);
		};

		fsm.changeOnTimeout(WAITING, TITLE, t -> t.to().setDuration(PULSE.secToTicks(3)));

		// ShowGameTitle
		fsm.changeOnTimeout(TITLE, COMPLETE);

		fsm.state(TITLE).exit = s -> app.setController(app.getStartScene());
	}

	@Override
	public void init() {
		city = entities.ofClass(City.class).findAny().get();
		city.setWidth(width);
		if (new Random().nextBoolean()) {
			city.letSunGoDown();
		} else {
			city.letSunGoUp();
		}

		logoAnimation = new PumpingImage(Assets.image("title"));
		logoAnimation.setScale(3);
		logoAnimation.center(width, height);

		textAnimation = TextArea.create().text(CREDITS_TEXT).font(Assets.font("Pacifico-Regular"))
				.color(city.isNight() ? Color.WHITE : Color.DARK_GRAY).build();
		textAnimation.centerHorizontally(width);

		fsm.setLogger(Application.LOGGER);
		fsm.init();
	}

	@Override
	public void update() {
		fsm.update();
	}

	@Override
	public void draw(Graphics2D g) {
		Stream.of(city, logoAnimation, textAnimation).forEach(e -> e.draw(g));
	}
}