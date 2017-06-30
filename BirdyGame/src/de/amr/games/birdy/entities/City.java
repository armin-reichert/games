package de.amr.games.birdy.entities;

import static de.amr.games.birdy.utils.Util.randomInt;

import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGame;

/**
 * The city shown in the background.
 * 
 * @author Armin Reichert
 */
public class City extends GameEntity {

	private final BirdyGame app;
	private final StateMachine<Boolean, String> starControl;
	private boolean night;
	private int displayWidth;

	public City(BirdyGame app) {
		this.app = app;
		setSprites(new Sprite(app.assets, "bg_night"), new Sprite(app.assets, "bg_day"));
		starControl = new StateMachine<>("Star control", Boolean.class, true);
		starControl.state(true).entry = s -> {
			s.setDuration(app.motor.secToTicks(5));
			createStars();
		};
		starControl.changeOnTimeout(true, false);
		starControl.change(false, true);
	}

	@Override
	public void init() {
		starControl.init();
	}

	@Override
	public void update() {
		if (night) {
			starControl.update();
			app.entities.filter(Star.class).forEach(GameEntity::update);
		}
	}

	private void createStars() {
		app.entities.removeAll(Star.class);
		for (int i = 0; i < randomInt(1, app.settings.get("max stars")); ++i) {
			Star star = app.entities.add(new Star(new Sprite(app.assets, "blink_00", "blink_01", "blink_02")));
			star.tf.moveTo(randomInt(50, getWidth() - 50), randomInt(100, 180));
		}
	}

	public boolean isNight() {
		return night;
	}

	public void setNight(boolean night) {
		this.night = night;
		if (!night) {
			app.entities.removeAll(Star.class);
		}
		starControl.init();
	}

	@Override
	public int getWidth() {
		return displayWidth;
	}

	public void setWidth(int width) {
		displayWidth = width;
	}

	@Override
	public Sprite currentSprite() {
		return getSprite(night ? 0 : 1);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX(), tf.getY());
		Image image = currentSprite().getImage();
		for (int x = 0; x < displayWidth; x += image.getWidth(null)) {
			g.drawImage(image, x, 0, null);
		}
		app.entities.filter(Star.class).forEach(e -> e.draw(g));
		g.translate(-tf.getX(), -tf.getY());
	}
}