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
import java.util.function.IntSupplier;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.widgets.PumpingImageWidget;
import de.amr.easy.game.ui.widgets.TextWidget;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.birdy.BirdyGameApp;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.play.scenes.IntroScene.State;
import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

/**
 * Intro scene.
 * 
 * @author Armin Reichert
 */
public class IntroScene extends StateMachine<State, Void> implements View, Controller {

	public enum State {
		CREDITS, WAITING, LOGO, COMPLETE
	}

	private static final String CREDITS_TEXT = String.join("\n",
	/*@formatter:off*/
			"Anna proudly presents", 
			"in cooperation with",
			"Prof. Zwickmann", 
			"Geräteschuppen Software 2017"
	/*@formatter:on*/
	);

	private City city;
	private PumpingImageWidget logoImage;
	private TextWidget creditsText;

	public IntroScene(BirdyGameApp app) {
		super(State.class, Match.BY_EQUALITY);
		/*@formatter:off*/
		beginStateMachine()
				.description("Intro Scene")
				.initialState(CREDITS)
				.states()

					.state(CREDITS)
						.onEntry(() -> creditsText.startAnimation())
						.onTick(() -> creditsText.update())
						.onExit(() -> creditsText.stopAnimation())

					.state(WAITING)
						.timeoutAfter(sec(2))
						.onExit(() -> creditsText.setVisible(false))
						
					.state(LOGO)
						.timeoutAfter(sec(4)) 
						.onEntry(() -> logoImage.setVisible(true))
						.onExit(() -> app().setController(app.getStartScene()))
						
				.transitions()
					.when(CREDITS).then(WAITING).condition(() -> creditsText.isAnimationCompleted())
					.when(WAITING).then(LOGO).onTimeout()
					.when(LOGO).then(COMPLETE).onTimeout()
				
		.endStateMachine();
		/*@formatter:on*/
		traceTo(LOGGER, app().clock::getFrequency);
	}

	private IntSupplier sec(float amount) {
		return () -> app().clock.sec(amount);
	}

	@Override
	public void init() {
		int width = app().settings.width, height = app().settings.height;

		city = new City();
		city.setWidth(width);
		if (new Random().nextBoolean()) {
			city.sunset();
		} else {
			city.sunrise();
		}

		creditsText = TextWidget.create().text(CREDITS_TEXT).font(Assets.font("Pacifico-Regular"))
				.color(city.isNight() ? Color.WHITE : Color.DARK_GRAY).build();
		creditsText.tf.centerX(width);
		creditsText.tf.setY(height);
		creditsText.tf.setVelocityY(-1.5f);
		creditsText.setCompletion(() -> creditsText.tf.getY() < height / 4);

		logoImage = PumpingImageWidget.create().image(Assets.image("title")).scale(3).build();
		logoImage.tf.center(width, height);
		logoImage.setVisible(false);

		super.init();
		Assets.sound("music/bgmusic.mp3").loop();
	}

	@Override
	public void draw(Graphics2D g) {
		Stream.of(city, logoImage, creditsText).forEach(e -> e.draw(g));
	}
}