package de.amr.games.birdy.entities;

import static de.amr.games.birdy.BirdyGameEvent.BirdLeftPassage;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.entities.ObstacleManagerState.Birth;
import static de.amr.games.birdy.entities.ObstacleManagerState.Breeding;
import static de.amr.games.birdy.entities.ObstacleManagerState.Stopped;
import static de.amr.games.birdy.utils.Util.randomInt;

import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.collision.CollisionHandler;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.entities.bird.Bird;

/**
 * Manages the creation and deletion of obstacles.
 * 
 * @author Armin Reichert
 */
public class ObstacleManager extends GameEntity {

	private final BirdyGame app;
	private final List<Obstacle> obstacles = new LinkedList<>();
	private final StateMachine<ObstacleManagerState, String> control;

	public ObstacleManager(BirdyGame app) {
		this.app = app;

		control = new StateMachine<>(getClass().getSimpleName(), ObstacleManagerState.class, Stopped);

		// Stay breeding for some random time from interval [MIN_PIPE_TIME, MAX_PIPE_TIME]:
		control.state(Breeding).entry = s -> {
			int minCreationTime = app.motor.secToTicks(app.settings.getFloat("min pipe creation sec"));
			int maxCreationTime = app.motor.secToTicks(app.settings.getFloat("max pipe creation sec"));
			s.setDuration(randomInt(minCreationTime, maxCreationTime));
		};

		// Update (move) pipes during breeding:
		control.state(Breeding).update = s -> obstacles.forEach(Obstacle::update);

		// When breeding time is over, it's birthday:
		control.changeOnTimeout(Breeding, Birth);

		// On birthday, a new obstacle is born and obsolete obstacles die:
		control.state(Birth).entry = s -> {
			addObstacle();
			removeObsoleteObstacles();
		};

		// And immediately become breeding again, like the M-people
		control.change(Birth, Breeding);

		// On "Stop" event, enter "Stopped" state:
		control.changeOnInput("Stop", Breeding, Stopped);
		control.changeOnInput("Stop", Birth, Stopped);

		// On "Start" event, become breeding again:
		control.changeOnInput("Start", Stopped, Breeding);
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

	private void addObstacle() {
		Ground ground = app.entities.findAny(Ground.class);
		City city = app.entities.findAny(City.class);
		Bird bird = app.entities.findAny(Bird.class);
		int minPipeHeight = app.settings.get("min pipe height");
		int passageHeight = app.settings.get("passage height");
		int passageCenterY = randomInt(minPipeHeight + passageHeight / 2,
				(int) ground.tr.getY() - minPipeHeight - passageHeight / 2);
		Obstacle obstacle = new Obstacle(app, app.settings.get("pipe width"), app.settings.get("pipe height"),
				app.settings.get("passage height"), passageCenterY);
		obstacle.tr.setVelocityX(app.settings.get("world speed"));
		obstacle.tr.setX(app.getWidth());
		obstacle.setLighted(city.isNight() && new Random().nextInt(5) == 0);
		obstacles.add(obstacle);
		CollisionHandler.detectCollisionStart(bird, obstacle.getUpperPart(), BirdTouchedPipe);
		CollisionHandler.detectCollisionStart(bird, obstacle.getLowerPart(), BirdTouchedPipe);
		CollisionHandler.detectCollisionEnd(bird, obstacle.getPassage(), BirdLeftPassage);
	}

	private void removeObsoleteObstacles() {
		Bird bird = app.entities.findAny(Bird.class);
		Iterator<Obstacle> it = obstacles.iterator();
		while (it.hasNext()) {
			Obstacle obstacle = it.next();
			if (obstacle.tr.getX() + obstacle.getWidth() < 0) {
				CollisionHandler.ignoreCollisionEnd(bird, obstacle.getUpperPart());
				CollisionHandler.ignoreCollisionEnd(bird, obstacle.getLowerPart());
				CollisionHandler.ignoreCollisionEnd(bird, obstacle.getPassage());
				it.remove();
			}
		}
	}

	public void setLogger(Logger log) {
		control.setLogger(log);
	}
}
