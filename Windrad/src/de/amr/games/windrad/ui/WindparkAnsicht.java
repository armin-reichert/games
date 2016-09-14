package de.amr.games.windrad.ui;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import de.amr.games.windrad.model.Windpark;
import de.amr.games.windrad.model.Windrad;

/**
 * Windpark (View).
 */
public class WindparkAnsicht extends JPanel {

	private final Windpark windpark;
	private final List<WindradAnsicht> windradAnsichten = new ArrayList<>();
	private final HilfeTextAnsicht hilfeText = new HilfeTextAnsicht();
	private boolean hilfeAn;
	private Image hintergrundBild;
	private int auswahl;
	private final Timer sonnenAnimation;

	public WindparkAnsicht(Windpark windpark, int breite, int höhe) throws IOException {
		this.windpark = windpark;
		hilfeAn = true;
		auswahl = 0;
		hintergrundBild = ImageIO.read(getClass().getResourceAsStream("/hintergrund.png"));

		// Sonnenanimation
		windpark.zentrumSonne.setLocation(-breite / 2, höhe / 2);
		sonnenAnimation = new Timer(0, e -> {
			int deltaX = 1;
			windpark.zentrumSonne.x += deltaX;
			if (windpark.zentrumSonne.x > breite / 2) {
				windpark.zentrumSonne.x = -breite / 2;
			}
			float radiusQuadrat = (float) 0.25 * (breite * breite + höhe * höhe);
			float xQuadrat = windpark.zentrumSonne.x * windpark.zentrumSonne.x;
			windpark.zentrumSonne.y = (float) sqrt(radiusQuadrat - xQuadrat);
			repaint();
		});
		sonnenAnimation.setDelay(600);
		sonnenAnimation.start();

		for (Windrad windrad : windpark.windräder) {
			windradAnsichten.add(new WindradAnsicht(this, windrad));
		}

		// Maus
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int modelX = e.getX() - getWidth() / 2;
				int modelY = getHeight() - e.getY();
				wähleWindradAnPosition(modelX, modelY);
				repaint();
			}
		});

		// Tastatur
		belegeTaste("F1", this::action_schalteHilfeEinAus);
		belegeTaste("SPACE", this::action_starteStoppeAusgewähltesWindrad);
		belegeTaste("shift SPACE", this::action_starteAlleWindräder);
		belegeTaste("S", this::action_wähleNächstesWindrad);
		belegeTaste("N", this::action_neuesWindrad);
		belegeTaste("DELETE", this::action_löscheWindrad);
		belegeTaste("I", this::action_ändereDrehrichtung);
		belegeTaste("UP", this::action_schiebeNachOben);
		belegeTaste("DOWN", this::action_schiebeNachUnten);
		belegeTaste("LEFT", this::action_schiebeNachLinks);
		belegeTaste("RIGHT", this::action_schiebeNachRechts);
		belegeTaste("PLUS", this::action_vergrößereHöhe);
		belegeTaste("MINUS", this::action_verkleinereHöhe);
		belegeTaste("control PLUS", this::action_vergrößereRotorLänge);
		belegeTaste("control MINUS", this::action_verkleinereRotorLänge);
		belegeTaste("shift PLUS", this::action_vergrößereNabenRadius);
		belegeTaste("shift MINUS", this::action_verkleinereNabenRadius);
		belegeTaste("Z", this::action_zwickyWindrad);
	}

	@Override
	protected void paintComponent(Graphics gg) {
		Graphics2D g = (Graphics2D) gg;
		g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
		super.paintComponent(g);

		if (hintergrundBild != null) {
			g.drawImage(hintergrundBild, 0, 0, getWidth(), getHeight(), null);
		} else {
			g.setColor(Color.BLUE);
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		// Koordinaten-Transformation Model -> View
		AffineTransform ot = g.getTransform();
		g.translate(getWidth() / 2, getHeight());
		g.scale(1, -1);

		// Sonne
		int durchmesser = 40;
		g.setColor(Color.YELLOW);
		g.fillOval((int) (windpark.zentrumSonne.x - durchmesser / 2),
				(int) (windpark.zentrumSonne.y - durchmesser / 2), durchmesser, durchmesser);

		// Windräder
		if (!windradAnsichten.isEmpty()) {
			for (WindradAnsicht w : windradAnsichten) {
				if (windradAnsichten.get(auswahl) != w) {
					w.draw(g, false);
				}
			}
			windradAnsichten.get(auswahl).draw(g, true);
		}

		g.setTransform(ot);

		// Hilfetext
		if (hilfeAn) {
			hilfeText.setLocation(getWidth() - 360, 20);
			hilfeText.draw(g);
		}
	}

	private void wähleWindradAnPosition(double modelX, double modelY) {
		for (WindradAnsicht w : windradAnsichten) {
			Windrad windrad = w.getWindrad();
			if (windrad.getNabe().contains(modelX, modelY)
					|| windrad.getTurm().contains(modelX, modelY)) {
				auswahl = windradAnsichten.indexOf(w);
				break;
			}
		}
	}

	public void wähleWindrad(Windrad windrad) {
		for (WindradAnsicht w : windradAnsichten) {
			if (windrad == w.getWindrad()) {
				auswahl = windradAnsichten.indexOf(w);
			}
		}
	}

	public void wähleNächstesWindrad() {
		// create [0, 1, 2, ... ]
		Integer[] sortedByX = new Integer[windradAnsichten.size()];
		for (int i = 0; i < sortedByX.length; ++i) {
			sortedByX[i] = i;
		}
		// sort by x-coordinate
		Arrays.sort(sortedByX, (i1, i2) -> {
			Point2D.Float pos1 = windradAnsichten.get(i1).getWindrad().getPosition();
			Point2D.Float pos2 = windradAnsichten.get(i2).getWindrad().getPosition();
			if (pos1.x == pos2.x) {
				return pos1.y < pos2.y ? -1 : pos1.y > pos2.y ? 1 : 0;
			}
			return pos1.x < pos2.x ? -1 : 1;
		});
		// find selected index in new order
		int index = 0;
		while (sortedByX[index] != auswahl) {
			++index;
		}
		// set selection to next index in new order
		auswahl = sortedByX[index + 1 < sortedByX.length ? index + 1 : 0];
		repaint();
	}

	public WindradAnsicht ausgewählteAnsicht() {
		return windradAnsichten.get(auswahl);
	}

	private Windrad ausgewähltesWindrad() {
		return ausgewählteAnsicht().getWindrad();
	}

	private void belegeTaste(String taste, Consumer<ActionEvent> action) {
		getInputMap().put(KeyStroke.getKeyStroke(taste), action.toString());
		getActionMap().put(action.toString(), new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				action.accept(e);
			}
		});
	}

	// Action-Handlers

	public void action_schalteHilfeEinAus(ActionEvent e) {
		hilfeAn = !hilfeAn;
		repaint();
	}

	public void action_neuesWindrad(ActionEvent e) {
		int y = getHeight() / 4;
		float turmHöhe = getHeight() / 4;
		float turmBreiteUnten = turmHöhe / 10;
		float turmBreiteOben = turmBreiteUnten * 0.80f;
		float nabenRadius = turmBreiteOben;
		float rotorLänge = turmHöhe / 2 - Windrad.getMinBodenAbstand();
		float rotorBreite = rotorLänge / 10;
		Windrad windrad = new Windrad(0, y, turmHöhe, turmBreiteUnten, turmBreiteOben, nabenRadius,
				rotorLänge, rotorBreite);
		windpark.windräder.add(windrad);
		windradAnsichten.add(new WindradAnsicht(this, windrad));
		wähleWindrad(windrad);
		repaint();
	}

	public void action_löscheWindrad(ActionEvent e) {
		if (windpark.windräder.size() <= 1) {
			return;
		}
		WindradAnsicht ansicht = windradAnsichten.remove(auswahl);
		windpark.windräder.remove(ansicht.getWindrad());
		repaint();
		auswahl = 0;
	}

	public void action_wähleNächstesWindrad(ActionEvent e) {
		wähleNächstesWindrad();
	}

	public void action_starteStoppeAusgewähltesWindrad(ActionEvent e) {
		if (ausgewählteAnsicht().drehtSich()) {
			ausgewählteAnsicht().stop();
		} else {
			ausgewählteAnsicht().start();
		}
	}

	public void action_starteAlleWindräder(ActionEvent e) {
		for (WindradAnsicht ansicht : windradAnsichten) {
			if (!ansicht.drehtSich()) {
				ansicht.start();
			}
		}
	}

	public void action_ändereDrehrichtung(ActionEvent e) {
		ausgewählteAnsicht().ändereDrehrichtung();
	}

	public void action_schiebeNachOben(ActionEvent e) {
		Windrad windrad = ausgewähltesWindrad();
		Point2D.Float altePosition = windrad.getPosition();
		if (altePosition.y + 5 < getHeight() - windrad.getHöhe()) {
			windrad.verschiebe(altePosition.x, altePosition.y + 5);
			repaint();
		}
	}

	public void action_schiebeNachUnten(ActionEvent e) {
		Windrad windrad = ausgewähltesWindrad();
		Point2D.Float altePosition = windrad.getPosition();
		if (altePosition.y - 5 >= 0) {
			windrad.verschiebe(altePosition.x, altePosition.y - 5);
			repaint();
		}
	}

	public void action_schiebeNachLinks(ActionEvent e) {
		Windrad windrad = ausgewähltesWindrad();
		Point2D.Float altePosition = windrad.getPosition();
		if (altePosition.x - 5 > windrad.getBreite() / 2 - getWidth() / 2) {
			windrad.verschiebe(altePosition.x - 5, altePosition.y);
			repaint();
		}
	}

	public void action_schiebeNachRechts(ActionEvent e) {
		Windrad windrad = ausgewähltesWindrad();
		Point2D.Float altePosition = windrad.getPosition();
		if (altePosition.x + 5 < getWidth() / 2 - windrad.getBreite() / 2) {
			windrad.verschiebe(altePosition.x + 5, altePosition.y);
			repaint();
		}
	}

	public void action_vergrößereHöhe(ActionEvent e) {
		Windrad windrad = ausgewähltesWindrad();
		if (windrad.getPosition().y + 20 < getHeight() - windrad.getHöhe()) {
			try {
				windrad.errichte(windrad.getTurmHöhe() + 20);
				repaint();
			} catch (IllegalStateException x) {
				System.out.println(x.getMessage());
			}
		}
	}

	public void action_verkleinereHöhe(ActionEvent e) {
		Windrad windrad = ausgewähltesWindrad();
		try {
			windrad.errichte(windrad.getTurmHöhe() - 20);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_vergrößereRotorLänge(ActionEvent e) {
		Windrad windrad = ausgewähltesWindrad();
		try {
			windrad.setzeRotorLänge(windrad.getRotorLänge() + 5);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_verkleinereRotorLänge(ActionEvent e) {
		Windrad windrad = ausgewähltesWindrad();
		try {
			windrad.setzeRotorLänge(windrad.getRotorLänge() - 5);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_vergrößereNabenRadius(ActionEvent e) {
		Windrad windrad = ausgewähltesWindrad();
		try {
			windrad.setzeNabenRadius(windrad.getNabenRadius() + 1);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_verkleinereNabenRadius(ActionEvent e) {
		Windrad windrad = ausgewähltesWindrad();
		try {
			windrad.setzeNabenRadius(windrad.getNabenRadius() - 1);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_zwickyWindrad(ActionEvent e) {
		Windrad windrad = ausgewähltesWindrad();
		try {
			windrad.minimiere();
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}
}