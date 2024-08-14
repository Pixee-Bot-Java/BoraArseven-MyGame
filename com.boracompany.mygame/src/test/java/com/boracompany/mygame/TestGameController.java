package com.boracompany.mygame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.boracompany.mygame.Controller.GameController;
import com.boracompany.mygame.Model.Player;
import com.boracompany.mygame.Model.PlayerBuilder;

class TestGameController {
	private static final Logger LOGGER = LogManager.getLogger(TestGameController.class);
	GameController controller;
	PlayerBuilder builder;

	@BeforeEach
	void Setup() {
		controller = new GameController();
		builder = new PlayerBuilder(); // Initialize the builder once
	}

	@Test
	void testWhenAttackingDefendingPlayerisNullThrowsException() {
		Player attacker = builder.resetBuilder().withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender = null;

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, defender);
		});
		assertEquals("Attacker or defender is not valid", exception.getMessage());
	}

	@Test
	void testwhenAttackingAttackingPLayerisNullThrowsException() {
		Player attacker = null;
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(30).build();

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, defender);
		});
		assertEquals("Attacker or defender is not valid", exception.getMessage());
	}

	@Test
	void testWhenAttackingBothPLayersareNullThrowsException() {
		Player attacker = null;
		Player defender = null;

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, defender);
		});
		assertEquals("Attacker or defender is not valid", exception.getMessage());
	}

	@Test
	void AttackerReducesHealthOfDefender() {
		Player attacker = builder.resetBuilder().withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(30).build();

		controller.attack(attacker, defender);

		attacker.setDamage(5);
		controller.attack(attacker, defender);

		assertEquals(15, defender.getHealth());
	}

	@Test
	void AttackerReducesHealthOfDefenderNotMinus() {
		Player attacker = builder.resetBuilder().withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(10).build();

		controller.attack(attacker, defender);

		attacker.setDamage(5);
		controller.attack(attacker, defender);

		assertEquals(0, defender.getHealth());
	}

	@Test
	void DefenderDiesIfHealthsmallerthanzero() {
		Player attacker = builder.resetBuilder().withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(10).build();

		controller.attack(attacker, defender);

		attacker.setDamage(5);
		controller.attack(attacker, defender);

		assertEquals(0, defender.getHealth());
		assertEquals(false, defender.Isalive());
	}

	@Test
	void DefenderNotDiesIfHealthbiggerthanzero() {
		Player attacker = builder.resetBuilder().withDamage(5).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(50).build();

		LOGGER.debug("Attacker created with damage: {}", attacker.getDamage());

		controller.attack(attacker, defender);

		LOGGER.debug("Defender's health after first attack: {}", defender.getHealth());

		attacker.setDamage(15);

		LOGGER.debug("Attacker's damage updated to: {}", attacker.getDamage());

		controller.attack(attacker, defender);

		LOGGER.debug("Defender's health after second attack: {}", defender.getHealth());

		assertEquals(30, defender.getHealth());
		assertEquals(true, defender.Isalive());
	}

	@Test
	void DamageShouldNotNull() {
		Player attacker = builder.resetBuilder().withDamage(-5).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(50).build();

		LOGGER.debug("Attacker created with damage: {}", attacker.getDamage());

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, defender);
		});
		assertEquals("Damage should be positive", exception.getMessage());
	}

	@Test
	void DamageShouldBePositive() {
		Player attacker = builder.resetBuilder().withDamage(-5).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(50).build();

		LOGGER.debug("Attacker created with damage: {}", attacker.getDamage());

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, defender);
		});
		assertEquals("Damage should be positive", exception.getMessage());
	}
	@Test
	void DamageShouldBeNonZero() {
	    Player attacker = builder.resetBuilder().withDamage(0).withName("Attacker").withHealth(30).build();
	    Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(50).build();

	    LOGGER.debug("Attacker created with damage: {}", attacker.getDamage());

	    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
	        controller.attack(attacker, defender);
	    });
	    assertEquals("Damage should be positive", exception.getMessage());
	}
	@Test
	void MaximumDamageHandling() {
	    Player attacker = builder.resetBuilder().withDamage(Float.MAX_VALUE).withName("Attacker").withHealth(30).build();
	    Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(50).build();
	    controller.attack(attacker, defender);
	    assertEquals(0, defender.getHealth());
	    assertEquals(false, defender.Isalive());
	}
}
