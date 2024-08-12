package com.boracompany.mygame.Controller;

import com.boracompany.mygame.Model.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameController {

	private static final Logger LOGGER = LogManager.getLogger(GameController.class);

	public void attack(Player attacker, Player defender) {
		// TODO Auto-generated method stub
		if (attacker != null && defender != null) {

			float defenderHealth = defender.getHealth();
			final float damage = attacker.getDamage();
			float newHealth = defenderHealth - damage;
			if (newHealth >= 0)
				defender.setHealth(defenderHealth - damage);
			else {
				defender.setHealth(0);
			}

		} else {
			throw new IllegalArgumentException("Attacker or defender is not valid");
		}
	}
}
