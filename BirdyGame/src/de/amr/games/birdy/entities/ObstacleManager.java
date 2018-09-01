package de.amr.games.birdy.entities;

import static de.amr.easy.game.Application.CLOCK;
import static de.amr.games.birdy.entities.ObstacleManagerState.Birth;
import static de.amr.games.birdy.entities.ObstacleManagerState.Breeding;
import static de.amr.games.birdy.entities.ObstacleManagerState.Stopped;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdLeftPassage;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.utils.Util.randomInt;

import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.view.View;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGameApp;
import de.amr.games.birdy.entities.bird.Bird;

/**
 * Manages the creation and deletion of obstacles.
 * 
 * @author Armin Reichert
 */
public class ObstacleManager extends GameEntity implements View {

	private final BirdyGameApp app;
	private final List<Obstacle> obstacles = new LinkedList<>();
	private final StateMachine<ObstacleManagerState, String> control;

	public ObstacleManager(BirdyGameApp app) {
		this.app = app;

		control = new StateMachine<>(getClass().getSimpleName(), ObstacleManagerState.class, Stopped);

		// Stay breeding for some random time from interval [MIN_PIPE_TIME, MAX_PIPE_TIME]:
		control.state(Breeding).entry = s -> {
			int minCreationTime = CLOCK.sec(app.settings.getAsFloat("min pipe creation sec"));
			int maxCreationTime = CLOCK.sec(app.settings.getAsFloat("max pipe creation sec"));
			s.setDuration(randomInt(minCreationTime, maxCreationTime));
		};

		// Update (move) pipes during breeding:
		control.state(Breeding).update = s -> obstacles.forEach(Obstacle::update);

		// When breeding time is over, it's birthday:
		control.changeOnTimeout(Breeding, Birth);

		// On birthday, update (add/remove) obstacles:
		control.state(Birth).entry = s -> updateObstacles();

		// And immediately become breeding again, like the M-people
		control.change(Birth, Breeding);

		// On "Stop" event, enter "Stopped" state:
		control.changeOnInput("Stop", Breeding, Stopped);
		control.changeOnInput("Stop", Birth, Stopped);

		// On "Start" event, become breeding again:
		control.changeOnInput("Start", Stopped, Breeding);
	}

	public void setLogger(Logger log) {
		control.setLogger(log);
	}

	@Override
	public void init() {
		obstacles.clear();
		control.init();
	}

	public void start() {
		control.addInput("Start");
		control.update();
	}

	public void stop() {
		control.addInput("Stop");
		control.update();
	}

	@Override
	public void update() {
		control.update();
	}

	@Override
	public void draw(Graphics2D g) {
		obstacles.forEach(o -> o.draw(g));
	}

	private void updateObstacles() {
		Ground ground = app.entities.ofClass(Ground.class).findAny().get();
		City city = app.entities.ofClass(City.class).findAny().get();
		Bird bird = app.entities.ofClass(Bird.class).findAny().get();

		// Add new obstacle
		int minHeight = app.settings.get("min pipe height");
		int passageHeight = app.settings.get("passage height");
		int width = app.settings.get("pipe width");
		int height = app.settings.get("pipe height");
		int passageCenterY = randomInt(minHeight + passageHeight / 2,
				(int) ground.tf.getY() - minHeight - passageHeight / 2);
		float speed = app.settings.get("world speed");

		Obstacle obstacle = new Obstacle(app, width, height, passageHeight, passageCenterY);
		obstacle.tf.setVelocityX(speed);
		obstacle.tf.setX(app.settings.width);
		obstacle.setLighted(city.isNight() && randomInt(0, 5) == 0);
		obstacles.add(obstacle);

		app.collisionHandler.registerStart(bird, obstacle.getUpperPart(), BirdTouchedPipe);
		app.collisionHandler.registerStart(bird, obstacle.getLowerPart(), BirdTouchedPipe);
		app.collisionHandler.registerEnd(bird, obstacle.getPassage(), BirdLeftPassage);

		// Remove obstacles that ran out of screen
		Iterator<Obstacle> it = obstacles.iterator();
		while (it.hasNext()) {
			obstacle = it.next();
			if (obstacle.tf.getX() + obstacle.tf.getWidth() < 0) {
				app.collisionHandler.unregisterStart(bird, obstacle.getUpperPart());
				app.collisionHandler.unregisterStart(bird, obstacle.getLowerPart());
				app.collisionHandler.unregisterEnd(bird, obstacle.getPassage());
				it.remove();
			}
		}
	}
}
