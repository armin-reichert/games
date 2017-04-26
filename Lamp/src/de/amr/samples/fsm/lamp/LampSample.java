package de.amr.samples.fsm.lamp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;

public class LampSample {

	public static final LampSample App = new LampSample();

	public static void main(String[] args) {
		EventQueue.invokeLater(App::showUI);
	}

	private final LampControl lampControl;
	private final Lamp lamp;

	private void showUI() {
		JFrame frame = new JFrame("Lamp Sample");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.add(lamp, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	public LampSample() {
		lamp = new Lamp();
		lampControl = new LampControl(lamp);
		lampControl.init();
		lamp.getLightSwitch().addActionListener((e) -> lampControl.run(LampControl.SWITCHED));
	}
}