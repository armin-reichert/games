package de.amr.games.birdy.entities;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.birdy.BirdyGameApp.entities;
import static de.amr.games.birdy.entities.City.DayEvent.SUNRISE;
import static de.amr.games.birdy.entities.City.DayEvent.SUNSET;
import static de.amr.games.birdy.entities.City.DayTime.DAY;
import static de.amr.games.birdy.entities.City.DayTime.NIGHT;
import static de.amr.games.birdy.utils.Util.randomInt;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.stream.IntStream;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.entity.SpriteEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

/**
 * The city shown in the background.
 * 
 * @author Armin Reichert
 */
public class City extends SpriteEntity {

	public enum DayTime {
		DAY, NIGHT
	}

	public enum DayEvent {
		SUNSET, SUNRISE
	}

	private final StateMachine<DayTime, DayEvent> fsm;

	public City() {

		sprites.set("s_night", Sprite.ofAssets("bg_night"));
		sprites.set("s_day", Sprite.ofAssets("bg_day"));
		sprites.select("s_day");

		tf.setWidth(sprites.current().getWidth());
		tf.setHeight(sprites.current().getHeight());

		fsm = new StateMachine<>(DayTime.class, Match.BY_EQUALITY);
		fsm.setDescription("City");
		fsm.setInitialState(DAY);

		fsm.state(DAY).setOnEntry(() -> {
			sprites.select("s_day");
		});

		fsm.addTransitionOnEventObject(DAY, NIGHT, null, null, SUNSET);
		fsm.addTransitionOnEventObject(DAY, DAY, null, null, SUNRISE);

		fsm.state(NIGHT).setTimer(() -> app().clock.sec(10));

		fsm.state(NIGHT).setOnEntry(() -> {
			sprites.select("s_night");
			replaceStars();
		});

		fsm.state(NIGHT).setOnTick(() -> {
			entities.ofClass(Star.class).forEach(Entity::update);
		});

		fsm.state(NIGHT).setOnExit(() -> {
			entities.removeAll(Star.class);
		});

		fsm.addTransitionOnTimeout(NIGHT, NIGHT, null, e -> {
			replaceStars();
			fsm.state(NIGHT).resetTimer();
		});

		fsm.addTransitionOnEventObject(NIGHT, DAY, null, null, SUNRISE);
	}

	@Override
	public void init() {
		fsm.init();
		fsm.traceTo(LOGGER, app().clock::getFrequency);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			sunset();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_D)) {
			sunrise();
		}
		fsm.update();
	}

	private void replaceStars() {
		entities.removeAll(Star.class);
		int numStars = randomInt(1, app().settings.get("max stars"));
		IntStream.range(1, numStars).forEach(i -> {
			Star star = entities.store(new Star());
			star.tf.setPosition(randomInt(50, tf.getWidth() - 50), randomInt(100, 180));
		});
		Application.LOGGER.info("Created " + numStars + " new stars");
	}

	public boolean isNight() {
		return fsm.getState() == NIGHT;
	}

	public void sunset() {
		fsm.enqueue(SUNSET);
	}

	public void sunrise() {
		fsm.enqueue(SUNRISE);
	}

	public void setWidth(int width) {
		if (tf.getWidth() != width) {
			tf.setWidth(width);
			sprites.forEach(sprite -> {
				sprite.scale(width, sprite.getHeight());
			});
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		Image image = sprites.current().currentFrame();
		for (int x = 0; x < tf.getWidth(); x += image.getWidth(null)) {
			g.drawImage(image, x, 0, null);
		}
		entities.ofClass(Star.class).forEach(star -> star.draw(g));
		g.translate(-tf.getX(), -tf.getY());
	}
}