package de.amr.games.windrad.ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import de.amr.games.windrad.model.Windpark;

public class WindparkFenster extends JFrame {

	public WindparkFenster(Windpark windpark, int ansichtBreite, int ansichtHöhe) {
		super("Windpark Simulation");
		try {
			WindparkAnsicht ansicht = new WindparkAnsicht(windpark, ansichtBreite, ansichtHöhe);
			ansicht.setPreferredSize(new Dimension(ansichtBreite, ansichtHöhe));
			ansicht.setMinimumSize(ansicht.getPreferredSize());
			add(ansicht);
		} catch (Exception e) {
			e.printStackTrace();
		}
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				windpark.speichern();
			}
		});
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}