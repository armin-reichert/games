package de.amr.games.birdy.entities;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.birdy.entities.City.CityEvent.SunGoesDown;
import static de.amr.games.birdy.entities.City.CityEvent.SunGoesUp;
import static de.amr.games.birdy.entities.City.CityState.Day;
import static de.amr.games.birdy.entities.City.CityState.Night;
import static de.amr.games.birdy.utils.Util.randomInt;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.stream.IntStream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.play.BirdyGame;

/**
 * The city shown in the background.
 * 
 * @author Armin Reichert
 */
public class City extends GameEntity {

	public enum CityState {
		Day, Night
	}

	public enum CityEvent {
		SunGoesDown, SunGoesUp
	}

	private final BirdyGame app;
	private final StateMachine<CityState, CityEvent> control;
	private int width;

	public City(BirdyGame app) {
		this.app = app;

		setSprites(new Sprite("bg_night"), new Sprite("bg_day"));

		control = new StateMachine<>("City control", CityState.class, Day);

		control.changeOnInput(SunGoesDown, Day, Night);

		control.state(Night).entry = s -> {
			s.setDuration(app.pulse.secToTicks(10));
			replaceStars();
		};

		control.state(Night).update = s -> {
			app.entities.filter(Star.class).forEach(Star::update);
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
		control.setLogger(LOG);
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
			Star star = app.entities.add(new Star());
			star.tf.moveTo(randomInt(50, getWidth() - 50), randomInt(100, 180));
		});
		LOG.info("Created " + numStars + " new stars");
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

	@Override
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public Sprite currentSprite() {
		return getSprite(isNight() ? 0 : 1);
	}

	@Override
	public void draw(Graphics2D pen) {
		pen.translate(tf.getX(), tf.getY());
		Image image = currentSprite().getImage();
		for (int x = 0; x < width; x += image.getWidth(null)) {
			pen.drawImage(image, x, 0, null);
		}
		app.entities.filter(Star.class).forEach(star -> star.draw(pen));
		pen.translate(-tf.getX(), -tf.getY());
	}
}