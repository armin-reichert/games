package de.amr.games.birdy.scenes.start;

import static de.amr.games.birdy.GameEvent.BirdLeftWorld;
import static de.amr.games.birdy.GameEvent.BirdTouchedGround;
import static de.amr.games.birdy.GameEvent.Tick;
import static de.amr.games.birdy.Globals.WORLD_SPEED;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.fsm.FSM;
import de.amr.easy.fsm.FSMEventDispatcher;
import de.amr.easy.game.Application;
import de.amr.easy.game.common.PumpingText;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.entity.collision.CollisionHandler;
import de.amr.easy.game.scene.Scene;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.GameEvent;
import de.amr.games.birdy.entities.Area;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.entities.Ground;
import de.amr.games.birdy.entities.TitleText;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.entities.bird.Feathers;
import de.amr.games.birdy.utils.Util;

public class StartScene extends Scene<BirdyGame> implements FSMEventDispatcher<GameEvent> {

	private Bird bird;
	private City city;
	private Ground ground;
	private GameEntity displayedText;

	private final FSM<StartSceneState, GameEvent> control;

	public StartScene(BirdyGame game) {
		super(game);
		control = new StartSceneControl(this);
		Application.Log.info("\n" + control.toGraphViz());
	}

	@Override
	public void init() {
		control.init();
	}

	void reset() {
		city = Application.Entities.findAny(City.class);
		city.setWidth(getWidth());
		city.setNight(new Random().nextBoolean());
		ground = Application.Entities.findAny(Ground.class);
		ground.setWidth(getWidth());
		ground.tr.moveTo(0, getHeight() - ground.getHeight());
		ground.tr.setVel(WORLD_SPEED, 0);
		bird = Application.Entities.findAny(Bird.class);
		bird.init();
		bird.tr.moveTo(getWidth() / 8, ground.tr.getY() / 2);
		bird.tr.setVel(0, 0);
		bird.setFeathers(city.isNight() ? Feathers.BLUE : Feathers.YELLOW);
		displayedText = Application.Entities.add(new TitleText());
		PumpingText readyText = new PumpingText("text_ready", 0.2f);
		readyText.setName("readyText");
		Application.Entities.add(readyText);
		CollisionHandler.clear();
		CollisionHandler.detectCollisionStart(bird, ground, BirdTouchedGround);
		Area birdWorld = new Area(0, -getHeight(), getWidth(), 2 * getHeight());
		CollisionHandler.detectCollisionEnd(bird, birdWorld, BirdLeftWorld);
	}

	@Override
	public void update() {
		city.update();
		ground.update();
		bird.update();
		for (Collision ce : CollisionHandler.collisions()) {
			dispatch((GameEvent) ce.getAppEvent());
		}
		control.run(Tick);
	}

	@Override
	public void dispatch(GameEvent event) {
		control.enqueue(event);
		bird.dispatch(event);
	}

	@Override
	public void draw(Graphics2D g) {
		city.draw(g);
		ground.draw(g);
		bird.draw(g);
		displayedText.center(getWidth(), getHeight() - ground.getHeight());
		displayedText.draw(g);
		showState(g);
	}

	private void showState(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		g.drawString(
				toString() + ", bird state:" + bird.getFlightState() + " - " + bird.getHealthState(), 20,
				getHeight() - 50);
	}

	@Override
	public String toString() {
		return control.getDescription() + "(" + control.getCurrentState() + ")";
	}

	void showReadyText() {
		displayedText = Application.Entities.findByName(PumpingText.class, "readyText");
	}

	void showTitleText() {
		displayedText = Application.Entities.findAny(TitleText.class);
	}

	void stopScrolling() {
		ground.tr.setVel(0, 0);
	}

	void keepBirdInAir() {
		while (bird.tr.getY() > ground.tr.getY() / 2) {
			bird.jump(Util.randomInt(1, 4));
		}
	}
}
