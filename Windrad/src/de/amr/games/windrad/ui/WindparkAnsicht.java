package de.amr.games.windrad.ui;

import static de.amr.games.windrad.model.WindparkModell.BREITE;
import static de.amr.games.windrad.model.WindparkModell.HÖHE;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.lang.Math.sqrt;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
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

import de.amr.games.windrad.model.WindparkModell;
import de.amr.games.windrad.model.WindradModell;

public class WindparkAnsicht extends JPanel {

	private static final String[][] HILFE_TEXTE = {
		/*@formatter:off*/
		{"F1", "Hilfe ein/aus"},
		{"SPACE", "Windrad starten/stoppen"},
		{"Shift SPACE", "Alle Windräder starten"},
		{"S", "Nächstes Windrad auswählen"},
		{"N", "Neues Windrad bauen"},
		{"C", "Windrad konfigurieren"},
		{"I", "Drehrichtung umkehren"},
		{"UP", "Windrad nach oben schieben"},
		{"DOWN", "Windrad nach unten schieben"},
		{"LEFT", "Windrad nach links schieben"},
		{"RIGHT", "Windrad nach rechts schieben"},
		{"Z", "Zwicky-Windrad (mini)"},
		{"+", "Windradhöhe vergrößern"},
		{"-", "Windradhöhe verkleinern"},
		{"Control +", "Rotorlänge vergrößern"},
		{"Control -", "Rotorlänge verkleinern"},
		{"Shift +", "Nabenradius vergrößern"},
		{"Shift -", "Nabenradius verkleinern"},
		/*@formatter:on*/
	};

	private static final Font HILFE_FONT = new Font("Monospaced", Font.PLAIN, 14);

	public final WindparkModell windpark;
	public final List<WindradAnsicht> ansichten = new ArrayList<>();

	private boolean hilfeAn;
	private Image hintergrundBild;
	private int auswahl;

	private WindradEditorFenster editorFenster;
	private final Timer sonnenTimer;

	public WindparkAnsicht(WindparkModell windpark) {
		this.windpark = windpark;
		hilfeAn = true;
		auswahl = 0;

		// Hintergrundbild
		try {
			hintergrundBild = ImageIO.read(getClass().getResourceAsStream("/hintergrund.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Sonnenanimation
		windpark.zentrumSonne.setLocation(-BREITE / 2, HÖHE / 2);
		sonnenTimer = new Timer(0, e -> {
			int deltaX = 1;
			windpark.zentrumSonne.x += deltaX;
			if (windpark.zentrumSonne.x > BREITE / 2) {
				windpark.zentrumSonne.x = -BREITE / 2;
			}
			float radiusQuadrat = (float) 0.25 * (BREITE * BREITE + HÖHE * HÖHE);
			float xQuadrat = windpark.zentrumSonne.x * windpark.zentrumSonne.x;
			windpark.zentrumSonne.y = (float) sqrt(radiusQuadrat - xQuadrat);
			repaint();
		});
		sonnenTimer.setDelay(300);
		sonnenTimer.start();

		// Windrad-Animationen
		for (WindradModell windrad : windpark.windräder) {
			ansichten.add(new WindradAnsicht(this, windrad));
		}

		// Maus
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int modelX = e.getX() - getWidth() / 2;
				int modelY = getHeight() - e.getY();
				wähleWindradDurchMausklick(modelX, modelY);
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
		belegeTaste("C", this::action_öffneEditor);
		belegeTaste("I", this::action_ändereDrehrichtung);
		belegeTaste("UP", this::action_schiebeNachOben);
		belegeTaste("DOWN", this::action_schiebeNachUnten);
		belegeTaste("LEFT", this::action_schiebeNachLinks);
		belegeTaste("RIGHT", this::action_schiebeNachRechts);
		belegeTaste("PLUS", this::action_vergrößereHöhe);
		belegeTaste("MINUS", this::action_verkleinereHöhe);
		belegeTaste("control PLUS", this::action_vergrößereRotorLänge);
		belegeTaste("control MINUS", this::action_verkleinereRotorLänge);
		belegeTaste("shift PLUS", this::action_vergrößereNabenGröße);
		belegeTaste("shift MINUS", this::action_verkleinereNabenGröße);
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

		if (hilfeAn) {
			zeichneHilfeText(g);
		}

		// Koordinaten-Transformation Welt -> Anzeige
		g.translate(getWidth() / 2, getHeight());
		g.scale(1, -1);

		zeichneSonne(g);

		// for (WindradAnsicht ansicht : ansichten) {
		// // TODO test wieder entfernen, wenn Bug gefixt
		// if (windpark.zentrumSonne.y > ansicht.windrad.turmLO.y) {
		// ansicht.zeichneSchatten(g);
		// }
		// }

		for (WindradAnsicht ansicht : ansichten) {
			if (ansichten.get(auswahl) != ansicht) {
				ansicht.zeichne(g, false);
			}
		}
		ansichten.get(auswahl).zeichne(g, true);
	}

	private void zeichneSonne(Graphics2D g) {
		int durchmesser = 40;
		g.setColor(Color.YELLOW);
		g.fillOval((int) (windpark.zentrumSonne.x - durchmesser / 2),
				(int) (windpark.zentrumSonne.y - durchmesser / 2), durchmesser, durchmesser);
	}

	private void zeichneHilfeText(Graphics2D g) {
		g.setFont(HILFE_FONT);
		g.setColor(Color.WHITE);
		int y = 2 * HILFE_FONT.getSize(), x = getWidth() - 380, lineHeight = HILFE_FONT.getSize() + 3;
		for (String[] text : HILFE_TEXTE) {
			g.drawString(String.format("%10s", text[0]) + " = " + text[1], x, y);
			y += lineHeight;
		}
	}

	private void wähleWindradDurchMausklick(double modelX, double modelY) {
		for (WindradAnsicht bild : ansichten) {
			WindradModell windrad = bild.windrad;
			Ellipse2D.Float nabenKreis = new Ellipse2D.Float(windrad.nabeZentrum.x - windrad.nabeRadius,
					windrad.nabeZentrum.y - windrad.nabeRadius, 2 * windrad.nabeRadius,
					2 * windrad.nabeRadius);

			if (nabenKreis.contains(modelX, modelY) || windrad.turm.contains(modelX, modelY)) {
				auswahl = ansichten.indexOf(bild);
				break;
			}
		}
	}

	public void wähleWindrad(WindradModell windrad) {
		for (WindradAnsicht ansicht : ansichten) {
			if (windrad == ansicht.windrad) {
				auswahl = ansichten.indexOf(ansicht);
			}
		}
	}

	public void wähleNächstesWindrad() {
		// create [0, 1, 2, ... ]
		Integer[] sortedByX = new Integer[ansichten.size()];
		for (int i = 0; i < sortedByX.length; ++i) {
			sortedByX[i] = i;
		}
		// sort by x-coordinate
		Arrays.sort(sortedByX, (i1, i2) -> {
			float x1 = ansichten.get(i1).windrad.basis().x;
			float x2 = ansichten.get(i2).windrad.basis().x;
			if (x1 == x2) {
				float y1 = ansichten.get(i1).windrad.basis().y;
				float y2 = ansichten.get(i2).windrad.basis().y;
				return y1 < y2 ? -1 : y1 > y2 ? 1 : 0;
			}
			return x1 < x2 ? -1 : 1;
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
		return ansichten.get(auswahl);
	}

	private WindradModell ausgewähltesWindrad() {
		return ausgewählteAnsicht().windrad;
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
		float turmBreite = turmHöhe / 10;
		float nabenRadius = turmBreite;
		float rotorLänge = turmHöhe / 2 - WindradModell.MIN_BODEN_ABSTAND;
		float rotorBreite = rotorLänge / 10;
		WindradModell windrad = new WindradModell(0, y, turmHöhe, turmBreite, nabenRadius, rotorLänge,
				rotorBreite);
		windpark.windräder.add(windrad);
		ansichten.add(new WindradAnsicht(this, windrad));
		wähleWindrad(windrad);
		repaint();
	}

	public void action_löscheWindrad(ActionEvent e) {
		if (windpark.windräder.size() <= 1) {
			return;
		}
		WindradAnsicht ansicht = ansichten.remove(auswahl);
		windpark.windräder.remove(ansicht.windrad);
		repaint();
		auswahl = 0;
	}

	public void action_wähleNächstesWindrad(ActionEvent e) {
		wähleNächstesWindrad();
	}

	public void action_starteStoppeAusgewähltesWindrad(ActionEvent e) {
		if (ausgewählteAnsicht().inBewegung()) {
			ausgewählteAnsicht().stop();
		} else {
			ausgewählteAnsicht().start();
		}
	}

	public void action_starteAlleWindräder(ActionEvent e) {
		for (WindradAnsicht ansicht : ansichten) {
			if (!ansicht.inBewegung()) {
				ansicht.start();
			}
		}
	}

	public void action_öffneEditor(ActionEvent e) {
		editorFenster = new WindradEditorFenster(this, ausgewähltesWindrad());
		editorFenster.setAlwaysOnTop(true);
		editorFenster.setLocationRelativeTo(this);
		editorFenster.setVisible(true);
	}

	public void action_ändereDrehrichtung(ActionEvent e) {
		ausgewählteAnsicht().ändereDrehrichtung();
	}

	public void action_schiebeNachOben(ActionEvent e) {
		WindradModell windrad = ausgewähltesWindrad();
		Point2D.Float altePosition = windrad.basis();
		if (altePosition.y + 5 < getHeight() - windrad.höhe()) {
			windrad.verschiebe(altePosition.x, altePosition.y + 5);
			repaint();
		}
	}

	public void action_schiebeNachUnten(ActionEvent e) {
		WindradModell windrad = ausgewähltesWindrad();
		Point2D.Float altePosition = windrad.basis();
		if (altePosition.y - 5 >= 0) {
			windrad.verschiebe(altePosition.x, altePosition.y - 5);
			repaint();
		}
	}

	public void action_schiebeNachLinks(ActionEvent e) {
		WindradModell windrad = ausgewähltesWindrad();
		Point2D.Float altePosition = windrad.basis();
		if (altePosition.x - 5 > windrad.breite() / 2 - getWidth() / 2) {
			windrad.verschiebe(altePosition.x - 5, altePosition.y);
			repaint();
		}
	}

	public void action_schiebeNachRechts(ActionEvent e) {
		WindradModell windrad = ausgewähltesWindrad();
		Point2D.Float altePosition = windrad.basis();
		if (altePosition.x + 5 < getWidth() / 2 - windrad.breite() / 2) {
			windrad.verschiebe(altePosition.x + 5, altePosition.y);
			repaint();
		}
	}

	public void action_vergrößereHöhe(ActionEvent e) {
		WindradModell windrad = ausgewähltesWindrad();
		if (windrad.basis().y + 20 < getHeight() - windrad.höhe()) {
			try {
				windrad.aufstellen(windrad.turmHöhe() + 20);
				repaint();
			} catch (IllegalStateException x) {
				System.out.println(x.getMessage());
			}
		}
	}

	public void action_verkleinereHöhe(ActionEvent e) {
		WindradModell windrad = ausgewähltesWindrad();
		try {
			windrad.aufstellen(windrad.turmHöhe() - 20);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_vergrößereRotorLänge(ActionEvent e) {
		WindradModell windrad = ausgewähltesWindrad();
		try {
			windrad.setzeRotorLänge(windrad.rotorLänge + 5);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_verkleinereRotorLänge(ActionEvent e) {
		WindradModell windrad = ausgewähltesWindrad();
		try {
			windrad.setzeRotorLänge(windrad.rotorLänge - 5);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_vergrößereNabenGröße(ActionEvent e) {
		WindradModell windrad = ausgewähltesWindrad();
		try {
			windrad.setzeNabeRadius(windrad.nabeRadius + 1);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_verkleinereNabenGröße(ActionEvent e) {
		WindradModell windrad = ausgewähltesWindrad();
		try {
			windrad.setzeNabeRadius(windrad.nabeRadius - 1);
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}

	public void action_zwickyWindrad(ActionEvent e) {
		WindradModell windrad = ausgewähltesWindrad();
		try {
			windrad.minimieren();
			repaint();
		} catch (IllegalStateException x) {
			System.out.println(x.getMessage());
		}
	}
}