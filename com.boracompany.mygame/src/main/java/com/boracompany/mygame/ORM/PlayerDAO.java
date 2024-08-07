package com.boracompany.mygame.ORM;

import java.util.List;

import com.boracompany.mygame.Model.Player;

public interface PlayerDAO {
	   public List<Player> getAllPlayers();
	   public Player getPlayer(Long ID);
	   public void updatePlayer(Player player);
	   public void deletePlayer(Player player);
}