package de.amr.games.birdy.entities;

import static de.amr.games.birdy.BirdyGame.CITY_MAX_STARS;

import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.game.timing.Countdown;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.utils.Util;

public class City extends GameEntity {

	private final BirdyGame app;
	private int width;
	private boolean night;
	private Countdown starLifetime;

	public City(BirdyGame app) {
		this.app = app;
		setSprites(new Sprite(app.assets, "bg_night"), new Sprite(app.assets, "bg_day"));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (night) {
			if (starLifetime.isComplete()) {
				createStars();
				starLifetime.restart();
			}
			app.entities.filter(Star.class).forEach(GameEntity::update);
			starLifetime.update();
		}
	}

	private void createStars() {
		app.entities.removeAll(Star.class);
		for (int i = 0; i < Util.randomInt(1, CITY_MAX_STARS); ++i) {
			Star star = app.entities.add(new Star(app.assets));
			star.tr.moveTo(Util.randomInt(50, width - 50), Util.randomInt(100, 180));
		}
		starLifetime = new Countdown(300);
		starLifetime.start();
	}

	public boolean isNight() {
		return night;
	}

	public void setNight(boolean b) {
		this.night = b;
		if (night) {
			createStars();
		} else {
			app.entities.removeAll(Star.class);
		}
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public Sprite currentSprite() {
		return getSprite(night ? 0 : 1);
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tr.getX(), tr.getY());
		Image image = currentSprite().getImage();
		for (int x = 0; x < width; x += image.getWidth(null)) {
			g.drawImage(image, x, 0, null);
		}
		app.entities.filter(Star.class).forEach(e -> e.draw(g));
		g.translate(-tr.getX(), -tr.getY());
	}
}
