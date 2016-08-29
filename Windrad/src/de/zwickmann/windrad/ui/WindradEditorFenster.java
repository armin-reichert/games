package de.zwickmann.windrad.ui;

import java.awt.BorderLayout;

import javax.swing.JDialog;

import de.zwickmann.windrad.model.WindradModell;

public class WindradEditorFenster extends JDialog {

	private WindradEditor controlPanel;

	public WindradEditorFenster(WindparkAnsicht windparkAnsicht, WindradModell windrad) {
		super((JDialog) null, true);
		setTitle("Windrad Eigenschaften");
		controlPanel = new WindradEditor();
		controlPanel.setModel(windrad);
		controlPanel.setView(windparkAnsicht);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(controlPanel, BorderLayout.CENTER);
		pack();
	}
}