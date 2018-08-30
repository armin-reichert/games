package de.amr.games.montagsmaler.sounds;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public enum Sound {
	SOLVED("fanfare.wav"),
	GAMEOVER("gong.wav"),
	HYMN("olympic.wav"),
	GAMESTART("pacman.wav"),
	TICK("tick.wav");

	private final URL url;
	private Clip clip;

	private Sound(String soundFileName) {
		try {
			url = getClass().getResource(soundFileName);
		} catch (Exception e) {
			throw new RuntimeException("Could not open audio clip: " + soundFileName, e);
		}
	}

	public void start() {
		try {
			clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(url));
			clip.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		if (clip != null) {
			clip.stop();
		}
	}

	public long getMicrosecondLength() {
		if (clip != null) {
			return clip.getMicrosecondLength();
		}
		throw new NullPointerException();
	}
}
