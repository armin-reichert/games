package de.amr.games.birdy.play.scenes;

import static de.amr.easy.game.Application.CLOCK;
import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.birdy.play.scenes.IntroScene.State.COMPLETE;
import static de.amr.games.birdy.play.scenes.IntroScene.State.CREDITS;
import static de.amr.games.birdy.play.scenes.IntroScene.State.TITLE;
import static de.amr.games.birdy.play.scenes.IntroScene.State.WAITING;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.controls.PumpingImage;
import de.amr.easy.game.controls.TextArea;
import de.amr.easy.game.entity.EntityMap;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.birdy.BirdyGameApp;
import de.amr.games.birdy.entities.City;
import de.amr.statemachine.MatchStrategy;
import de.amr.statemachine.StateMachine;

/**
 * Intro scene.
 * 
 * @author Armin Reichert
 */
public class IntroScene implements View, Controller {

	private static final String CREDITS_TEXT = "Anna proudly presents" + "\nin cooperation with"
			+ "\nProf. Zwickmann" + "\nGeräteschuppen Software 2017";

	public enum State {
		CREDITS, WAITING, TITLE, COMPLETE
	}

	private final int width;
	private final int height;
	private final StateMachine<State, Void> fsm;

	private final EntityMap entities;
	private City city;
	private PumpingImage logoAnimation;
	private TextArea textAnimation;

	public IntroScene(BirdyGameApp app) {

		this.width = app.settings.width;
		this.height = app.settings.height;
		this.entities = app.entities;

		fsm = new StateMachine<>(State.class, MatchStrategy.BY_EQUALITY);
		fsm.setDescription("Intro Scene");
		fsm.setInitialState(CREDITS);

		// ShowCredits

		fsm.state(CREDITS).setOnEntry(() -> {
			textAnimation.tf.setY(height);
			textAnimation.setSpeedY(-.75f);
			logoAnimation.setVisible(false);
			Assets.sound("music/bgmusic.mp3").loop();
		});

		fsm.state(CREDITS).setOnTick(() -> textAnimation.update());

		fsm.addTransition(CREDITS, WAITING,
				() -> textAnimation.tf.getY() < (height - textAnimation.getHeight()) / 2, null);

		fsm.state(WAITING).setDuration(() -> CLOCK.sec(2));

		// Wait

		fsm.state(WAITING).setOnEntry(() -> {
			logoAnimation.setVisible(true);
			textAnimation.setVisible(false);
		});

		fsm.addTransition(WAITING, TITLE, null, null);

		// ShowGameTitle

		fsm.state(TITLE).setDuration(() -> CLOCK.sec(3));

		fsm.addTransitionOnTimeout(TITLE, COMPLETE, null, null);

		fsm.state(TITLE).setOnExit(() -> app.setController(app.getStartScene()));
	}

	@Override
	public void init() {
		city = entities.ofClass(City.class).findAny().get();
		city.setWidth(width);
		if (new Random().nextBoolean()) {
			city.sunset();
		} else {
			city.sunrise();
		}

		logoAnimation = PumpingImage.create().image(Assets.image("title")).scale(3).build();
		logoAnimation.tf.center(width, height);

		textAnimation = TextArea.create().text(CREDITS_TEXT).font(Assets.font("Pacifico-Regular"))
				.color(city.isNight() ? Color.WHITE : Color.DARK_GRAY).build();
		textAnimation.tf.centerX(width);

		fsm.traceTo(LOGGER, CLOCK::getFrequency);
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