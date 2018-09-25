package de.amr.games.birdy.play.scenes;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.birdy.play.scenes.IntroScene.State.COMPLETE;
import static de.amr.games.birdy.play.scenes.IntroScene.State.CREDITS;
import static de.amr.games.birdy.play.scenes.IntroScene.State.LOGO;
import static de.amr.games.birdy.play.scenes.IntroScene.State.WAITING;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.widgets.PumpingImageWidget;
import de.amr.easy.game.ui.widgets.TextWidget;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.birdy.BirdyGameApp;
import de.amr.games.birdy.entities.City;
import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

/**
 * Intro scene.
 * 
 * @author Armin Reichert
 */
public class IntroScene implements View, Controller {

	private static final String CREDITS_TEXT = String.join("\n", "Anna proudly presents", "in cooperation with",
			"Prof. Zwickmann", "Geräteschuppen Software 2017");

	public enum State {
		CREDITS, WAITING, LOGO, COMPLETE
	}

	private final int width;
	private final int height;
	private final StateMachine<State, Void> fsm;

	private City city;
	private PumpingImageWidget logo;
	private TextWidget credits;

	public IntroScene(BirdyGameApp app) {

		this.width = app.settings.width;
		this.height = app.settings.height;

		fsm = new StateMachine<>(State.class, Match.BY_EQUALITY);
		fsm.traceTo(LOGGER, app().clock::getFrequency);
		fsm.setDescription("Intro Scene");
		fsm.setInitialState(CREDITS);

		fsm.state(CREDITS).setOnEntry(() -> {
			credits.startAnimation();
			Assets.sound("music/bgmusic.mp3").loop();
		});

		fsm.state(CREDITS).setOnTick(() -> credits.update());
		fsm.addTransition(CREDITS, WAITING, () -> {
			return credits.isAnimationCompleted();
		}, e -> credits.stopAnimation());

		fsm.state(CREDITS).setOnExit(() -> {
			credits.stopAnimation();
		});

		fsm.state(WAITING).setTimer(() -> app().clock.sec(2));
		fsm.addTransitionOnTimeout(WAITING, LOGO, null, null);

		fsm.state(LOGO).setOnEntry(() -> {
			logo.setVisible(true);
			credits.setVisible(false);
		});
		fsm.state(LOGO).setTimer(() -> app().clock.sec(3));
		fsm.addTransitionOnTimeout(LOGO, COMPLETE, null, null);

		fsm.state(LOGO).setOnExit(() -> app.setController(app.getStartScene()));
	}

	@Override
	public void init() {
		city = new City();
		city.setWidth(width);
		if (new Random().nextBoolean()) {
			city.sunset();
		} else {
			city.sunrise();
		}

		credits = TextWidget.create().text(CREDITS_TEXT).font(Assets.font("Pacifico-Regular"))
				.color(city.isNight() ? Color.WHITE : Color.DARK_GRAY).build();
		credits.tf.centerX(width);
		credits.tf.setY(height);
		credits.tf.setVelocityY(-1.5f);
		credits.setCompletion(() -> credits.tf.getY() < height / 4);

		logo = PumpingImageWidget.create().image(Assets.image("title")).scale(3).build();
		logo.tf.center(width, height);
		logo.setVisible(false);

		fsm.init();
	}

	@Override
	public void update() {
		fsm.update();
	}

	@Override
	public void draw(Graphics2D g) {
		Stream.of(city, logo, credits).forEach(e -> e.draw(g));
	}
}