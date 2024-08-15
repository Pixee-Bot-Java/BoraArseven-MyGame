package com.boracompany.mygame;

import static org.junit.Assert.assertFalse;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.boracompany.mygame.Controller.GameController;
import com.boracompany.mygame.Model.Player;
import com.boracompany.mygame.Model.PlayerBuilder;

class TestGameController {
	private static final Logger LOGGER = LogManager.getLogger(TestGameController.class);
	GameController controller;
	PlayerBuilder builder;

	private Logger logger;
	private GameController controllerSpy;

	@BeforeEach
	void Setup() throws Exception {
		controller = new GameController();
		builder = new PlayerBuilder(); // Initialize the builder once

		// Mock the Logger
		logger = mock(Logger.class);

		// Spy on the GameController
		controllerSpy = spy(new GameController());

		// Use reflection to replace the static final LOGGER with our mock
		Field loggerField = GameController.class.getDeclaredField("LOGGER");
		loggerField.setAccessible(true);

		// Replace the static logger instance with the mock logger
		loggerField.set(null, logger);
	}

	@Test
	void testWhenAttackingDefendingPlayerisNullThrowsException() {
		Player attacker = builder.resetBuilder().withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender = null;

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, defender);
		});
		assertEquals("Attacker or defender is null.", exception.getMessage());
	}

	@Test
	void testwhenAttackingAttackingPLayerisNullThrowsException() {
		Player attacker = null;
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(30).build();

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, defender);
		});
		assertEquals("Attacker or defender is null.", exception.getMessage());
	}

	@Test
	void testWhenAttackingBothPLayersareNullThrowsException() {
		Player attacker = null;
		Player defender = null;

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, defender);
		});
		assertEquals("Attacker or defender is null.", exception.getMessage());
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
		Player attacker = builder.resetBuilder().withDamage(Float.MAX_VALUE).withName("Attacker").withHealth(30)
				.build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(50).build();
		controller.attack(attacker, defender);
		assertEquals(0, defender.getHealth());
		assertEquals(false, defender.Isalive());
	}

	@Test
	void AttackerDealsExactDamageToKillDefender() {
		Player attacker = builder.resetBuilder().withDamage(50).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(50).build();

		controller.attack(attacker, defender);

		assertEquals(0, defender.getHealth());
		assertEquals(false, defender.Isalive());
	}

	@Test
	void AttackerDealsDamageToIncapacitateDefender() {
		Player attacker = builder.resetBuilder().withDamage(60).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(50).build();

		controller.attack(attacker, defender);

		assertEquals(0, defender.getHealth());
		assertEquals(false, defender.Isalive());
	}

	@Test
	void AttackerDealsNonLethalDamageToDefender() {
		Player attacker = builder.resetBuilder().withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(50).build();

		controller.attack(attacker, defender);

		assertEquals(40, defender.getHealth());
		assertEquals(true, defender.Isalive());
	}

	@Test
	void TestLoggingForAttackSuccess() {
		Player attacker = builder.resetBuilder().withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(20).build();

		controller.attack(attacker, defender);

		assertEquals(10, defender.getHealth());
		// This test ensures the attack success condition is covered and indirectly the
		// log message.
	}

	@Test
	void TestDefenderHealthBoundaryAtZero() {
		Player attacker = builder.resetBuilder().withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(10).build();

		controller.attack(attacker, defender);

		assertEquals(0, defender.getHealth());
		assertEquals(false, defender.Isalive()); // Ensure that defender is marked as dead.
		assertEquals("Defender", defender.getName()); // Check that name retrieval works after setting health to 0.
	}

	@Test
	void TestDamageZeroShouldFail() {
		Player attacker = builder.resetBuilder().withDamage(0).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(50).build();

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, defender);
		});
		assertEquals("Damage should be positive", exception.getMessage());
		assertEquals("Attacker", attacker.getName()); // Ensure name retrieval works after invalid attack.
	}

	// Ensure attacking with positive damage works
	@Test
	void TestAttackingWithPositiveDamage() {
		Player attacker = builder.resetBuilder().withDamage(10).withName("Attacker").withHealth(30).build();
		Player defender = builder.resetBuilder().withDamage(10).withName("Defender").withHealth(50).build();

		controller.attack(attacker, defender);

		assertEquals(40, defender.getHealth()); // Health should decrease
		assertTrue(defender.Isalive()); // Defender should still be alive
	}

	@Test
	void testValidatePlayersWithNonNullValues() {
		Player attacker = mock(Player.class);
		Player defender = mock(Player.class);

		when(attacker.getDamage()).thenReturn(10.0f);

		controller.attack(attacker, defender);
	}

	@Test
	void testValidatePlayersWithNullAttacker() {
		Player defender = mock(Player.class);
		// This should throw an IllegalArgumentException
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(null, defender);
		});
		assertTrue(exception.getMessage().contains("Attacker or defender is null"));
	}

	@Test
	void testValidatePlayersWithNullDefender() {
		Player attacker = mock(Player.class);
		// This should throw an IllegalArgumentException
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, null);
		});
		assertTrue(exception.getMessage().contains("Attacker or defender is null"));
	}

	@Test
	void testCalculateDamageWithPositiveValue() {
		Player attacker = mock(Player.class);
		when(attacker.getDamage()).thenReturn(10.0f);
		// This should not throw an exception
		controller.attack(attacker, mock(Player.class));
	}

	@Test
	void testCalculateDamageWithZeroValue() {
		Player attacker = mock(Player.class);
		when(attacker.getDamage()).thenReturn(0.0f);
		// This should throw an IllegalArgumentException
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, mock(Player.class));
		});
		assertTrue(exception.getMessage().contains("Damage should be positive"));
	}

	@Test
	void testCalculateDamageWithNegativeValue() {
		Player attacker = mock(Player.class);
		when(attacker.getDamage()).thenReturn(-5.0f);
		// This should throw an IllegalArgumentException
		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			controller.attack(attacker, mock(Player.class));
		});
		assertTrue(exception.getMessage().contains("Damage should be positive"));
	}

	@Test
	void testAttackSuccess() {
		// Create Players using the builder pattern
		Player attacker = new PlayerBuilder().resetBuilder().withName("Attacker").withDamage(100).withIsAlive(true)
				.build();
		Player defender = new PlayerBuilder().resetBuilder().withName("Defender").withDamage(50).withIsAlive(true)
				.withHealth(150).build();

		// Run the attack method
		controllerSpy.attack(attacker, defender);

		// Verify that the public behavior of the GameController is correct
		assertEquals(50, defender.getHealth());
		assertTrue(defender.Isalive());

		// Verify that logging occurred with the correct messages
		verify(logger).info(anyString(), eq("Attacker"), eq(100f), eq("Defender"), eq(150f));
		verify(logger).info(anyString(), eq("Defender"), eq(50f));
	}

	@Test
	void testAttackWithZeroDamageThrowsException() {
		// Create Players using the builder pattern
		Player attacker = new PlayerBuilder().resetBuilder().withName("Attacker").withDamage(0).withIsAlive(true)
				.build();
		Player defender = new PlayerBuilder().resetBuilder().withName("Defender").withDamage(50).withIsAlive(true)
				.withHealth(150).build();

		// Test that the exception is thrown when damage is zero
		assertThrows(IllegalArgumentException.class, () -> controllerSpy.attack(attacker, defender));

		// Verify that the error logging occurred with the correct message
		verify(logger).error("Attack failed: Damage should be positive");
	}

	@Test
	void testNullPlayerThrowsException() {
		// Create Players with one null player
		Player attacker = null;
		Player defender = new PlayerBuilder().resetBuilder().withName("Defender").withDamage(50).withIsAlive(true)
				.withHealth(150).build();

		// Test that the exception is thrown when attacker is null
		assertThrows(IllegalArgumentException.class, () -> controllerSpy.attack(attacker, defender));

		// Verify that the error logging occurred with the correct message
		verify(logger).error("Attack failed: Attacker or defender is null.");
	}

	@Test
	void testDefenderDefeated() {
		// Create Players using the builder pattern
		Player attacker = new PlayerBuilder().resetBuilder().withName("Attacker").withDamage(200).withIsAlive(true)
				.build();
		Player defender = new PlayerBuilder().resetBuilder().withName("Defender").withDamage(50).withIsAlive(true)
				.withHealth(150).build();

		// Run the attack method
		controllerSpy.attack(attacker, defender);

		// Verify the defender's health and alive status
		assertEquals(0, defender.getHealth());
		assertFalse(defender.Isalive());

		// Verify that the correct logging occurred
		verify(logger).info(eq("Attack successful: Defender: {} has been defeated (Health: 0, IsAlive: {})"),
				eq("Defender"), eq(false));

	}

	@Test
	void testCalculateDamageWithPositiveValueAndZeroSubstitution() {
		Player attacker = builder.resetBuilder().withDamage(1).build();
		Player defender = builder.resetBuilder().withHealth(100).build();

		controller.attack(attacker, defender);

		assertEquals(99, defender.getHealth()); // Ensure damage is reduced by 1
	}

	@Test
	void testUpdateDefenderHealthCallsSetAlive() {
		Player attacker = builder.resetBuilder().withDamage(100).build();
		Player defender = spy(builder.resetBuilder().withHealth(50).withIsAlive(true).build());

		controller.attack(attacker, defender);

		assertEquals(0, defender.getHealth()); // Ensure defender's health is 0
		verify(defender).setAlive(false); // Verify setAlive(false) is called
		assertFalse(defender.Isalive()); // Ensure defender is not alive
	}

	@Test
	void testDefenderHealthIsNotSetTo1WhenHealthIsZero() {
		// Create attacker and defender
		Player attacker = builder.resetBuilder().withDamage(50).withName("Attacker").withHealth(100).build();
		Player defender = builder.resetBuilder().withName("Defender").withHealth(50).build();

		// Call the attack method, which should reduce defender's health to exactly 0
		controller.attack(attacker, defender);

		// Assert that the defender's health is exactly 0, not 1
		assertEquals(0, defender.getHealth());
		assertFalse(defender.Isalive()); // Ensure defender is dead
	}

	@Test
	void testIsAliveCalledCorrectlyWhenDefenderDies() {
		// Create attacker and defender
		Player attacker = builder.resetBuilder().withDamage(100).withName("Attacker").withHealth(100).build();
		Player defender = builder.resetBuilder().withName("Defender").withHealth(50).build();

		// Spy on the defender object to verify that Isalive() is called
		Player defenderSpy = spy(defender);

		// Call the attack method, which should kill the defender
		controller.attack(attacker, defenderSpy);

		// Verify that the Isalive() method is called during the attack (inside
		// updateDefenderHealth)
		InOrder inOrder = Mockito.inOrder(defenderSpy);
		inOrder.verify(defenderSpy).setAlive(false);
		inOrder.verify(defenderSpy).Isalive();
	}

}
