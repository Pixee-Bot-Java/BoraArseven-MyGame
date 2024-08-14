package com.boracompany.mygame.Controller;

import com.boracompany.mygame.Model.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameController {

	private static final Logger LOGGER = LogManager.getLogger(GameController.class);

	public void attack(Player attacker, Player defender) {
		if (attacker == null || defender == null) {
			LOGGER.error("Attack failed: Attacker or defender is null. Attacker: {}, Defender: {}", attacker, defender);
			throw new IllegalArgumentException("Attacker or defender is not valid");
		}
		float damage = attacker.getDamage();
		if (damage <= 0) {
			LOGGER.error("Attack failed: Damage must be positive. Attacker: {}, Damage: {}", attacker.getName(),
					damage);
			throw new IllegalArgumentException("Damage should be positive");
		}

		float defenderHealth = defender.getHealth();
		LOGGER.info("Attack initiated: Attacker: {} (Damage: {}), Defender: {} (Health: {})", attacker.getName(),
				damage, defender.getName(), defenderHealth);

		float newHealth = defenderHealth - damage;

		if (newHealth > 0) {
			defender.setHealth(newHealth);
			LOGGER.info("Attack successful: Defender: {}'s new health: {}", defender.getName(), newHealth);
		} else {
			defender.setHealth(0);
			defender.setAlive(false);
			LOGGER.info("Attack successful: Defender: {} has been defeated (Health: 0, IsAlive: {})",
					defender.getName(), defender.Isalive());
		}
	}

}
