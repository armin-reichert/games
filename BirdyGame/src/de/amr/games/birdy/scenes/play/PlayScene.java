package de.amr.games.birdy.scenes.play;

import static de.amr.games.birdy.BirdyGame.Game;
import static de.amr.games.birdy.GameEvent.BirdLeftPassage;
import static de.amr.games.birdy.GameEvent.BirdLeftWorld;
import static de.amr.games.birdy.GameEvent.BirdTouchedGround;
import static de.amr.games.birdy.GameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.Globals.OBSTACLE_MAX_CREATION_TIME;
import static de.amr.games.birdy.Globals.OBSTACLE_MIN_CREATION_TIME;
import static de.amr.games.birdy.Globals.OBSTACLE_MIN_PIPE_HEIGHT;
import static de.amr.games.birdy.Globals.OBSTACLE_PASSAGE_HEIGHT;
import static de.amr.games.birdy.Globals.WORLD_SPEED;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.amr.easy.fsm.FSMEventDispatcher;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.entity.collision.CollisionHandler;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.game.timing.Countdown;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.GameEvent;
import de.amr.games.birdy.entities.Area;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.entities.GameOverText;
import de.amr.games.birdy.entities.Ground;
import de.amr.games.birdy.entities.PairOfPipes;
import de.amr.games.birdy.entities.PipeDown;
import de.amr.games.birdy.entities.PipeUp;
import de.amr.games.birdy.entities.ScoreDisplay;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.utils.Util;

public class PlayScene extends Scene<BirdyGame> implements FSMEventDispatcher<GameEvent> {

	private PipesManager pipesManager;
	private Bird bird;
	private City city;
	private Ground ground;
	private GameEntity gameOverText;
	private GameEntity scoreDisplay;

	private final PlaySceneControl control;

	public PlayScene(BirdyGame game) {
		super(game);
		control = new PlaySceneControl(app, this);
	}

	@Override
	public void dispatch(GameEvent event) {
		control.addInput(event);
		bird.dispatch(event);
	}

	@Override
	public void init() {
		initEntities();
		control.init();
	}

	void initEntities() {
		ground = Game.entities.findAny(Ground.class);
		city = Game.entities.findAny(City.class);
		bird = Game.entities.findAny(Bird.class);
		scoreDisplay = new ScoreDisplay(getApp().score, 1.5f);
		scoreDisplay.centerHor(getWidth());
		scoreDisplay.tr.setY(ground.tr.getY() / 4);
		gameOverText = Game.entities.add(new GameOverText());
		gameOverText.center(getWidth(), getHeight());
		pipesManager = new PipesManager();
		CollisionHandler.detectCollisionStart(bird, ground, BirdTouchedGround);
		Area birdWorld = new Area(0, -getHeight(), getWidth(), 2 * getHeight());
		CollisionHandler.detectCollisionEnd(bird, birdWorld, BirdLeftWorld);
	}

	@Override
	public void update() {
		bird.update();
		city.update();
		ground.update();
		pipesManager.update();
		gameOverText.update();
		scoreDisplay.update();
		for (Collision collision : CollisionHandler.collisions()) {
			dispatch((GameEvent) collision.getAppEvent());
		}
		control.update();
	}

	@Override
	public void draw(Graphics2D g) {
		city.draw(g);
		pipesManager.render(g);
		ground.draw(g);
		scoreDisplay.draw(g);
		bird.draw(g);
		if (control.stateID() == PlaySceneState.GameOver) {
			gameOverText.draw(g);
		}
		showState(g);
	}

	private void showState(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		g.drawString(toString() + ", bird state:" + bird.getFlightState() + " - " + bird.getHealthState(), 20,
				getHeight() - 50);
	}

	@Override
	public String toString() {
		return control.getDescription() + " (" + control.stateID() + ")";
	}

	void startScrolling() {
		ground.tr.setVel(WORLD_SPEED, 0);
		pipesManager.start();
	}

	void stopScrolling() {
		ground.tr.setVel(0, 0);
		pipesManager.stop();
	}

	// --- inner class for managing pipes

	private class PipesManager {

		private final List<PairOfPipes> pipesList;
		private final Countdown pipeCreation321;
		private boolean running;

		private PipesManager() {
			pipesList = new LinkedList<>();
			pipeCreation321 = new Countdown(Util.randomInt(OBSTACLE_MIN_CREATION_TIME, OBSTACLE_MAX_CREATION_TIME));
			running = false;
		}

		public void start() {
			pipeCreation321.reset();
			pipeCreation321.start();
			running = true;
		}

		public void stop() {
			running = false;
		}

		public void update() {
			if (running) {
				addPipes();
				removePipes();
				for (PairOfPipes pipes : pipesList) {
					pipes.update();
				}
			}
		}

		public void render(Graphics2D g) {
			for (PairOfPipes pipes : pipesList) {
				pipes.render(g);
			}
		}

		private void addPipes() {
			if (pipeCreation321.isComplete()) {
				PairOfPipes pipes = new PairOfPipes(Util.randomInt(OBSTACLE_MIN_PIPE_HEIGHT + OBSTACLE_PASSAGE_HEIGHT / 2,
						(int) ground.tr.getY() - OBSTACLE_MIN_PIPE_HEIGHT - OBSTACLE_PASSAGE_HEIGHT / 2));
				pipes.setPositionX(getApp().getWidth());
				pipes.setLighted(city.isNight() && new Random().nextBoolean());
				pipesList.add(pipes);
				CollisionHandler.detectCollisionStart(bird, pipes.getPipeDown(), BirdTouchedPipe);
				CollisionHandler.detectCollisionStart(bird, pipes.getPipeUp(), BirdTouchedPipe);
				CollisionHandler.detectCollisionEnd(bird, pipes.getPassage(), BirdLeftPassage);
				start();
			} else {
				pipeCreation321.update();
			}
		}

		private void removePipes() {
			Iterator<PairOfPipes> it = pipesList.iterator();
			while (it.hasNext()) {
				PairOfPipes pipes = it.next();
				if (pipes.getPositionX() + pipes.getWidth() < 0) {
					CollisionHandler.ignoreCollisionEnd(bird, pipes.getPipeDown());
					CollisionHandler.ignoreCollisionEnd(bird, pipes.getPipeUp());
					CollisionHandler.ignoreCollisionEnd(bird, pipes.getPassage());
					it.remove();
				}
			}
			Game.entities.removeAll(PipeUp.class);
			Game.entities.removeAll(PipeDown.class);
		}
	}
}