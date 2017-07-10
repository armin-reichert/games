package de.amr.games.birdy.scenes.intro;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.birdy.scenes.intro.IntroScene.State.Finished;
import static de.amr.games.birdy.scenes.intro.IntroScene.State.ShowCredits;
import static de.amr.games.birdy.scenes.intro.IntroScene.State.ShowGameTitle;
import static de.amr.games.birdy.scenes.intro.IntroScene.State.WaitForShowGameTitle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.common.PumpingImage;
import de.amr.easy.game.common.ScrollingText;
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

	private static final String CREDITS_TEXT = "Anna proudly presents\nin cooperation with\nProf. Zwickmann\nGeräteschuppen Software 2017";

	public enum State {
		ShowCredits, WaitForShowGameTitle, ShowGameTitle, Finished
	}

	private final StateMachine<State, Object> control;

	private City city;
	private PumpingImage gameTitleImage;
	private ScrollingText creditsText;

	public IntroScene(BirdyGame app) {
		super(app);

		control = new StateMachine<>("Intro Scene Control", State.class, ShowCredits);
		control.setLogger(LOG);

		control.state(ShowCredits).entry = s -> {
			creditsText.tf.setY(getHeight());
			creditsText.setScrollSpeed(-.75f);
			app.assets.sound("music/bgmusic.mp3").loop();
		};

		control.state(ShowCredits).update = s -> creditsText.update();

		control.change(ShowCredits, WaitForShowGameTitle,
				() -> creditsText.tf.getY() < (getHeight() - creditsText.getHeight()) / 2,
				(s, t) -> t.setDuration(app.pulse.secToTicks(2)));

		control.changeOnTimeout(WaitForShowGameTitle, ShowGameTitle, (s, t) -> t.setDuration(app.pulse.secToTicks(3)));

		control.changeOnTimeout(ShowGameTitle, Finished);

		control.state(ShowGameTitle).exit = s -> app.views.show(StartScene.class);
	}

	@Override
	public void init() {
		city = app.entities.findAny(City.class);
		city.setWidth(getWidth());
		if (new Random().nextBoolean()) {
			city.letSunGoDown();
		} else {
			city.letSunGoUp();
		}

		gameTitleImage = new PumpingImage(app.assets.image("title"));
		gameTitleImage.setScale(3);
		gameTitleImage.visibility = () -> control.stateID() == ShowGameTitle;

		creditsText = new ScrollingText(CREDITS_TEXT);
		creditsText.setFont(app.assets.font("Pacifico-Regular"));
		creditsText.setColor(city.isNight() ? Color.WHITE : Color.DARK_GRAY);
		creditsText.visibility = () -> control.is(ShowCredits, WaitForShowGameTitle);

		control.init();
	}

	@Override
	public void update() {
		control.update();
	}

	@Override
	public void draw(Graphics2D pen) {
		creditsText.hCenter(getWidth());
		gameTitleImage.center(getWidth(), getHeight());
		Stream.of(city, gameTitleImage, creditsText).forEach(e -> e.draw(pen));
	}
}