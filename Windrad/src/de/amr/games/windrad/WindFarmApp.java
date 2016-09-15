package de.amr.games.windrad;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import de.amr.games.windrad.model.WindFarm;
import de.amr.games.windrad.ui.WindFarmWindow;

public class WindFarmApp {

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
		EventQueue.invokeLater(WindFarmApp::new);
	}

	public WindFarmApp() {
		new WindFarmWindow(WindFarm.loadFromFile(), 1260, 700);
	}
}