package de.zwickmann.windrad.ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import de.zwickmann.windrad.model.WindparkModell;

public class WindparkFenster extends JFrame {

	public WindparkFenster(WindparkModell windpark, int ansichtBreite, int ansichtHöhe) {
		super("Windpark Simulation");
		WindparkAnsicht ansicht = new WindparkAnsicht(windpark);
		ansicht.setPreferredSize(new Dimension(ansichtBreite, ansichtHöhe));
		ansicht.setMinimumSize(ansicht.getPreferredSize());
		add(ansicht);
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