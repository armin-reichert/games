package de.amr.games.montagsmaler.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a team in the game.
 */
public class Team {

	private String name;
	private List<Player> players = new ArrayList<Player>(3);

	public Team(String name) {
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addPlayer(Player player) {
		players.add(player);
	}

	public void deletePlayer(Player player) {
		players.remove(player);
	}

	public boolean isEmpty() {
		return players.size() == 0;
	}

	public List<Player> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	public int getPoints() {
		int points = 0;
		for (Player player : players) {
			points += player.getPoints();
		}
		return points;
	}
}
