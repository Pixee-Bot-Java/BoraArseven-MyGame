package com.boracompany.mygame.Controller;

import com.boracompany.mygame.Model.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameController {

	private static final Logger LOGGER = LogManager.getLogger(GameController.class);

	public void attack(Player attacker, Player defender) {
		validatePlayers(attacker, defender);

		float damage = calculateDamage(attacker);

		float defenderHealth = defender.getHealth();
		logAttackInitiation(attacker, defender, damage, defenderHealth);

		float newHealth = calculateNewHealth(defenderHealth, damage);

		updateDefenderHealth(defender, newHealth);
	}

	private void validatePlayers(Player attacker, Player defender) {
		if (attacker == null || defender == null) {
			logAndThrowError("Attacker or defender is null.");
		}
	}

	private float calculateDamage(Player attacker) {
		float damage = attacker.getDamage();
		if (damage <= 0) {
			logAndThrowError("Damage should be positive");
		}
		return damage;
	}

	private void logAndThrowError(String message) {
		LOGGER.error("Attack failed: " + message);
		throw new IllegalArgumentException(message);
	}

	private void logAttackInitiation(Player attacker, Player defender, float damage, float defenderHealth) {
		LOGGER.info("Attack initiated: Attacker: {} (Damage: {}), Defender: {} (Health: {})", attacker.getName(),
				damage, defender.getName(), defenderHealth);
	}

	private float calculateNewHealth(float defenderHealth, float damage) {
		return defenderHealth - damage;
	}

	private void updateDefenderHealth(Player defender, float newHealth) {
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
