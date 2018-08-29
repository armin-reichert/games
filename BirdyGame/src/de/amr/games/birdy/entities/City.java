package de.amr.games.birdy.entities;

import static de.amr.easy.game.Application.PULSE;
import static de.amr.games.birdy.entities.City.CityEvent.SunGoesDown;
import static de.amr.games.birdy.entities.City.CityEvent.SunGoesUp;
import static de.amr.games.birdy.entities.City.CityState.Day;
import static de.amr.games.birdy.entities.City.CityState.Night;
import static de.amr.games.birdy.utils.Util.randomInt;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.stream.IntStream;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGameApp;

/**
 * The city shown in the background.
 * 
 * @author Armin Reichert
 */
public class City extends GameEntityUsingSprites {

	public enum CityState {
		Day, Night
	}

	public enum CityEvent {
		SunGoesDown, SunGoesUp
	}

	private final BirdyGameApp app;
	private final StateMachine<CityState, CityEvent> control;

	public City(BirdyGameApp app) {
		this.app = app;
		addSprite("s_night", new Sprite("bg_night"));
		addSprite("s_day", new Sprite("bg_day"));
		setCurrentSprite("s_day");
		tf.setWidth(currentSprite().getWidth());

		control = new StateMachine<>("City control", CityState.class, Day);

		control.state(Day).entry = s -> {
			setCurrentSprite("s_day");
		};

		control.changeOnInput(SunGoesDown, Day, Night);

		control.state(Night).entry = s -> {
			s.setDuration(PULSE.secToTicks(10));
			setCurrentSprite("s_night");
			replaceStars();
		};

		control.state(Night).update = s -> {
			app.entities.ofClass(Star.class).forEach(GameEntity::update);
		};

		control.state(Night).exit = s -> {
			app.entities.removeAll(Star.class);
		};

		control.changeOnTimeout(Night, Night, t -> {
			replaceStars();
			t.from().resetTimer();
		});

		control.changeOnInput(SunGoesUp, Night, Day);
	}

	@Override
	public void init() {
		control.init();
		control.setLogger(Application.LOGGER);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			letSunGoDown();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_D)) {
			letSunGoUp();
		}
		control.update();
	}

	private void replaceStars() {
		app.entities.removeAll(Star.class);
		int numStars = randomInt(1, app.settings.get("max stars"));
		IntStream.range(1, numStars).forEach(i -> {
			Star star = app.entities.store(new Star());
			star.tf().moveTo(randomInt(50, tf.getWidth() - 50), randomInt(100, 180));
		});
		Application.LOGGER.info("Created " + numStars + " new stars");
	}

	public boolean isNight() {
		return control.is(Night);
	}

	public void letSunGoDown() {
		control.addInput(SunGoesDown);
	}

	public void letSunGoUp() {
		control.addInput(SunGoesUp);
	}

	public void setWidth(int width) {
		if (tf.getWidth() != width) {
			tf.setWidth(width);
			getSprites().forEach(sprite -> {
				sprite.scale(width, sprite.getHeight());
			});
		}
	}

	@Override
	public void draw(Graphics2D pen) {
		pen.translate(tf.getX(), tf.getY());
		Image image = currentSprite().currentFrame();
		for (int x = 0; x < tf.getWidth(); x += image.getWidth(null)) {
			pen.drawImage(image, x, 0, null);
		}
		app.entities.ofClass(Star.class).forEach(star -> star.draw(pen));
		pen.translate(-tf.getX(), -tf.getY());
	}
}