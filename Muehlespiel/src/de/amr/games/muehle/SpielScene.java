package de.amr.games.muehle;

import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static de.amr.games.muehle.SpielPhase.Initialisiert;
import static de.amr.games.muehle.SpielPhase.Setzen;
import static de.amr.games.muehle.SpielPhase.Spielen;
import static java.awt.event.KeyEvent.VK_SPACE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.ScrollingText;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;

public class SpielScene extends Scene<MuehleApp> {

	private MouseEvent mouseEvent;

	private SpielSteuerung steuerung;
	private MuehleBrett brett;
	private ScrollingText startText;

	private boolean weißAnDerReihe;
	private int weißeSteineGesetzt;
	private int schwarzeSteineGesetzt;

	private class SpielSteuerung extends StateMachine<SpielPhase, String> {

		public SpielSteuerung() {
			super("Mühlespiel Steuerung", SpielPhase.class, SpielPhase.Initialisiert);

			state(Initialisiert).entry = s -> {
				brett = new MuehleBrett(600, 600);
				brett.tf.moveTo(100, 100);
				app.entities.add(brett);

				startText = new ScrollingText();
				startText.setColor(Color.BLACK);
				startText.setFont(new Font("Sans", Font.PLAIN, 20));
				startText.setText("Drücke SPACE zum Start");
				startText.tf.moveTo(0, getHeight() - 40);
				app.entities.add(startText);
			};

			state(Initialisiert).exit = s -> {
				startText.visibility = () -> false;
			};

			change(Initialisiert, Setzen, () -> keyPressedOnce(VK_SPACE));

			state(Setzen).entry = s -> {
				weißAnDerReihe = true;
				weißeSteineGesetzt = 0;
				schwarzeSteineGesetzt = 0;
			};

			state(Setzen).update = s -> {
				if (mouseEvent != null) {
					int p = findeBrettPosition(mouseEvent);
					if (p != -1 && brett.gibStein(p) == null) {
						// An position p Stein setzen:
						if (weißAnDerReihe) {
							brett.setzeStein(Steinfarbe.WEISS, p);
							weißeSteineGesetzt += 1;
						} else {
							brett.setzeStein(Steinfarbe.SCHWARZ, p);
							schwarzeSteineGesetzt += 1;
						}
						weißAnDerReihe = !weißAnDerReihe;
					}
					mouseEvent = null;
				}
			};

			change(Setzen, Spielen, () -> weißeSteineGesetzt == 9 && schwarzeSteineGesetzt == 9);

		}

	}

	public SpielScene(MuehleApp app) {
		super(app);
		setBgColor(Color.WHITE);
		steuerung = new SpielSteuerung();
		steuerung.setLogger(Application.LOG);

		app.getShell().getCanvas().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseEvent = e;
			}
		});
	}

	private int findeBrettPosition(MouseEvent e) {
		int mouseX = e.getX();
		int mouseY = e.getY();
		int brettX = Math.abs(Math.round(mouseX - brett.tf.getX()));
		int brettY = Math.abs(Math.round(mouseY - brett.tf.getY()));
		return brett.findePosition(brettX, brettY, brett.getWidth() / 18);
	}

	@Override
	public void init() {
		steuerung.init();
	}

	@Override
	public void update() {
		steuerung.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(getBgColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		brett.draw(g);
		startText.hCenter(getWidth());
		startText.draw(g);
	}
}