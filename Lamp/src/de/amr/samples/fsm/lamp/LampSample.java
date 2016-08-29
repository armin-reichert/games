package de.amr.samples.fsm.lamp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;

public class LampSample {

	private final LampControl lampControl;
	private final Lamp lamp;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> new LampSample());
	}

	public LampSample() {
		lamp = new Lamp();
		lampControl = new LampControl(lamp);
		lampControl.init();
		lamp.getLightSwitch().addActionListener((e) -> lampControl.run(LampControl.SWITCHED));
		JFrame frame = new JFrame("Lamp Sample");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setBackground(Color.BLACK);
		frame.add(lamp, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
}