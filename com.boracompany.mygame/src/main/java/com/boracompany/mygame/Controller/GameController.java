package com.boracompany.mygame.Controller;

import com.boracompany.mygame.Model.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
public class GameController {
	
	private static final Logger LOGGER = LogManager.getLogger(GameController.class);
	
	
	public void attack(Player attacker, Player defender) {
		// TODO Auto-generated method stub
		if (attacker !=null && defender != null) {
			defender.setHealth(defender.getHealth()-attacker.getDamage());
		}
		else {
			throw new IllegalArgumentException("Attacker or defender is not valid");
		}

	}
	


}
