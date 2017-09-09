package de.amr.games.birdy.play.scenes;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.birdy.play.scenes.IntroScene.State.Finished;
import static de.amr.games.birdy.play.scenes.IntroScene.State.ShowCredits;
import static de.amr.games.birdy.play.scenes.IntroScene.State.ShowGameTitle;
import static de.amr.games.birdy.play.scenes.IntroScene.State.Wait;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.controls.PumpingImage;
import de.amr.easy.game.controls.TextArea;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.play.BirdyGame;

/**
 * Intro scene.
 * 
 * @author Armin Reichert
 */
public class IntroScene extends Scene<BirdyGame> {

	private static final String CREDITS_TEXT = "Anna proudly presents\nin cooperation with\nProf. Zwickmann\nGeräteschuppen Software 2017";

	public enum State {
		ShowCredits, Wait, ShowGameTitle, Finished
	}

	private final StateMachine<State, Object> control;
	private City city;
	private PumpingImage gameTitleImage;
	private TextArea creditsText;

	public IntroScene(BirdyGame app) {
		super(app);

		control = new StateMachine<>("Intro Scene Control", State.class, ShowCredits);

		// ShowCredits
		control.state(ShowCredits).entry = s -> {
			creditsText.tf.setY(getHeight());
			creditsText.setScrollSpeed(-.75f);
			Assets.sound("music/bgmusic.mp3").loop();
		};

		control.state(ShowCredits).update = s -> creditsText.update();

		control.change(ShowCredits, Wait, () -> creditsText.tf.getY() < (getHeight() - creditsText.getHeight()) / 2,
				t -> t.to().setDuration(app.pulse.secToTicks(2)));

		// Wait
		control.changeOnTimeout(Wait, ShowGameTitle, t -> t.to().setDuration(app.pulse.secToTicks(3)));

		// ShowGameTitle
		control.changeOnTimeout(ShowGameTitle, Finished);

		control.state(ShowGameTitle).exit = s -> app.selectView(StartScene.class);
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

		gameTitleImage = new PumpingImage(Assets.image("title"));
		gameTitleImage.setScale(3);
		gameTitleImage.visibility = () -> control.is(ShowGameTitle);

		creditsText = new TextArea(CREDITS_TEXT);
		creditsText.setFont(Assets.font("Pacifico-Regular"));
		creditsText.setColor(city.isNight() ? Color.WHITE : Color.DARK_GRAY);
		creditsText.visibility = () -> control.is(ShowCredits, Wait);

		control.setLogger(LOG);
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