package de.amr.games.windrad;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.amr.games.windrad.model.Windpark;
import de.amr.games.windrad.ui.WindparkFenster;

public class WindradApp {

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
		EventQueue.invokeLater(WindradApp::new);
	}

	public WindradApp() {
		new WindparkFenster(Windpark.laden(), 1260, 700);
	}
}